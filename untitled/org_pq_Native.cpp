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
    return jPtr(conn);
};

jobjectArray PQ_read_options(JNIEnv* env, PQconninfoOption* opt) {

    jstring keyword, envvar, compiled, val, label, dispchar;
    int dispsize;
    jobject jEl;

    int len = 0;
    while ((opt + len)->keyword != NULL) {
        len++;
    }

    defClass(jPQconninfoOption, env, "org/pq/PQconninfoOption");

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
    PGconn* conn = getConn(jconn);
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
    PGconn* conn = getConn(jconn);
    PQfinish(conn);
};

/*
 * Class:     org_pq_Native
 * Method:    PQreset
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_org_pq_Native_PQreset
(JNIEnv* env, jclass, jlong jconn) {
    PGconn* conn = getConn(jconn);
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

/*
 * Class:     org_pq_Native
 * Method:    PQstatus
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_org_pq_Native_PQstatus
(JNIEnv* env, jclass, jlong jconn) {
    PGconn* conn = getConn(jconn);
    return PQstatus(conn);
};


/*
 * Class:     org_pq_Native
 * Method:    PQtransactionStatus
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_org_pq_Native_PQtransactionStatus
(JNIEnv* env, jclass, jlong jconn) {
    PGconn* conn = getConn(jconn);
    return PQtransactionStatus(conn);
};

/*
 * Class:     org_pq_Native
 * Method:    PQerrorMessage
 * Signature: (J)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_pq_Native_PQerrorMessage
(JNIEnv* env, jclass, jlong jconn) {
    PGconn* conn = getConn(jconn);
    char* ptr = PQerrorMessage(conn);
    return jString(env, ptr);
};

/*
 * Class:     org_pq_Native
 * Method:    PQexec
 * Signature: (JLjava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_org_pq_Native_PQexec
(JNIEnv* env, jclass, jlong jconn, jstring jcommand) {
    PGconn* conn = getConn(jconn);
    const char* command = cString(env, jcommand);

    PGresult* result = PQexecParams(conn,
                                    command,
                                    0,
                                    NULL,
                                    NULL,
                                    NULL,
                                    NULL,
                                    1);

    // PGresult* result = PQexec(conn, command);
    return jPtr(result);
};

/*
 * Class:     org_pq_Native
 * Method:    PQresultStatus
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_org_pq_Native_PQresultStatus
(JNIEnv* env, jclass, jlong jresult) {
    PGresult* result = getResult(jresult);
    return PQresultStatus(result);
};

/*
 * Class:     org_pq_Native
 * Method:    PQresStatus
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_pq_Native_PQresStatus
(JNIEnv* env, jclass, jint jcode) {
    char* message = PQresStatus((ExecStatusType) jcode);
    return jString(env, message);
};

/*
 * Class:     org_pq_Native
 * Method:    PQresultErrorMessage
 * Signature: (J)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_pq_Native_PQresultErrorMessage
(JNIEnv* env, jclass, jlong jresult) {
    PGresult* result = getResult(jresult);
    char* message = PQresultErrorMessage(result);
    return jString(env, message);
};

/*
 * Class:     org_pq_Native
 * Method:    PQclear
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_org_pq_Native_PQclear
(JNIEnv* env, jclass, jlong jresult) {
    PGresult* result = getResult(jresult);
    PQclear(result);
};

/*
 * Class:     org_pq_Native
 * Method:    PQgetvalue
 * Signature: (JII)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_pq_Native_PQgetvalue
(JNIEnv* env, jclass, jlong jresult, jint jrow, jint jcol) {
    PGresult* result = getResult(jresult);
    char* ptr = PQgetvalue(result, jrow, jcol);
    return jString(env, ptr);
};

/*
 * Class:     org_pq_Native
 * Method:    PQgetisnull
 * Signature: (JII)Z
 */
JNIEXPORT jboolean JNICALL Java_org_pq_Native_PQgetisnull
(JNIEnv* env, jclass, jlong jresult, jint jrow, jint jcol) {
    PGresult* result = getResult(jresult);
    return PQgetisnull(result, jrow, jcol);
};

/*
 * Class:     org_pq_Native
 * Method:    PQgetlength
 * Signature: (JII)I
 */
JNIEXPORT jint JNICALL Java_org_pq_Native_PQgetlength
(JNIEnv* env, jclass, jlong jresult, jint jrow, jint jcol) {
    PGresult* result = getResult(jresult);
    return PQgetlength(result, jrow, jcol);
};

/*
 * Class:     org_pq_Native
 * Method:    getValue
 * Signature: (JII)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_org_pq_Native_getValue
(JNIEnv* env, jclass, jlong jresult, jint jrow, jint jcol) {
    PGresult* result = getResult(jresult);
    int isNull = PQgetisnull(result, jrow, jcol);
    if (isNull == 1) {
        return NULL;
    }
    char* val = PQgetvalue(result, jrow, jcol);
    int format = PQfformat(result, jcol);
    Oid oid = PQftype(result, jcol);

    switch ((int) oid) {
    case 21: { // int2
        // short parsed = (short) *val;
        // int parsed = std::stos(val);
        // jclass jClass = env->FindClass("java/lang/Short");
        // jmethodID jMethod = env->GetMethodID(jClass, "<init>", "(S)V");
        // return env->NewObject(jClass, jMethod, parsed);
        return jString(env, val);
    }
    case 23: { // int4
        int parsed = (int) htonl(*((int*) val));
        // int parsed = std::stoi(val);
        std::cout << "parsed: " << parsed;
        jclass jClass = env->FindClass("java/lang/Integer");
        jmethodID jMethod = env->GetMethodID(jClass, "<init>", "(I)V");
        return env->NewObject(jClass, jMethod, parsed);
    }
    case 20: // int8
        return jString(env, val);
    case 25:
        return jString(env, val);
    default:
        return jString(env, val);
    }
};
