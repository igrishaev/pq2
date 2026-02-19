
all: cleanup headers compile link classes

classes:
	find . -name '*.class' -delete

cleanup:
	rm -f org_pq_Native.o libfoo.dylib

# LIBPQ = /opt/homebrew/opt/libpq
LIBPQ = /usr/local/opt/libpq

# remove trailing slash
JAVA_HOME := $(shell echo $${JAVA_HOME%/})

$(info JAVA_HOME=${JAVA_HOME})
$(info LIBPQ=${LIBPQ})

CPPFLAGS = -I${LIBPQ}/include -I${JAVA_HOME}/include -I${JAVA_HOME}/include/darwin
LDFLAGS = -L${LIBPQ}/lib

$(info LDFLAGS=${LDFLAGS})
$(info CPPFLAGS=${CPPFLAGS})

$(info )

compile:
	g++ -c -fPIC ${CPPFLAGS} org_pq_Native.cpp -o org_pq_Native.o

link:
	g++ -dynamiclib -o libfoo.dylib org_pq_Native.o -lc -lpq ${LDFLAGS}

headers:
	javac -h . src/main/java/org/pq/Native.java src/main/java/org/pq/api/PGRES.java

# HOMEBREW_NO_INSTALLED_DEPENDENTS_CHECK=1
