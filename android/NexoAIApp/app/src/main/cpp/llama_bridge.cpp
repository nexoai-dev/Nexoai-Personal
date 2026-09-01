#include "llama_bridge.h"

#include <android/log.h>
#include <algorithm>
#include <cstdint>
#include <string>
#include <thread>
#include <vector>

#include "llama.h"

#define LOG_TAG "LlamaBridge"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

bool LlamaBridge::loaded_ = false;
void* LlamaBridge::model_ = nullptr;
void* LlamaBridge::context_ = nullptr;

bool LlamaBridge::initialize() {
    llama_backend_init();
    return true;
}

bool LlamaBridge::loadModel(const std::string& modelPath) {
    releaseModel();

    llama_model_params modelParams = llama_model_default_params();

    llama_model* model = llama_model_load_from_file(modelPath.c_str(), modelParams);
    if (model == nullptr) {
        LOGE("Failed to load GGUF model from %s", modelPath.c_str());
        return false;
    }

    llama_context_params ctxParams = llama_context_default_params();
    ctxParams.n_ctx = 2048;
    ctxParams.n_batch = 256;
    ctxParams.n_threads = std::max(1, std::min(4, (int)std::thread::hardware_concurrency() - 1));
    ctxParams.n_threads_batch = ctxParams.n_threads;

    llama_context* ctx = llama_init_from_model(model, ctxParams);
    if (ctx == nullptr) {
        LOGE("Failed to initialize llama context for %s", modelPath.c_str());
        llama_model_free(model);
        return false;
    }

    model_ = model;
    context_ = ctx;
    loaded_ = true;

    char modelDescription[256];
    llama_model_desc(model, modelDescription, sizeof(modelDescription));
    LOGI("Model loaded: %s", modelDescription);
    return true;
}

std::string LlamaBridge::generate(const std::string& prompt) {
    if (!loaded_ || model_ == nullptr || context_ == nullptr) {
        return "Model not loaded.";
    }

    auto* model = static_cast<llama_model*>(model_);
    auto* ctx = static_cast<llama_context*>(context_);

    const struct llama_vocab* vocab = llama_model_get_vocab(model);
    if (vocab == nullptr) {
        return "Failed to access model vocab.";
    }

    std::vector<llama_token> tokens(prompt.size() + 16);
    int32_t nTokens = llama_tokenize(vocab, prompt.c_str(), static_cast<int32_t>(prompt.size()), tokens.data(), static_cast<int32_t>(tokens.size()), true, false);
    if (nTokens <= 0) {
        return "Prompt tokenization failed.";
    }

    tokens.resize(static_cast<size_t>(nTokens));
    llama_batch batch = llama_batch_get_one(tokens.data(), static_cast<int32_t>(tokens.size()));
    if (llama_decode(ctx, batch) != 0) {
        return "Model decode failed.";
    }

    std::string response = "Modelo GGUF carregado com sucesso. Prompt recebido: " + prompt;
    return response;
}

void LlamaBridge::releaseModel() {
    if (context_ != nullptr) {
        llama_free(static_cast<llama_context*>(context_));
        context_ = nullptr;
    }

    if (model_ != nullptr) {
        llama_model_free(static_cast<llama_model*>(model_));
        model_ = nullptr;
    }

    loaded_ = false;
    llama_backend_free();
}

bool LlamaBridge::isLoaded() {
    return loaded_ && model_ != nullptr && context_ != nullptr;
}
