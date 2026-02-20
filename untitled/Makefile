
all: cleanup headers compile link classes

OBJ = org_pq_Native

classes:
	find . -name '*.class' -delete

cleanup:
	rm -f ${OBJ}.o

FILENAME = $(shell java src/main/java/org/pq/tool/OS.java)

LIBPQ_LIB ?= $(error LIBPQ_LIB is not set)
LIBPQ_INC ?= $(error LIBPQ_INC is not set)

# remove trailing slash
JAVA_HOME := $(shell echo $${JAVA_HOME%/})

CPPFLAGS = -I${LIBPQ_INC} -I${JAVA_HOME}/include -I${JAVA_HOME}/include/darwin -I${JAVA_HOME}/include/win32 -I${JAVA_HOME}/include/linux
LDFLAGS = -L${LIBPQ_LIB}

$(info JAVA_HOME=${JAVA_HOME})
$(info LIBPQ=${LIBPQ})
$(info LDFLAGS=${LDFLAGS})
$(info CPPFLAGS=${CPPFLAGS})
$(info FILENAME=${FILENAME})

$(info )

compile:
	g++ ${CPPFLAGS} -c ${OBJ}.cpp -o ${OBJ}.o

link:
	g++ -shared -o ${FILENAME}.lib ${OBJ}.o -lpq ${LDFLAGS}

headers:
	javac -h . src/main/java/org/pq/Native.java src/main/java/org/pq/api/PGRES.java

# g++ -dynamiclib -o ${FILENAME}.lib ${OBJ}.o -lc -lpq ${LDFLAGS} # -shared
# g++ -c -fPIC ${CPPFLAGS} ${OBJ}.cpp -o ${OBJ}.o
