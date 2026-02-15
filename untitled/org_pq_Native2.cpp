#include <jni.h>
#include <iostream>
#include "libpq-fe.h"
#include "org_pq_Native2.h"

char* put_int(char* bb, int value) {
    memcpy(bb, &value, 4);
    return bb += 4;
}

char* put_long(char* bb, long value) {
    memcpy(bb, &value, 8);
    return bb += 8;
}

/*
 * Class:     org_pq_Native2
 * Method:    connect
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_org_pq_Native2_connect
(JNIEnv* env, jclass, jstring jconninfo) {
    const char* conninfo = env->GetStringUTFChars(jconninfo, 0);
    return (long) PQconnectdb(conninfo);
}

/*
 * Class:     org_pq_Native2
 * Method:    closeConnection
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_org_pq_Native2_closeConnection
(JNIEnv *, jclass, jlong jconn) {
    PGconn* conn = (PGconn*) jconn;
    PQfinish(conn);
}

/*
 * Class:     org_pq_Native2
 * Method:    connStatus
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_org_pq_Native2_connStatus
(JNIEnv *, jclass, jlong jconn) {
    PGconn* conn = (PGconn*) jconn;
    return PQstatus(conn);
}

/*
 * Class:     org_pq_Native2
 * Method:    resStatus
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_org_pq_Native2_resStatus
(JNIEnv *, jclass, jlong jresult) {
    PGresult* result = (PGresult*) jresult;
    return PQresultStatus(result);
}

/*
 * Class:     org_pq_Native2
 * Method:    connError
 * Signature: (J)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_pq_Native2_connError
(JNIEnv* env, jclass, jlong jconn) {
    PGconn* conn = (PGconn*) jconn;
    char* ptr = PQerrorMessage(conn);
    return env->NewStringUTF(ptr);
}

/*
 * Class:     org_pq_Native2
 * Method:    initByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;)I
 */
JNIEXPORT jint JNICALL Java_org_pq_Native2_initByteBuffer
(JNIEnv* env, jclass, jobject jbb) {
    void* addr = env->GetDirectBufferAddress(jbb);
    if (addr == NULL) {
        return -1;
    }

    char* bb = (char*) addr;

    bb = put_int(bb, 1);
    bb = put_long(bb, (long) bb);
    bb = put_long(bb, (long) NULL);

    return 0;
}

/*
 * Class:     org_pq_Native2
 * Method:    closeResult
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_org_pq_Native2_closeResult
(JNIEnv *, jclass, jlong jresult) {
    PGresult* result = (PGresult*) jresult;
    PQclear(result);
}

/*
 * Class:     org_pq_Native2
 * Method:    prepare
 * Signature: (JLjava/lang/String;Ljava/lang/String;J)J
 */
JNIEXPORT jlong JNICALL Java_org_pq_Native2_prepare
(JNIEnv* env, jclass, jlong jconn, jstring jname, jstring jquery, jlong jbb) {
    PGconn* conn = (PGconn*) jconn;
    const char* stmtName = env->GetStringUTFChars(jname, NULL);
    const char* query = env->GetStringUTFChars(jquery, NULL);
    return (long) PQprepare(conn, stmtName, query, 0, NULL);

    // result =
    // status = PQresultStatus(result);
    // // TODO: check result
    // result = PQdescribePrepared(conn, stmtName);
    // status = PQresultStatus(result);
    // // TODO: check result
    // // bb = PQ_dump_PGresult(result, bb);
    // return 0;
}

/*
 * Class:     org_pq_Native2
 * Method:    closeStatement
 * Signature: (JLjava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_org_pq_Native2_closeStatement
(JNIEnv* env, jclass, jlong jconn, jstring jstmtName) {
    PGconn* conn = (PGconn*) jconn;
    const char* stmtName = env->GetStringUTFChars(jstmtName, NULL);
    PGresult* result = PQclosePrepared(conn, stmtName);
    // TODO
    // close result
    if (result == NULL) {
        return -1;
    }
    return 0;
}
