
all: cleanup headers compile link classes

OBJ = org_pq_Native

classes:
	find . -name '*.class' -delete

cleanup:
	rm -f ${OBJ}.o

# FILENAME = $(shell java src/main/java/org/pq/tool/OS.java)

FILENAME = foobar.lib

# LIBPQ_LIB ?= $(error LIBPQ_LIB is not set)
# LIBPQ_INC ?= $(error LIBPQ_INC is not set)

# remove trailing slash
# JAVA_HOME := $(shell echo $${JAVA_HOME%/})

JAVA_HOME = /c/hostedtoolcache/windows/Java_Temurin-Hotspot_jdk/21.0.9-10.0/aarch64

# CPPFLAGS = -I${LIBPQ_INC} -I${JAVA_HOME}/include -I${JAVA_HOME}/include/darwin -I${JAVA_HOME}/include/win32 -I${JAVA_HOME}/include/linux
# LDFLAGS = -L${LIBPQ_LIB}

CPPFLAGS = -I${JAVA_HOME}/include -I${JAVA_HOME}/include/darwin -I${JAVA_HOME}/include/win32 -I${JAVA_HOME}/include/linux
LDFLAGS =

$(info JAVA_HOME=${JAVA_HOME})
$(info LDFLAGS=${LDFLAGS})
$(info CPPFLAGS=${CPPFLAGS})
$(info FILENAME=${FILENAME})

$(info )

compile:
	g++ -fPIC ${CPPFLAGS} -c ${OBJ}.cpp -o ${OBJ}.o
	g++ -v -shared ${OBJ}.o -lpq ${LDFLAGS} -o ${FILENAME}.lib

link:

headers:
	javac -h . src/main/java/org/pq/Native.java src/main/java/org/pq/api/PGRES.java

# g++ -dynamiclib -o ${FILENAME}.lib ${OBJ}.o -lc -lpq ${LDFLAGS} # -shared
# g++ -c -fPIC ${CPPFLAGS} ${OBJ}.cpp -o ${OBJ}.o
# -shared

# libtool -static -o libfoo.a ${OBJ}.o /opt/homebrew/opt/libpq/lib/libpq.a
# g++ -shared -static -L. -L${LIBPQ_LIB} -lfoo -lpq -o ${FILENAME}.lib
# g++ -shared org_pq_Native.o /opt/homebrew/opt/libpq/lib/libpq.a -o foo.lib -L/opt/homebrew/opt/libpq/lib -I/opt/homebrew/opt/libpq/include
# g++ -shared -Wl,-all_load libfoo.a -o foo.lib -v
# $(shell pg_config --libs)
