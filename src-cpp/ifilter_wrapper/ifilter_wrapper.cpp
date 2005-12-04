/*
 * regain - A file search engine providing plenty of formats
 * Copyright (C) 2004  Til Schneider
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Contact: Til Schneider, info@murfman.de
 *
 * CVS information:
 *  $RCSfile: ifilter_wrapper.cpp,v $
 *   $Source: /cvsroot/regain/regain/src-cpp/ifilter_wrapper/ifilter_wrapper.cpp,v $
 *     $Date: 2005/10/28 16:00:23 $
 *   $Author: til132 $
 * $Revision: 1.1 $
 */

#include "stdafx.h"
#include "ifilter_wrapper.h"

#define _WIN32_DCOM

#include <objbase.h>
#include <wchar.h>
#include <atlconv.h>

// IFilter
#include <filter.h>
#include <filterr.h>

// IPersistFile
#include <objidl.h>

#ifndef ifilter_wrapper
#define ifilter_wrapper

#define COM_FIELD_NAME "mPersistentHandler"

// Defines the main entry point for the DLL
BOOL APIENTRY DllMain( HANDLE hModule, 
                       DWORD  ul_reason_for_call, 
                       LPVOID lpReserved
					 )
{
    return TRUE;
}


void ThrowException(JNIEnv *env, const char* desc, jint hr)
{
    jclass failClass = env->FindClass("net/sf/regain/RegainException");
    jmethodID failCons = 
       env->GetMethodID(failClass, "<init>", "(Ljava/lang/String;)V");
    if (!desc) desc = "Java/COM Error";
    jstring js = env->NewStringUTF(desc);
    jthrowable fail = (jthrowable)env->NewObject(failClass, failCons, js);
    env->Throw(fail);
}


// NOTE: Documentation of the IFilter interface:
//       http://msdn.microsoft.com/library/default.asp?url=/library/en-us/indexsrv/html/ixrefint_9sfm.asp
// NOTE: Documentation of the IPersistFile interface:
//       http://msdn.microsoft.com/library/default.asp?url=/library/en-us/com/html/7d34507f-8a16-43b4-8225-010798abc546.asp


JNIEXPORT void JNICALL Java_net_sf_regain_crawler_preparator_ifilter_IfilterWrapper_doCoInitialize
  (JNIEnv *env, jclass clazz, jint mode)
{
	int threadMode = mode;
	CoInitializeEx(NULL, threadMode);
}


JNIEXPORT void JNICALL Java_net_sf_regain_crawler_preparator_ifilter_IfilterWrapper_doCoUninitialize
  (JNIEnv *env, jclass clazz)
{
	CoUninitialize();
}


JNIEXPORT void JNICALL Java_net_sf_regain_crawler_preparator_ifilter_IfilterWrapper_init
  (JNIEnv *env, jobject obj, jstring _progid)
{
	jclass clazz = env->GetObjectClass(obj);
	jfieldID jf = env->GetFieldID(clazz, COM_FIELD_NAME, "J");

	const char *progid = env->GetStringUTFChars(_progid, NULL);
	HRESULT hr;
	IUnknown *punk = NULL;
	USES_CONVERSION;
	LPOLESTR bsProgId = A2W(progid);

    env->ReleaseStringUTFChars(_progid, progid);
    // it's a moniker
    hr = CoGetObject(bsProgId, NULL, IID_IUnknown, (LPVOID *)&punk);
    if (FAILED(hr)) {
		ThrowException(env, "Can't find moniker", hr);
		return;
    }

	IClassFactory *pIClass;
    // if it was a clsid moniker, I may have a class factory
    hr = punk->QueryInterface(IID_IClassFactory, (void **)&pIClass);
	if (SUCCEEDED(hr)) {
		punk->Release();
		// try to create an instance
		hr = pIClass->CreateInstance(NULL, IID_IUnknown, (void **)&punk);
		if (FAILED(hr)) {
			ThrowException(env, "Can't create moniker class instance", hr);
			return;
		}
		pIClass->Release();
	}

	// Store a pointer to the IFilter in the Java Object
	env->SetLongField(obj, jf, (SIZE_T)punk);
}


