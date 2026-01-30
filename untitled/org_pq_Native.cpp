#include <jni.h>
#include <string.h>
#include "org_pq_Native.h"
#include "libpq-fe.h"
#include <iostream>

JNIEXPORT jstring JNICALL Java_org_pq_Native_foobar
  (JNIEnv* env, jclass self, jint param) {

  const char* c_string = "Hello from C/C++ native code!";

  // Create a new Java string object from a null-terminated C string (UTF-8 format)
  jstring java_string = env->NewStringUTF(c_string);

  std::cout << "const integer is: " << CONNECTION_BAD ;

  // Check if the string creation was successful (important for error handling)
  if (java_string == NULL) {
      return NULL; // Return NULL if an error occurred (e.g., out of memory)
  }

  // Return the new jstring object to the Java environment
  return java_string;
};

JNIEXPORT jlong JNICALL Java_org_pq_Native_connect (JNIEnv *env, jclass jthis, jstring jconn_info) {
    const char *conn_info = env->GetStringUTFChars(jconn_info, 0);
    PGconn *conn = PQconnectdb(conn_info);
    return (long) conn;
};
