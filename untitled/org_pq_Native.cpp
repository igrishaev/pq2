#include <jni.h>
#include <string.h>
#include "org_pq_Native.h"
#include "libpq-fe.h"
#include <iostream>

JNIEXPORT jstring JNICALL Java_org_pq_Native_foobar
  (JNIEnv* env, jclass self, jlong param) {

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


#define jString(env, ptr) env->NewStringUTF(ptr);

#define getClass(var, env, path) \
jclass var = env->FindClass(path); \
if (var == NULL) { \
    env->ThrowNew(env->FindClass("java/lang/Exception"), "cannot find class"); \
}

#define jThrow(env, template, ...) env->ThrowNew(env->FindClass("java/lang/Exception"), template);

/*
 * Class:     org_pq_Native
 * Method:    PQconndefaults
 * Signature: ()[Lorg/pq/PQconninfoOption;
 */
JNIEXPORT jobjectArray JNICALL Java_org_pq_Native_PQconndefaults
  (JNIEnv* env, jclass) {

  jstring keyword, envvar, compiled, val, label, dispchar;
  int dispsize;
  jobject jEl;

  // PGconn* conn = (PGconn*) jconn;
  PQconninfoOption* opt = PQconndefaults();

  int len = 0;
  while ((opt + len)->keyword != NULL) {
    len++;
  }

  // jThrow(env, "test");


  // jclass jPQconninfoOption = env->FindClass("org/pq/PQconninfoOption");

  // env->FindClass("java/lang/SDFSF");


  // env->ThrowNew(env->FindClass("java/lang/Exception"), "cannot find class");

  getClass(jPQconninfoOption, env, "org/pq/PQconninfoOption");

  // = getClass(env, "org/pq/PQconninfoOption");
  // env->FindClass("org/pq/PQconninfoOption");
//    if (jPQconninfoOption == NULL) {
//      std::cout << "FindClass is null";
//      return NULL;
//    }

jmethodID jInit = env->GetMethodID(jPQconninfoOption, "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I)V");

  if (jInit == NULL) {
                std::cout << "GetMethodID is null";
                return NULL;
            }

  jobjectArray jarr = env->NewObjectArray(len, jPQconninfoOption, NULL);

  for (int i = 0; i < len; i++) {
    keyword = jString(env, (opt + i)->keyword);
    envvar = jString(env, (opt + i)->envvar);
    compiled = jString(env, (opt + i)->compiled);
    val = jString(env, (opt + i)->val);
    label = jString(env, (opt + i)->label);
    dispchar = jString(env, (opt + i)->dispchar);
    dispsize = (opt + i)->dispsize;
    jEl = env->NewObject(jPQconninfoOption, jInit, keyword, envvar, compiled, val, label, dispchar, dispsize);
    env->SetObjectArrayElement(jarr, i, jEl);
  }

  return jarr;
  //    return newObj;

  //    const char *conninfo = env->GetStringUTFChars(jconninfo, 0);
  //    PGconn *conn = PQconnectdb(conninfo);
  // return env->NewDirectByteBuffer((void*) opt, sizeof(PQconninfoOption));


};


/*
 * Class:     org_pq_Native
 * Method:    PQconnectdb
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_org_pq_Native_PQconnectdb
(JNIEnv* env, jclass, jstring jconninfo) {
    const char* conninfo = env->GetStringUTFChars(jconninfo, 0);
    PGconn* conn = PQconnectdb(conninfo);
    return (long) conn;

//    const char *conninfo = env->GetStringUTFChars(jconninfo, 0);
//    PGconn *conn = PQconnectdb(conninfo);
//    return env->NewDirectByteBuffer((void*) conn, sizeof(conn));

//    const char *conninfo = env->GetStringUTFChars(jconninfo, 0);
//    PGconn *conn = PQconnectdb(conninfo);
//    jclass class_pgconn = env->FindClass("org/pq/PGconn");
//    if (class_pgconn == NULL) {
//        std::cout << "FindClass is null";
//    }
//    jmethodID class_init = env->GetMethodID(class_pgconn, "<init>", "(J)V");
//    if (class_init == NULL) {
//            std::cout << "GetMethodID is null";
//        }
//    jobject newObj = env->NewObject(class_pgconn, class_init, (long) conn);
//    return newObj;
};
