
;--------------------------------
; Include Modern UI
!include "MUI.nsh"


;--------------------------------
; Configuration

; program name
Name "${PROG_NAME}"

; The file to write
OutFile "${PUBLIC_DIR}\${PROG_NAME_FILE}_v${VERSION_FILE}_desktop_win.exe"

; The default installation directory
InstallDir "$PROGRAMFILES\${PROG_NAME}"

; Get installation folder from registry if available
InstallDirRegKey HKCU "Software\${PROG_NAME}" ""

; Use LZMA compression
SetCompressor lzma

; The icons of the installer and uninstaller
!define MUI_ICON "${NSISDIR}\Contrib\Graphics\Icons\murfman-install.ico" 
!define MUI_UNICON "${NSISDIR}\Contrib\Graphics\Icons\murfman-uninstall.ico"

!define MUI_WELCOMEPAGE_TITLE_3LINES
!define MUI_FINISHPAGE_TITLE_3LINES
!define MUI_UNWELCOMEPAGE_TITLE_3LINES
!define MUI_UNFINISHPAGE_TITLE_3LINES

; Use no descriptions in the components page
!define MUI_COMPONENTSPAGE_NODESC

; The file to run on the finish page
!define MUI_FINISHPAGE_RUN "$INSTDIR\regain.exe"

;--------------------------------
;Variables

Var MUI_TEMP
Var STARTMENU_FOLDER


;--------------------------------
;Interface Settings

!define MUI_ABORTWARNING


;--------------------------------
;Pages

!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_LICENSE "txt\license.txt"
!insertmacro MUI_PAGE_COMPONENTS
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_STARTMENU Application $STARTMENU_FOLDER
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH

!insertmacro MUI_UNPAGE_WELCOME
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES
!insertmacro MUI_UNPAGE_FINISH


;--------------------------------
;Languages
 
!insertmacro MUI_LANGUAGE "English"
!insertmacro MUI_LANGUAGE "German"

LangString SecProgName          ${LANG_ENGLISH} "${PROG_NAME} (required)"
LangString SecProgName          ${LANG_GERMAN}  "${PROG_NAME} (erforderlich)"
LangString SecLinkName          ${LANG_ENGLISH} "Links"
LangString SecLinkName          ${LANG_GERMAN}  "Verknüpfungen"
LangString SecLinkStartmenuName ${LANG_ENGLISH} "Links in the start menu"
LangString SecLinkStartmenuName ${LANG_GERMAN}  "Verknüpfungen im Start-Menü"
LangString LicenseTerms         ${LANG_ENGLISH} "License terms"
LangString LicenseTerms         ${LANG_GERMAN}  "Lizenzbedingungen"
LangString UninstallRegain      ${LANG_ENGLISH} "Uninstall ${PROG_NAME}"
LangString UninstallRegain      ${LANG_GERMAN}  "${PROG_NAME} deinstallieren"
LangString SecLinkAutostartName ${LANG_ENGLISH} "Link in autostart folder"
LangString SecLinkAutostartName ${LANG_GERMAN}  "Verknüpfung im Autostart-Ordner"


;--------------------------------
;Installer Sections

Section $(SecProgName)
  ; make the section requiered
  SectionIn RO

  ; Store installation folder
  WriteRegStr HKCU "Software\${PROG_NAME}" "" $INSTDIR

  SetOutPath $INSTDIR
  File txt\license.txt
  File txt\WhatIsNew.txt
  File txt\WasIstNeu.txt
  File build\runtime\desktop\win\*.dll
  File build\runtime\desktop\win\regain.exe

  SetOutPath $INSTDIR\conf\default
  File build\runtime\desktop\win\conf\default\*

  SetOutPath $INSTDIR\preparator
  File build\runtime\desktop\win\preparator\*

  SetOutPath $INSTDIR\web
  File build\runtime\desktop\win\web\*

  SetOutPath $INSTDIR\web\img
  File build\runtime\desktop\win\web\img\*

  SetOutPath $INSTDIR\web\img\ext
  File build\runtime\desktop\win\web\img\ext\*

  SetOutPath $INSTDIR
  WriteUninstaller "Uninstall.exe"

  # Register uninstaller at Windows (Add/Remove programs)
  WriteRegExpandStr \
    HKLM \
    "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROG_NAME_FILE}" \
    "UninstallString" \
    "$INSTDIR\Uninstall.exe"
  WriteRegExpandStr \
    HKLM \
    "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROG_NAME_FILE}" \
    "InstallLocation" \
    "$INSTDIR"
  WriteRegStr \
    HKLM \
    "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROG_NAME_FILE}" \
    "DisplayName" \
    "${PROG_NAME} ${VERSION}"
  WriteRegStr \
    HKLM \
    "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROG_NAME_FILE}" \
    "DisplayIcon" \
    "$INSTDIR\regain.exe,0"
  WriteRegStr \
    HKLM \
    "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROG_NAME_FILE}" \
    "DisplayVersion" \
    "${VERSION}"
  WriteRegStr \
    HKLM \
    "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROG_NAME_FILE}" \
    "URLUpdateInfo" \
    "http://regain.sourceforge.net"

SectionEnd ; end the section


SubSection /e $(SecLinkName)

  Section $(SecLinkStartmenuName)
    !insertmacro MUI_STARTMENU_WRITE_BEGIN Application
  
      CreateDirectory "$SMPROGRAMS\$STARTMENU_FOLDER"
    
      CreateShortCut \
        "$SMPROGRAMS\$STARTMENU_FOLDER\${PROG_NAME}.lnk" \
        "$INSTDIR\regain.exe"

      CreateShortCut \
        "$SMPROGRAMS\$STARTMENU_FOLDER\$(LicenseTerms).lnk" \
        "$INSTDIR\license.txt"

      CreateShortCut \
        "$SMPROGRAMS\$STARTMENU_FOLDER\Was ist neu.lnk" \
        "$INSTDIR\WhatIsNew.txt"

      CreateShortCut \
        "$SMPROGRAMS\$STARTMENU_FOLDER\What is new.lnk" \
        "$INSTDIR\WhatIsNew.txt"

      CreateShortCut \
        "$SMPROGRAMS\$STARTMENU_FOLDER\$(UninstallRegain).lnk" \
        "$INSTDIR\uninstall.exe" \
        "" \
        "$INSTDIR\uninstall.exe" \
        0
  
    !insertmacro MUI_STARTMENU_WRITE_END
  SectionEnd


  Section $(SecLinkAutostartName)
    CreateShortCut \
      "$SMSTARTUP\${PROG_NAME}.lnk" \
      "$INSTDIR\regain.exe"
  SectionEnd

SubSectionEnd


; special uninstall section.
Section "Uninstall"
  ; remove directories used.
  RMDir /r "$INSTDIR"

  ; Remove start menu shortcuts
  !insertmacro MUI_STARTMENU_GETFOLDER Application $MUI_TEMP
  RMDir /r "$SMPROGRAMS\$MUI_TEMP"

  ; remove desktop shortcut
  Delete "$DESKTOP\${PROG_NAME}.lnk"

  ; remove the autostart link
  Delete "$SMSTARTUP\${PROG_NAME}.lnk" \

  # Unregister uninstaller at Windows (Add/Remove programs)
  DeleteRegKey \
    HKLM \
    "Software\Microsoft\Windows\CurrentVersion\Uninstall\${PROG_NAME_FILE}"

SectionEnd

;eof
