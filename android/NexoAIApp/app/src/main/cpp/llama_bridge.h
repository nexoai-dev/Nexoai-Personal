#pragma once

#include <string>

class LlamaBridge {
public:
    static bool initialize();
    static bool loadModel(const std::string& modelPath);
    static std::string generate(const std::string& prompt);
    static void releaseModel();
    static bool isLoaded();

private:
    static bool loaded_;
    static void* model_;
    static void* context_;
};
