
all: cleanup headers compile link classes

OBJ = org_pq_Native

classes:
	find . -name '*.class' -delete

cleanup:
	rm -f ${OBJ}.o

FILENAME = $(shell java src/main/java/org/pq/tool/OS.java)

LIBPQ = /opt/homebrew/opt/libpq
# LIBPQ = /usr/local/opt/libpq

# remove trailing slash
JAVA_HOME := $(shell echo $${JAVA_HOME%/})

$(info JAVA_HOME=${JAVA_HOME})
$(info LIBPQ=${LIBPQ})

CPPFLAGS = -I${LIBPQ}/include -I${JAVA_HOME}/include -I${JAVA_HOME}/include/darwin
LDFLAGS = -L${LIBPQ}/lib

$(info LDFLAGS=${LDFLAGS})
$(info CPPFLAGS=${CPPFLAGS})
$(info FILENAME=${FILENAME})

$(info )

compile:
	g++ -c -fPIC ${CPPFLAGS} ${OBJ}.cpp -o ${OBJ}.o

link:
	g++ -dynamiclib -o ${FILENAME}.lib ${OBJ}.o -lc -lpq ${LDFLAGS}

headers:
	javac -h . src/main/java/org/pq/Native.java src/main/java/org/pq/api/PGRES.java

# HOMEBREW_NO_INSTALLED_DEPENDENTS_CHECK=1
