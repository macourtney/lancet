@echo off

set HOME_DIR=.
set LIB_DIR=%HOME_DIR%/lib

set JARS_PATH=%LIB_DIR%/*

set CLASS_PATH=.;%JARS_PATH%

@echo on
@echo %CLASS_PATH%
@echo off

java -cp %CLASS_PATH% clojure.lang.Script build.clj