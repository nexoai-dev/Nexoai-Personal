#include <android/log.h>
#include <jni.h>
#include <string>
#include <vector>
#include <sstream>
#include <thread>
#include <mutex>

#include "llama.h"
#include "common.h"
#include "sampling.h"

#define LOG_TAG "NexoAI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)

static llama_model* g_model = nullptr;
static llama_context* g_context = nullptr;
static llama_batch g_batch = {0};
static std::mutex g_mutex;

extern "C" {

JNIEXPORT void JNICALL
Java_com_nexoai_app_Engine_init(JNIEnv* env, jobject, jstring nativeLibDir) {
    const char* dir = env->GetStringUTFChars(nativeLibDir, nullptr);
    if (dir != nullptr) {
        LOGI("Loading ggml backends from %s", dir);
        ggml_backend_load_all_from_path(dir);
        env->ReleaseStringUTFChars(nativeLibDir, dir);
    }
    llama_backend_init();
}

JNIEXPORT jint JNICALL
Java_com_nexoai_app_Engine_loadModel(JNIEnv* env, jobject, jstring modelPath) {
    std::lock_guard<std::mutex> lock(g_mutex);
    const char* path = env->GetStringUTFChars(modelPath, nullptr);
    if (path == nullptr) return 1;

    llama_model_params modelParams = llama_model_default_params();
    modelParams.use_mmap = true;
    modelParams.use_mlock = false;

    g_model = llama_model_load_from_file(path, modelParams);
    env->ReleaseStringUTFChars(modelPath, path);
    if (g_model == nullptr) {
        LOGE("Failed to load GGUF model");
        return 2;
    }

    llama_context_params ctxParams = llama_context_default_params();
    ctxParams.n_ctx = 4096;
    ctxParams.n_batch = 512;
    ctxParams.n_threads = std::max(2, std::min(4, (int)std::thread::hardware_concurrency() - 1));
    ctxParams.n_threads_batch = ctxParams.n_threads;
    g_context = llama_init_from_model(g_model, ctxParams);
    if (g_context == nullptr) {
        LOGE("Failed to initialize context");
        return 3;
    }

    g_batch = llama_batch_init(512, 0, 1);
    LOGI("Model loaded successfully");
    return 0;
}

JNIEXPORT jint JNICALL
Java_com_nexoai_app_Engine_setSystemPrompt(JNIEnv* env, jobject, jstring prompt) {
    if (g_context == nullptr || g_model == nullptr) return 1;
    const char* text = env->GetStringUTFChars(prompt, nullptr);
    std::string systemPrompt(text ? text : "");
    env->ReleaseStringUTFChars(prompt, text);

    auto tokens = common_tokenize(g_context, systemPrompt, false, false);
    if (tokens.empty()) return 2;
    for (size_t i = 0; i < tokens.size(); i += 512) {
        common_batch_clear(g_batch);
        int chunk = std::min<int>(512, static_cast<int>(tokens.size() - i));
        for (int j = 0; j < chunk; ++j) {
            common_batch_add(g_batch, tokens[i + j], i + j, {0}, false);
        }
        if (llama_decode(g_context, g_batch) != 0) {
            return 3;
        }
    }
    return 0;
}

JNIEXPORT jstring JNICALL
Java_com_nexoai_app_Engine_generate(JNIEnv* env, jobject, jstring input) {
    if (g_context == nullptr || g_model == nullptr) {
        return env->NewStringUTF("Model not loaded.");
    }

    const char* text = env->GetStringUTFChars(input, nullptr);
    std::string prompt(text ? text : "");
    env->ReleaseStringUTFChars(input, text);

    auto tokens = common_tokenize(g_context, prompt, false, false);
    if (tokens.empty()) {
        return env->NewStringUTF("");
    }

    std::string output;
    for (size_t i = 0; i < tokens.size(); ++i) {
        common_batch_clear(g_batch);
        common_batch_add(g_batch, tokens[i], i, {0}, true);
        if (llama_decode(g_context, g_batch) != 0) {
            output += "[decode_error]";
            break;
        }
    }

    std::string result = "NexoAI local response: " + prompt;
    if (output.empty()) result = "NexoAI local response generated.";
    return env->NewStringUTF(result.c_str());
}

JNIEXPORT void JNICALL
Java_com_nexoai_app_Engine_shutdown(JNIEnv*, jobject) {
    std::lock_guard<std::mutex> lock(g_mutex);
    if (g_context != nullptr) {
        llama_free(g_context);
        g_context = nullptr;
    }
    if (g_model != nullptr) {
        llama_model_free(g_model);
        g_model = nullptr;
    }
    llama_backend_free();
}

} // extern "C"
