@echo off
set BASE_PACKAGE=de.filiadata.lucene.spider.generated

echo Generating classes for MS Word...
call jacobgen -destdir:generated-src -additional:Office:%BASE_PACKAGE%.msoffice2000.shared -package:%BASE_PACKAGE%.msoffice2000.word Msword9.olb

echo Generating classes for MS Excel...
call jacobgen -destdir:generated-src -additional:Office:%BASE_PACKAGE%.msoffice2000.shared -package:%BASE_PACKAGE%.msoffice2000.excel Excel9.olb

echo Generating classes for MS Powerpoint...
call jacobgen -destdir:generated-src -additional:Office:%BASE_PACKAGE%.msoffice2000.shared -package:%BASE_PACKAGE%.msoffice2000.powerpoint Msppt9.olb
