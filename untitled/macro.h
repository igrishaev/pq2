#define jString(env, ptr) env->NewStringUTF(ptr)

#define cString(env, jstring) env->GetStringUTFChars(jstring, 0)

#define defClass(var, env, path) \
jclass var = env->FindClass(path); \
if (var == NULL) { \
    env->ThrowNew(env->FindClass("java/lang/Exception"), "Cannot find class: " path); \
}

#define defMethod(var, env, class, meth, sig) \
jmethodID var = env->GetMethodID(class, meth, sig); \
if (var == NULL) { \
    env->ThrowNew(env->FindClass("java/lang/Exception"), "Cannot find method: " meth ", signature: " sig); \
}

#define defStaticMethod(var, env, cls, meth, sig) \
jmethodID var = env->GetStaticMethodID(cls, meth, sig); \
if (var == NULL) { \
    env->ThrowNew(env->FindClass("java/lang/Exception"), "Cannot find static method: " meth ", signature: " sig); \
}

#define jPtr(x) (jlong) x

#define getConn(x) (PGconn*) x

#define getResult(x) (PGresult*) x

// #define jThrow(env, template, ...) env->ThrowNew(env->FindClass("java/lang/Exception"), template);
