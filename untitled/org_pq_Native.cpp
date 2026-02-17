#include <jni.h>
#include <iostream>
#include "libpq-fe.h"
#include "org_pq_Native.h"

char* put_int(char* bb, int value) {
    memcpy(bb, &value, 4);
    return bb += 4;
}

char* put_long(char* bb, long value) {
    memcpy(bb, &value, 8);
    return bb += 8;
}

char* put_string(char* bb, char* string) {
    char* pos = stpcpy(bb + 4, string);
    int len = pos - bb - 4;
    put_int(bb, len);
    memcpy(bb, &len, 4);
    return pos;
}

int get_int(char* bb, int& off) {
    int i = *((int*) (bb + off));
    off += 4;
    return i;
}

/*
 * Class:     org_pq_Native
 * Method:    connect
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_org_pq_Native_connect
(JNIEnv* env, jclass, jstring jconninfo) {
    const char* conninfo = env->GetStringUTFChars(jconninfo, 0);
    return (long) PQconnectdb(conninfo);
}

/*
 * Class:     org_pq_Native
 * Method:    closeConnection
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_org_pq_Native_closeConnection
(JNIEnv *, jclass, jlong jconn) {
    PGconn* conn = (PGconn*) jconn;
    PQfinish(conn);
}

/*
 * Class:     org_pq_Native
 * Method:    connStatus
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_org_pq_Native_connStatus
(JNIEnv *, jclass, jlong jconn) {
    PGconn* conn = (PGconn*) jconn;
    return PQstatus(conn);
}

/*
 * Class:     org_pq_Native
 * Method:    resStatus
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_org_pq_Native_resStatus
(JNIEnv *, jclass, jlong jresult) {
    PGresult* result = (PGresult*) jresult;
    return PQresultStatus(result);
}

/*
 * Class:     org_pq_Native
 * Method:    connError
 * Signature: (J)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_pq_Native_connError
(JNIEnv* env, jclass, jlong jconn) {
    PGconn* conn = (PGconn*) jconn;
    char* ptr = PQerrorMessage(conn);
    return env->NewStringUTF(ptr);
}

/*
 * Class:     org_pq_Native
 * Method:    initByteBuffer
 * Signature: (Ljava/nio/ByteBuffer;)I
 */
JNIEXPORT jint JNICALL Java_org_pq_Native_initByteBuffer
(JNIEnv* env, jclass, jobject jbb) {
    void* addr = env->GetDirectBufferAddress(jbb);
    if (addr == NULL) {
        return -1;
    }

    char* bb = (char*) addr;

    bb = put_int(bb, 1);
    bb = put_long(bb, (long) addr);
    bb = put_long(bb, (long) NULL);

    return 0;
}

/*
 * Class:     org_pq_Native
 * Method:    closeResult
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_org_pq_Native_closeResult
(JNIEnv *, jclass, jlong jresult) {
    PGresult* result = (PGresult*) jresult;
    PQclear(result);
}

/*
 * Class:     org_pq_Native
 * Method:    prepare
 * Signature: (JLjava/lang/String;Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_org_pq_Native_prepare
(JNIEnv* env, jclass, jlong jconn, jstring jname, jstring jquery) {
    PGconn* conn = (PGconn*) jconn;
    const char* stmtName = env->GetStringUTFChars(jname, NULL);
    const char* query = env->GetStringUTFChars(jquery, NULL);
    return (long) PQprepare(conn, stmtName, query, 0, NULL);
}

/*
 * Class:     org_pq_Native
 * Method:    describe
 * Signature: (JLjava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_org_pq_Native_describe
(JNIEnv* env, jclass, jlong jconn, jstring jname) {
    PGconn* conn = (PGconn*) jconn;
    const char* stmtName = env->GetStringUTFChars(jname, NULL);
    return (long) PQdescribePrepared(conn, stmtName);
}

/*
 * Class:     org_pq_Native
 * Method:    closeStatement
 * Signature: (JLjava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_org_pq_Native_closeStatement
(JNIEnv* env, jclass, jlong jconn, jstring jstmtName) {
    PGconn* conn = (PGconn*) jconn;
    const char* stmtName = env->GetStringUTFChars(jstmtName, NULL);
    return (long) PQclosePrepared(conn, stmtName);
}

/*
 * Class:     org_pq_Native
 * Method:    execPrepared
 * Signature: (JLjava/lang/String;J)J
 */
