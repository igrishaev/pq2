
all: cleanup platform headers compile

OBJ = org_pq_Native

cleanup:
	rm -f ${OBJ}.o

# remove trailing slash
JAVA_HOME := $(shell echo $${JAVA_HOME%/})
JAVA_INC = -I${JAVA_HOME}/include -I${JAVA_HOME}/include/darwin -I${JAVA_HOME}/include/win32 -I${JAVA_HOME}/include/linux

FILENAME ?= $(error FILENAME is not set)

PLATFORM_FILE = _PLATFORM

platform:
	java src/main/java/org/pq/tool/OS.java > ${PLATFORM_FILE}

compile: platform
	g++ -fPIC -I$(shell pg_config --includedir) ${JAVA_INC} -c ${OBJ}.cpp -o ${OBJ}.o
	g++ -shared ${OBJ}.o -lpq -L$(shell pg_config --libdir) -o src/main/resources/bin/$(shell cat ${PLATFORM_FILE})_api.dylib
	cp $(shell pg_config --libdir)/libpq.dylib src/main/resources/bin/$(shell cat ${PLATFORM_FILE})_libpq.dylib
	mvn compile

maven-sync:
	rm -rf target
	mvn resources:resources
	mvn compile

ci-check: maven-sync
	rm -rf target/classes/bin/*.*            # drop existing binaries
	cp *.dylib target/classes/bin/ | true    # copy the new ones (mac)
	cp *.so    target/classes/bin/ | true    # copy the new ones (linux)
	cp *.dll   target/classes/bin/ | true    # copy the new ones (windows)
	java -cp target/classes org.pq.CICheck   # run the check (with new libs)

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
