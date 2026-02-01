
compile:
	g++ -c -fPIC \
		-I${JAVA_HOME}/include \
		-I/opt/homebrew/Cellar/libpq/18.1/include \
		org_pq_Native.cpp -o org_pq_Native.o

link:
	g++ -dynamiclib -o libfoo.dylib org_pq_Native.o -lc -lpq -L/opt/homebrew/Cellar/libpq/18.1/lib


gen-h:
	javac -h . src/main/java/org/pq/Native.java src/main/java/org/pq/PQconninfoOption.java
