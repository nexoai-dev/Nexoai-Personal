package com.nexoai.app.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.*

/**
 * Serviço para gerenciar conexão com servidor de IA em background.
 */
class AIBackgroundService : Service() {
    private val binder = LocalBinder()
    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private var aiService: AIService? = null

    inner class LocalBinder : Binder() {
        fun getService(): AIBackgroundService = this@AIBackgroundService
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("AIBackgroundService", "Serviço criado")
        aiService = AIService.getInstance(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("AIBackgroundService", "Serviço iniciado")
        
        scope.launch {
            try {
                // Verifica conexão com servidor
                aiService?.generateResponse("Ping", "general", emptyList())
                Log.d("AIBackgroundService", "Conectado ao servidor de IA")
            } catch (e: Exception) {
                Log.e("AIBackgroundService", "Erro de conexão: ${e.message}")
            }
        }
        
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        super.onDestroy()
        Log.d("AIBackgroundService", "Serviço destruído")
        aiService?.disconnect()
        scope.cancel()
    }
}
