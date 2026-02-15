
all: cleanup gen-h compile link

cleanup:
	rm -f org_pq_Native.o libfoo.dylib

compile:
	g++ -c -fPIC \
		-I${JAVA_HOME}/include \
		-I/opt/homebrew/opt/libpq/include \
		org_pq_Native2.cpp -o org_pq_Native2.o

link:
	g++ -dynamiclib -o libfoo2.dylib org_pq_Native2.o -lc -lpq -L/opt/homebrew/opt/libpq/lib


gen-h:
	javac -h . src/main/java/org/pq/Native2.java
# javac -h . src/main/java/org/pq/Native.java src/main/java/org/pq/api/PQconninfoOption.java
# src/main/java/org/pq/Decode.java src/main/java/org/pq/api/OID.java

# export LDFLAGS="-L/opt/homebrew/opt/libpq/lib"
# export CPPFLAGS="-I/opt/homebrew/opt/libpq/include"
# echo 'export PATH="/opt/homebrew/opt/libpq/bin:$PATH"' >> ~/.zshrc
