
compile:
	g++ -c -fPIC -I${JAVA_HOME}/include org_pq_Native.cpp -o org_pq_Native.o

link:
	g++ -dynamiclib -o libfoo.dylib org_pq_Native.o -lc
