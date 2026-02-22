#!/bin/bash

set -euo pipefail
set -x

OBJ=org_pq_Native

# remove traing slash
JAVA_HOME=$(echo ${JAVA_HOME%/})

JAVA_INC="-I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux"

PLATFORM=$(cat PLATFORM)

g++ -fPIC -I$(pg_config --includedir) ${JAVA_INC} -c ${OBJ}.cpp -o ${OBJ}.o
g++ -shared ${OBJ}.o -lpq -o ${PLATFORM}_api.so

# copy libpq
cp $(pg_config --libdir)/libpq.so ./${PLATFORM}_libpq.so

find . -name '*.so'
