package com.nexoai.app.service

import android.content.Context
import com.nexoai.app.data.AGENTS
import com.nexoai.app.data.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket
import java.util.concurrent.TimeoutException

class AIService(private val context: Context) {
    private var socket: Socket? = null
    private var reader: BufferedReader? = null
    private var writer: OutputStreamWriter? = null
    
    companion object {
        private const val HOST = "localhost"
        private const val PORT = 9999
        private var instance: AIService? = null
        
        fun getInstance(context: Context? = null): AIService {
            if (instance == null && context != null) {
                instance = AIService(context)
            }
            return instance ?: throw IllegalStateException("AIService not initialized")
        }
    }

    suspend fun generateResponse(
        prompt: String,
        agent: String = "general",
        history: List<ChatMessage> = emptyList()
    ): String = withContext(Dispatchers.IO) {
        return@withContext try {
            ensureConnected()
            
            val systemPrompt = AGENTS[agent]?.systemPrompt 
                ?: "Você é NexoAI, assistente local inteligente."
            
            // Formata a requisição como JSON
            val request = buildRequestJson(prompt, agent, systemPrompt, history)
            
            // Envia a requisição
            writer?.write(request)
            writer?.write("\n")
            writer?.flush()
            
            // Lê a resposta com timeout
            val response = readResponseWithTimeout()
            response ?: "Sem resposta do motor de IA."
        } catch (e: TimeoutException) {
            "Timeout: Motor de IA não respondeu no tempo limite."
        } catch (e: Exception) {
            "Erro de comunicação: ${e.message}"
        }
    }

    fun saveMessage(message: ChatMessage) {
        // Implementar persistência local
        // Por enquanto, será feita no banco SQLite via backend
    }

    fun loadChatHistory(): List<ChatMessage> {
        // Implementar carregamento do histórico
        return emptyList()
    }

    fun clearChatHistory() {
        // Limpar histórico
    }

    private suspend fun ensureConnected() {
        if (socket == null || !socket!!.isConnected) {
            disconnect()
            connectToService()
        }
    }

    private suspend fun connectToService() = withContext(Dispatchers.IO) {
        try {
            socket = Socket(HOST, PORT)
            reader = BufferedReader(InputStreamReader(socket!!.inputStream))
            writer = OutputStreamWriter(socket!!.outputStream)
        } catch (e: Exception) {
            socket = null
            reader = null
            writer = null
            throw Exception("Não foi possível conectar ao serviço de IA: ${e.message}")
        }
    }

    private suspend fun readResponseWithTimeout(timeoutMs: Long = 30000): String? = 
        withContext(Dispatchers.IO) {
            return@withContext try {
                val startTime = System.currentTimeMillis()
                var line: String?
                val response = StringBuilder()
                
                while (true) {
                    if (System.currentTimeMillis() - startTime > timeoutMs) {
                        throw TimeoutException("Resposta não recebida em ${timeoutMs}ms")
                    }
                    
                    line = reader?.readLine() ?: break
                    if (line == "::END::") break
                    response.append(line).append("\n")
                }
                
                response.toString().trim().takeIf { it.isNotEmpty() }
            } catch (e: Exception) {
                if (e is TimeoutException) throw e
                null
            }
        }

    private fun buildRequestJson(
        prompt: String,
        agent: String,
        systemPrompt: String,
        history: List<ChatMessage>
    ): String {
        // Formata como JSON simples
        val historyJson = history.takeLast(5).joinToString(",") { msg ->
            """{"role":"${msg.role}","content":"${msg.content.replace("\"", "\\\"")}"}"""
        }
        
        return """{
            "prompt": "${prompt.replace("\"", "\\\"")}",
            "agent": "$agent",
            "system_prompt": "${systemPrompt.replace("\"", "\\\"")}",
            "history": [$historyJson]
        }"""
    }

    fun disconnect() {
        try {
            writer?.close()
            reader?.close()
            socket?.close()
        } catch (e: Exception) {
            // Ignorar erros de desconexão
        } finally {
            socket = null
            reader = null
            writer = null
        }
    }

    override fun finalize() {
        disconnect()
        super.finalize()
    }
}
