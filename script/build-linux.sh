#!/bin/bash

set -euo pipefail
set -x

OBJ=org_pq_Native

# remove traing slash
JAVA_HOME=$(echo ${JAVA_HOME%/})

JAVA_INC="-I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux"

PLATFORM=$(cat PLATFORM)

g++ -fPIC -L$(pg_config --libdir) -I$(pg_config --includedir) ${JAVA_INC} -c ${OBJ}.cpp -o ${OBJ}.o
g++ -shared ${OBJ}.o -lpq -o ${PLATFORM}.so

ls -l . | grep *.so
