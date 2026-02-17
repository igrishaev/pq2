
all: cleanup headers compile link classes

classes:
	find . -name '*.class' -delete

cleanup:
	rm -f org_pq_Native.o libfoo.dylib

compile:
	g++ -c -fPIC \
		-I${JAVA_HOME}/include \
		-I/opt/homebrew/opt/libpq/include \
		org_pq_Native.cpp -o org_pq_Native.o

link:
	g++ -dynamiclib -o libfoo.dylib org_pq_Native.o -lc -lpq -L/opt/homebrew/opt/libpq/lib


headers:
	javac -h . src/main/java/org/pq/Native.java

# javac -h . src/main/java/org/pq/Native.java src/main/java/org/pq/api/PQconninfoOption.java
# src/main/java/org/pq/Decode.java src/main/java/org/pq/api/OID.java

# export LDFLAGS="-L/opt/homebrew/opt/libpq/lib"
# export CPPFLAGS="-I/opt/homebrew/opt/libpq/include"
# echo 'export PATH="/opt/homebrew/opt/libpq/bin:$PATH"' >> ~/.zshrc