JNIEXPORT void JNICALL Java_net_sf_regain_crawler_preparator_ifilter_IfilterWrapper_getText
  (JNIEnv *env, jobject obj, jstring _fileName, jobject stringBuffer,
   jboolean showTextEndings)
{
	jclass clazz = env->GetObjectClass(obj);
	jfieldID jf = env->GetFieldID(clazz, COM_FIELD_NAME, "J");
	jlong num = env->GetLongField(obj, jf);

    IPersistFile *pPersistFile;
    IFilter *pFilter;

	HRESULT hr;
	STAT_CHUNK st;
	SCODE scode;

	IUnknown *punk = (IUnknown *)num;
	if (! punk) {
		ThrowException(env, "Not initialized", hr);
		return;
	}

	hr = punk->QueryInterface(IID_IPersistFile, (void **)&pPersistFile);
	if (!SUCCEEDED(hr)) {
		ThrowException(env, "Can't query interface object for IPersistFile", hr);
		return;
	}

	hr = punk->QueryInterface(IID_IFilter, (void **)&pFilter);
	if (!SUCCEEDED(hr)) {
		pPersistFile->Release();
		ThrowException(env, "Can't query interface object for IFilter", hr);
		return;
	}

	const jchar *fileName = env->GetStringChars(_fileName, NULL);
	pPersistFile->Load(fileName, 0);
	env->ReleaseStringChars(_fileName, fileName);

	ULONG pwdFlags;
	scode = pFilter->Init(0, 0, NULL, &pwdFlags);
	if (scode != S_OK) {
		pPersistFile->Release();
		pFilter->Release();
		ThrowException(env, "Initializing IFilter failed", hr);
		return;
	}

	jclass sbufClass = env->GetObjectClass(stringBuffer);
	jmethodID appendID = env->GetMethodID(sbufClass, "append",
		"(Ljava/lang/String;)Ljava/lang/StringBuffer;");

	const ULONG bufferSize = 1024;
	WCHAR buffer[bufferSize];
	ULONG len;

	while ((scode = pFilter->GetChunk(&st)) != FILTER_E_END_OF_CHUNKS) {
		if (st.flags == CHUNK_TEXT) {

			while (true) {
				len = bufferSize;
				// NOTE: len will set by GetText to the correct length
				scode = pFilter->GetText(&len, buffer);

				if (scode == FILTER_E_NO_MORE_TEXT) {
					// We are done
					break;
				}

				//printf("C: size: %d\n", len);

				// Create a Java String
				jstring str = env->NewString((jchar*)buffer, len);

				// Append it to the StringBuffer
				env->CallObjectMethod(stringBuffer, appendID, str);

				if (showTextEndings) {
					str = env->NewStringUTF("\n<end of text>\n");
					env->CallObjectMethod(stringBuffer, appendID, str);
				}
			}

			// End of chunk
			if (showTextEndings) {
				jstring str = env->NewStringUTF("\n<end of chunk>\n");
				env->CallObjectMethod(stringBuffer, appendID, str);
			}
			jstring str = env->NewStringUTF("\n");
			env->CallObjectMethod(stringBuffer, appendID, str);
		}
	}

	// Release the Interfaces
	pPersistFile->Release();
	pFilter->Release();
}


JNIEXPORT void JNICALL Java_net_sf_regain_crawler_preparator_ifilter_IfilterWrapper_close
  (JNIEnv *env, jobject obj)
{
	jclass clazz = env->GetObjectClass(obj);
	jfieldID jf = env->GetFieldID(clazz, COM_FIELD_NAME, "J");
	jlong num = env->GetLongField(obj, jf);

	IUnknown *punk = (IUnknown *)num;
	punk->Release();
	env->SetLongField(obj, jf, (SIZE_T)NULL);
}


#endif 