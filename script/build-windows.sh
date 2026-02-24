#!/bin/bash

set -euo pipefail
set -x

OBJ=org_pq_Native

# remove traing slash
JAVA_HOME=$(echo ${JAVA_HOME%/})

JAVA_INC="-I${JAVA_HOME}/include -I${JAVA_HOME}/include/win32"

PLATFORM=$(cat PLATFORM)

g++ -fPIC ${JAVA_INC} -c ${OBJ}.cpp -o ${OBJ}.o
g++ -shared ${OBJ}.o -lpq -o ${PLATFORM}_api.dll

# compile static libpq as shared
# g++ -shared ${MSYS_DIR}/lib/libpq.dll.a -o ${PLATFORM}_libpq.dll

g++ -shared -lpq -o ${PLATFORM}_libpq.dll

# ls -la ${MSYS_DIR}/lib

find . -name '*.dll'
