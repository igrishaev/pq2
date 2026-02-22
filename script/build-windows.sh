#!/bin/bash

set -euo pipefail
set -x

OBJ=org_pq_Native

# remove traing slash
JAVA_HOME=$(echo ${JAVA_HOME%/})

JAVA_INC="-I${JAVA_HOME}/include -I${JAVA_HOME}/include/win32"

PLATFORM=$(cat PLATFORM)

g++ -fPIC ${JAVA_INC} -c ${OBJ}.cpp -o ${OBJ}.o
g++ -shared ${OBJ}.o -lpq -o ${PLATFORM}.dll

MSYS_DIR=D:/a/_temp/msys64/${MSYS}

ls -l ${MSYS_DIR}/lib

# copy libpq
cp ${MSYS_DIR}/lib/libpq.dll ./${PLATFORM}_libpq.dll

find . -name '*.dll'