JNIEXPORT jlong JNICALL Java_org_pq_Native_execPrepared
(JNIEnv* env, jclass, jlong jconn, jstring jstmt, jlong jbb) {
    PGconn* conn = (PGconn*) jconn;
    char* bb = (char*) jbb;

    const char* stmtName = env->GetStringUTFChars(jstmt, NULL);

    int off = 0;

    // int nParams = get_int(bb, off);
    // int* int_ptr = reinterpret_cast<int*>(bb);

    int32_t nParams = *((int32_t*) (bb + off));
    off += sizeof(int32_t);

    // printf("nParams: %d, off: %d \n", nParams, off);

    // Oid* paramTypes = (Oid*) (bb + off);
    // off += sizeof(Oid) * nParams;

    char** paramValues = (char**) (bb + off);
    off += sizeof(char*) * nParams;

    int32_t* paramLengths = (int32_t*) (bb + off);
    off += sizeof(int32_t) * nParams;;

    int32_t* paramFormats = (int32_t*) (bb + off);
    off += sizeof(int32_t) * nParams;;

    int32_t resultFormat = *((int32_t*) (bb + off));
    off += sizeof(int32_t);

    // Oid* oid;
    // for (int i = 0; i < nParams; i++) {
    //     oid = paramTypes + i;
    //     printf("oid: %d \n", *oid);
    // }

    char* ptr;
    int val;
    for (int i = 0; i < nParams; i++) {
        ptr = paramValues[i];
        val = *((int*) ptr);
        // printf("val: %d \n", htonl(val));
    }

    int* len;
    for (int i = 0; i < nParams; i++) {
        len = paramLengths + i;
        // printf("len: %d \n", *len);
    }

    int* fmt;
    for (int i = 0; i < nParams; i++) {
        fmt = paramFormats + i;
        // printf("format: %d \n", *fmt);
    }

    // printf("resultFormat: %d \n", resultFormat);

    return (long) PQexecPrepared(conn,
                                 stmtName,
                                 nParams,
                                 paramValues,
                                 paramLengths,
                                 paramFormats,
                                 resultFormat);
}

/*
 * Class:     org_pq_Native
 * Method:    fieldValue
 * Signature: (JIIJ)V
 */
JNIEXPORT void JNICALL Java_org_pq_Native_fieldValue
  (JNIEnv *, jclass, jlong jresult, jint row, jint col, jlong jbb) {
    PGresult* result = (PGresult*) jresult;
    char* bb = (char*) jbb;
    char* value = PQgetvalue(result, row, col);
    int len = PQgetlength(result, row, col);
    memcpy(bb, value, len);
}

JNIEXPORT jboolean JNICALL Java_org_pq_Native_fieldIsNull
(JNIEnv *, jclass, jlong jresult, jint row, jint col) {
    PGresult* result = (PGresult*) jresult;
    return PQgetisnull(result, row, col);
}

/*
 * Class:     org_pq_Native
 * Method:    fieldOid
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL Java_org_pq_Native_fieldOid
(JNIEnv *, jclass, jlong jresult, jint col) {
    PGresult* result = (PGresult*) jresult;
    return PQftype(result, col);
}

/*
 * Class:     org_pq_Native
 * Method:    fieldFormat
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL Java_org_pq_Native_fieldFormat
(JNIEnv *, jclass, jlong jresult, jint col) {
    PGresult* result = (PGresult*) jresult;
    return PQfformat(result, col);
}

/*
 * Class:     org_pq_Native
 * Method:    fieldLength
 * Signature: (JII)I
 */
JNIEXPORT jint JNICALL Java_org_pq_Native_fieldLength
(JNIEnv *, jclass, jlong jresult, jint row, jint col) {
    PGresult* result = (PGresult*) jresult;
    return PQgetlength(result, row, col);
}

/*
 * Class:     org_pq_Native
 * Method:    nTuples
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_org_pq_Native_nTuples
(JNIEnv *, jclass, jlong jresult) {
    PGresult* result = (PGresult*) jresult;
    return PQntuples(result);
}

/*
 * Class:     org_pq_Native
 * Method:    query
 * Signature: (JLjava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_org_pq_Native_query
(JNIEnv* env, jclass, jlong jconn, jstring jquery) {
    PGconn* conn = (PGconn*) jconn;
    const char* query = env->GetStringUTFChars(jquery, NULL);
    return (long) PQexec(conn, query);
}

/*
 * Class:     org_pq_Native
 * Method:    sendQuery
 * Signature: (JLjava/lang/String;I)I
 */
JNIEXPORT int JNICALL Java_org_pq_Native_sendQuery
(JNIEnv* env, jclass, jlong jconn, jstring jquery) {
    PGconn* conn = (PGconn*) jconn;
    const char* query = env->GetStringUTFChars(jquery, NULL);
    return (int) PQsendQuery(conn, query);
}

/*
 * Class:     org_pq_Native
 * Method:    setChunkedRowsMode
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL Java_org_pq_Native_setChunkedRowsMode
(JNIEnv *, jclass, jlong jconn, jint size) {
    PGconn* conn = (PGconn*) jconn;
    return PQsetChunkedRowsMode(conn, size);
}

/*
 * Class:     org_pq_Native
 * Method:    nColumns
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_org_pq_Native_nColumns
(JNIEnv *, jclass, jlong jresult) {
    PGresult* result = (PGresult*) jresult;
    return PQnfields(result);
}

/*
 * Class:     org_pq_Native
 * Method:    nParams
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_org_pq_Native_nParams
(JNIEnv *, jclass, jlong jresult) {
    PGresult* result = (PGresult*) jresult;
    return PQnparams(result);
}

/*
 * Class:     org_pq_Native
 * Method:    paramOid
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL Java_org_pq_Native_paramOid
(JNIEnv *, jclass, jlong jresult, jint i) {
    PGresult* result = (PGresult*) jresult;
    return PQparamtype(result, i);
}

/*
 * Class:     org_pq_Native
 * Method:    getResult
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_org_pq_Native_getResult
(JNIEnv *, jclass, jlong jconn) {
    PGconn* conn = (PGconn*) jconn;
    return (long) PQgetResult(conn);
}
