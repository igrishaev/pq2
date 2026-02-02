#include <jni.h>
#include <iostream>
#include "libpq-fe.h"
#include "org_pq_Native.h"
#include "macro.h"
#include "oid.h"

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

    defClass(PQconninfoOption, env, "org/pq/PQconninfoOption");
    defMethod(Init, env, PQconninfoOption, "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;I)V");

    jobjectArray jarr = env->NewObjectArray(len, PQconninfoOption, NULL);

    for (int i = 0; i < len; i++) {
        keyword = jString(env, (opt + i)->keyword);
        envvar = jString(env, (opt + i)->envvar);
        compiled = jString(env, (opt + i)->compiled);
        val = jString(env, (opt + i)->val);
        label = jString(env, (opt + i)->label);
        dispchar = jString(env, (opt + i)->dispchar);
        dispsize = (opt + i)->dispsize;
        jEl = env->NewObject(PQconninfoOption, Init, keyword, envvar, compiled, val, label, dispchar, dispsize);
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
                                    0);

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
 * Method:    PQntuples
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_org_pq_Native_PQntuples
    (JNIEnv* env, jclass, jlong jresult) {
    PGresult* result = getResult(jresult);
    return  PQntuples(result);
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

short PQparseInt2(char* val, int format) {
    if (format == 0) {
        return std::stoi(val);
    } else {
        return ntohs(*((short*) val));
    }
}

int PQparseInt4(char* val, int format) {
    if (format == 0) {
        return std::stoi(val);
    } else {
        return ntohl(*((int*) val));
    }
}

long PQparseInt8(char* val, int format) {
    if (format == 0) {
        return std::stol(val);
    } else {
        return ntohll(*((long*) val));
    }
}

jobject PQJavaUUID(JNIEnv* env, char* raw, int format) {

    defClass(jUUID, env, "java/util/UUID");
    defStaticMethod(fromString, env, jUUID, "fromString", "(Ljava/lang/String;)Ljava/util/UUID;");
    defMethod(jInit, env, jUUID, "<init>", "(JJ)V");

    jstring payload;
    long bits_low, bits_hi;

    if (format == 0) {
        payload = jString(env, raw);
        return env->CallStaticObjectMethod(jUUID, fromString, payload);
    } else {
        bits_hi = ntohll(*((long*) raw));
        bits_low = ntohll(*((long*) (raw + 8)));
        return env->NewObject(jUUID, jInit, bits_hi, bits_low);
        return NULL;
    }
}

jobject PQJavaShort(JNIEnv* env, short val) {
    defClass(jClass, env, "java/lang/Short");
    defMethod(jInit, env, jClass, "<init>", "(S)V");
    return env->NewObject(jClass, jInit, val);
}

jobject PQJavaInteger(JNIEnv* env, int val) {
    defClass(jClass, env, "java/lang/Integer");
    defMethod(jInit, env, jClass, "<init>", "(I)V");
    return env->NewObject(jClass, jInit, val);
}

jobject PQJavaLong(JNIEnv* env, long val) {
    defClass(jClass, env, "java/lang/Long");
    defMethod(jInit, env, jClass, "<init>", "(J)V");
    return env->NewObject(jClass, jInit, val);
}


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
    case OID_INT2: {
        short parsed = PQparseInt2(val, format);
        return PQJavaShort(env, parsed);
    }
    case OID_INT4: {
        int parsed = PQparseInt4(val, format);
        return PQJavaInteger(env, parsed);
    }
    case OID_INT8: {
        long parsed = PQparseInt8(val, format);
        return PQJavaLong(env, parsed);
    }
    case OID_UUID: {
        return PQJavaUUID(env, val, format);
    }
    case OID_TEXT: {
        return jString(env, val);
    }
    default: {
        return jString(env, val);
    }}
};
