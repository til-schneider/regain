@echo off

set GEN_CP=%CLASSPATH%;lib\jacobgen.jar;lib\samskivert.jar
set PATH=%PATH%;lib

%JAVA_HOME%\bin\java -cp %GEN_CP% it.bigatti.jacobgen.Jacobgen %1 %2 %3 %4 %5 %6 %7 %8 %9
