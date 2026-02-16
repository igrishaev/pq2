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

char* PQ_dump_PGresult(PGresult* result, char* bb) {

    // self
    bb = put_long(bb, (long) result);

    int nTuples = PQntuples(result);
    bb = put_int(bb, nTuples);

    // n of columns
    int nColumns = PQnfields(result);
    bb = put_int(bb, nColumns);

    // columns
    char* column;
    Oid tableOid;
    int format;
    Oid oid;
    int typeMod;
    for (int i = 0; i < nColumns; i++) {

        oid = PQftype(result, i);
        bb = put_int(bb, oid);

        format = PQfformat(result, i);
        bb = put_int(bb, format);

        tableOid = PQftable(result, i);
        bb = put_int(bb, tableOid);

        typeMod = PQfmod(result, i);
        bb = put_int(bb, typeMod);

        column = PQfname(result, i);
        bb = put_string(bb, column);
    }

    // params
    int nParams = PQnparams(result);
    bb = put_int(bb, nParams);
    for (int i = 0; i < nParams; i++) {
        oid = PQparamtype(result, i);
        bb = put_int(bb, oid);
    }

    return bb;
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
    bb = put_long(bb, (long) addr);
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
 * Signature: (JLjava/lang/String;Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_org_pq_Native2_prepare
(JNIEnv* env, jclass, jlong jconn, jstring jname, jstring jquery) {
    PGconn* conn = (PGconn*) jconn;
    const char* stmtName = env->GetStringUTFChars(jname, NULL);
    const char* query = env->GetStringUTFChars(jquery, NULL);
    return (long) PQprepare(conn, stmtName, query, 0, NULL);
}

/*
 * Class:     org_pq_Native2
 * Method:    describe
 * Signature: (JLjava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_org_pq_Native2_describe
(JNIEnv* env, jclass, jlong jconn, jstring jname) {
    PGconn* conn = (PGconn*) jconn;
    const char* stmtName = env->GetStringUTFChars(jname, NULL);
    return (long) PQdescribePrepared(conn, stmtName);
}

/*
 * Class:     org_pq_Native2
 * Method:    closeStatement
 * Signature: (JLjava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_org_pq_Native2_closeStatement
(JNIEnv* env, jclass, jlong jconn, jstring jstmtName) {
    PGconn* conn = (PGconn*) jconn;
    const char* stmtName = env->GetStringUTFChars(jstmtName, NULL);
    return (long) PQclosePrepared(conn, stmtName);
}



/*
 * Class:     org_pq_Native2
 * Method:    serializePrepared
 * Signature: (JJ)I
 */
JNIEXPORT jint JNICALL Java_org_pq_Native2_serializePrepared
  (JNIEnv *, jclass, jlong jresult, jlong jbb) {
    PGresult* result = (PGresult*) jresult;
    char* bb = (char*) jbb;
    PQ_dump_PGresult(result, bb);
    return 0;
}

/*
 * Class:     org_pq_Native2
 * Method:    execPrepared
 * Signature: (JLjava/lang/String;J)J
 */
JNIEXPORT jlong JNICALL Java_org_pq_Native2_execPrepared
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
 * Class:     org_pq_Native2
 * Method:    resultInfo
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_org_pq_Native2_resultInfo
(JNIEnv *, jclass, jlong jresult, jlong jbb) {

    PGresult* result = (PGresult*) jresult;
    char* bb = (char*) jbb;

    int nTuples = PQntuples(result);
    char* cmdStatus = PQcmdStatus(result);
    char* cmdTuples = PQcmdTuples(result);
    Oid oid = PQoidValue(result);

    bb = put_int(bb, nTuples);
    bb = put_int(bb, oid);
    bb = put_string(bb, cmdStatus);
    bb = put_string(bb, cmdTuples);
}

/*
 * Class:     org_pq_Native2
 * Method:    fieldValue
 * Signature: (JIIJ)V
 */
JNIEXPORT void JNICALL Java_org_pq_Native2_fieldValue
  (JNIEnv *, jclass, jlong jresult, jint row, jint col, jlong jbb) {
    PGresult* result = (PGresult*) jresult;
    char* bb = (char*) jbb;
    char* value = PQgetvalue(result, row, col);
    int len = PQgetlength(result, row, col);
    memcpy(bb, value, len);
}

JNIEXPORT jboolean JNICALL Java_org_pq_Native2_fieldIsNull
(JNIEnv *, jclass, jlong jresult, jint row, jint col) {
    PGresult* result = (PGresult*) jresult;
    return PQgetisnull(result, row, col);
}

/*
 * Class:     org_pq_Native2
 * Method:    fieldOid
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL Java_org_pq_Native2_fieldOid
(JNIEnv *, jclass, jlong jresult, jint col) {
    PGresult* result = (PGresult*) jresult;
    return PQftype(result, col);
}

/*
 * Class:     org_pq_Native2
 * Method:    fieldFormat
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL Java_org_pq_Native2_fieldFormat
(JNIEnv *, jclass, jlong jresult, jint col) {
    PGresult* result = (PGresult*) jresult;
    return PQfformat(result, col);
}

/*
 * Class:     org_pq_Native2
 * Method:    fieldLength
 * Signature: (JII)I
 */
JNIEXPORT jint JNICALL Java_org_pq_Native2_fieldLength
(JNIEnv *, jclass, jlong jresult, jint row, jint col) {
    PGresult* result = (PGresult*) jresult;
    return PQgetlength(result, row, col);
}

/*
 * Class:     org_pq_Native2
 * Method:    nTuples
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_org_pq_Native2_nTuples
(JNIEnv *, jclass, jlong jresult) {
    PGresult* result = (PGresult*) jresult;
    return PQntuples(result);
}
