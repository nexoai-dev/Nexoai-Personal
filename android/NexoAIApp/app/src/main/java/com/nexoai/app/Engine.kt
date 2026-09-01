package com.nexoai.app

class Engine {
    companion object {
        init {
            System.loadLibrary("nexoai_native")
        }
    }

    external fun init(nativeLibDir: String)
    external fun loadModel(modelPath: String): Int
    external fun setSystemPrompt(prompt: String): Int
    external fun generate(input: String): String
    external fun releaseModel()
    external fun shutdown()
}
