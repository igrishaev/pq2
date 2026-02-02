#define jString(env, ptr) env->NewStringUTF(ptr)

#define cString(env, jstring) env->GetStringUTFChars(jstring, 0)

#define defclass(var, env, path) \
jclass var = env->FindClass(path); \
if (var == NULL) { \
    env->ThrowNew(env->FindClass("java/lang/Exception"), "Cannot find class: " path); \
}

#define getconn(x) (PGconn*) x

// #define jThrow(env, template, ...) env->ThrowNew(env->FindClass("java/lang/Exception"), template);
