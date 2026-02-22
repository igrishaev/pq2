
all: cleanup dump-platform headers compile

OBJ = org_pq_Native

cleanup:
	rm -f ${OBJ}.o

# remove trailing slash
JAVA_HOME := $(shell echo $${JAVA_HOME%/})
JAVA_INC = -I${JAVA_HOME}/include -I${JAVA_HOME}/include/darwin -I${JAVA_HOME}/include/win32 -I${JAVA_HOME}/include/linux

FILENAME ?= $(error FILENAME is not set)

PLATFORM = _PLATFORM

build-windows:
	echo ${foo}

dump-platform:
	java src/main/java/org/pq/tool/OS.java > ${PLATFORM}

filename:
	@java src/main/java/org/pq/tool/OS.java

compile: ${PLATFORM}
	rm -rf src/main/resources/*.lib
	g++ -fPIC -I$(shell pg_config --includedir) ${JAVA_INC} -c ${OBJ}.cpp -o ${OBJ}.o
	g++ -shared ${OBJ}.o -lpq -L$(shell pg_config --libdir) -o src/main/resources/$(shell cat ${PLATFORM})_api.lib
	cp $(shell pg_config --libdir)/libpq.dylib src/main/resources/$(shell cat ${PLATFORM})_libpq.lib
	mvn compile


headers:
	javac -h . src/main/java/org/pq/Native.java src/main/java/org/pq/tool/OS.java

# g++ -dynamiclib -o ${FILENAME}.lib ${OBJ}.o -lc -lpq ${LDFLAGS} # -shared
# g++ -c -fPIC ${CPPFLAGS} ${OBJ}.cpp -o ${OBJ}.o
# -shared

# libtool -static -o libfoo.a ${OBJ}.o /opt/homebrew/opt/libpq/lib/libpq.a
# g++ -shared -static -L. -L${LIBPQ_LIB} -lfoo -lpq -o ${FILENAME}.lib
# g++ -shared org_pq_Native.o /opt/homebrew/opt/libpq/lib/libpq.a -o foo.lib -L/opt/homebrew/opt/libpq/lib -I/opt/homebrew/opt/libpq/include
# g++ -shared -Wl,-all_load libfoo.a -o foo.lib -v
# $(shell pg_config --libs)

# CPPFLAGS = -I${LIBPQ_INC}
# # LDFLAGS =

# LIBPQ_LIB ?= $(error LIBPQ_LIB is not set)
# LIBPQ_INC ?= $(error LIBPQ_INC is not set)

# make compile FILENAME=$(make filename) CPPFLAGS=-I$(pg_config --includedir) LDFLAGS=-L$(pg_config --libdir)
