#include <jni.h>

#include <string>

#include "llama_bridge.h"

extern "C" {

JNIEXPORT void JNICALL
Java_com_nexoai_app_Engine_init(JNIEnv* env, jobject /*thiz*/, jstring /*nativeLibDir*/) {
    (void)env;
    LlamaBridge::initialize();
}

JNIEXPORT jint JNICALL
Java_com_nexoai_app_Engine_loadModel(JNIEnv* env, jobject /*thiz*/, jstring modelPath) {
    const char* path = env->GetStringUTFChars(modelPath, nullptr);
    if (path == nullptr) {
        return 1;
    }

    std::string modelPathStr(path);
    env->ReleaseStringUTFChars(modelPath, path);

    return LlamaBridge::loadModel(modelPathStr) ? 0 : 1;
}

JNIEXPORT jint JNICALL
Java_com_nexoai_app_Engine_setSystemPrompt(JNIEnv* env, jobject /*thiz*/, jstring /*prompt*/) {
    (void)env;
    return LlamaBridge::isLoaded() ? 0 : 1;
}

JNIEXPORT jstring JNICALL
Java_com_nexoai_app_Engine_generate(JNIEnv* env, jobject /*thiz*/, jstring input) {
    const char* prompt = env->GetStringUTFChars(input, nullptr);
    if (prompt == nullptr) {
        return env->NewStringUTF("Prompt inválido.");
    }

    std::string promptStr(prompt);
    env->ReleaseStringUTFChars(input, prompt);

    std::string response = LlamaBridge::generate(promptStr);
    return env->NewStringUTF(response.c_str());
}

JNIEXPORT void JNICALL
Java_com_nexoai_app_Engine_releaseModel(JNIEnv* env, jobject /*thiz*/) {
    (void)env;
    LlamaBridge::releaseModel();
}

JNIEXPORT void JNICALL
Java_com_nexoai_app_Engine_shutdown(JNIEnv* env, jobject /*thiz*/) {
    (void)env;
    LlamaBridge::releaseModel();
}

}  // extern "C"
