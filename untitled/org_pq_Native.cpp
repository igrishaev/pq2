#include <jni.h>
#include <iostream>
#include "libpq-fe.h"
#include "org_pq_Native.h"
#include "macro.h"

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
};

jobjectArray PQ_read_options(JNIEnv* env, PQconninfoOption* opt) {

    jstring keyword, envvar, compiled, val, label, dispchar;
    int dispsize;
    jobject jEl;

    int len = 0;
    while ((opt + len)->keyword != NULL) {
        len++;
    }

    defclass(jPQconninfoOption, env, "org/pq/PQconninfoOption");

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

}

/*
 * Class:     org_pq_Native
 * Method:    PQconndefaults
 * Signature: ()[Lorg/pq/PQconninfoOption;
 */
JNIEXPORT jobjectArray JNICALL Java_org_pq_Native_PQconndefaults
(JNIEnv* env, jclass) {
    PQconninfoOption* opt = PQconndefaults();
    return PQ_read_options(env, opt);
};

/*
 * Class:     org_pq_Native
 * Method:    PQconninfo
 * Signature: (J)[Lorg/pq/PQconninfoOption;
 */
JNIEXPORT jobjectArray JNICALL Java_org_pq_Native_PQconninfo
(JNIEnv* env, jclass, jlong jconn) {
    PGconn* conn = getconn(jconn);
    PQconninfoOption* opt = PQconninfo(conn);
    return PQ_read_options(env, opt);
};

/*
 * Class:     org_pq_Native
 * Method:    PQfinish
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_org_pq_Native_PQfinish
(JNIEnv* env, jclass, jlong jconn) {
    PGconn* conn = getconn(jconn);
    PQfinish(conn);
};

/*
 * Class:     org_pq_Native
 * Method:    PQreset
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_org_pq_Native_PQreset
(JNIEnv* env, jclass, jlong jconn) {
    PGconn* conn = getconn(jconn);
    PQreset(conn);
};

/*
 * Class:     org_pq_Native
 * Method:    PQping
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jint JNICALL Java_org_pq_Native_PQping
(JNIEnv* env, jclass, jstring jconninfo) {
    const char* conninfo = cString(env, jconninfo);
    return PQping(conninfo);
};
