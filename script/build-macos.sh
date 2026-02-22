#!/bin/bash

set -euo pipefail
set -x

OBJ=org_pq_Native

# remove traing slash
JAVA_HOME=$(echo ${JAVA_HOME%/})

JAVA_INC="-I${JAVA_HOME}/include -I${JAVA_HOME}/include/darwin"

PLATFORM=$(cat PLATFORM)

g++ -fPIC -I${LIBPQ}/include ${JAVA_INC} -c ${OBJ}.cpp -o ${OBJ}.o
g++ -shared ${OBJ}.o -lpq -L${LIBPQ}/lib -o ${PLATFORM}_api.dylib

ls -l ${LIBPQ}/lib

# copy libpq
cp ${LIBPQ}/lib/libpq.dylib ./${PLATFORM}_libpq.dylib
