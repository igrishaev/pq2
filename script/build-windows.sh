#!/bin/bash

set -euo pipefail
set -x

OBJ=org_pq_Native

# remove traing slash
JAVA_HOME=$(echo ${JAVA_HOME%/})

JAVA_INC="-I${JAVA_HOME}/include -I${JAVA_HOME}/include/win32"

PLATFORM=$(cat PLATFORM)

# -march=x86-64

ls -la ${MSYS_DIR}/bin
# ls -la ${MSYS_DIR}/lib
# ls -la ${MSYS_DIR}/include

# -march=native

g++ -fPIC ${JAVA_INC} -c ${OBJ}.cpp -o ${OBJ}.o -L${MSYS_DIR}/lib
g++ -shared ${OBJ}.o -L${MSYS_DIR}/lib -o ${PLATFORM}_api.dll

# compile static libpq as shared
# g++ -shared ${MSYS_DIR}/lib/libpq.dll.a -o ${PLATFORM}_libpq.dll
# g++ -shared -lpq -o ${PLATFORM}_libpq.dll -march=native

# g++ -shared ${MSYS_DIR}/lib/libpq.a -o ${PLATFORM}_libpq.dll -march=native

cp ${MSYS_DIR}/bin/libpq.dll ${PLATFORM}_libpq.dll
# g++ -shared ${MSYS_DIR}/lib/libpq.a -o ${PLATFORM}_libpq.dll -march=native

# ls -la ${MSYS_DIR}/lib

find . -name '*.dll'
