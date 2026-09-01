package com.nexoai.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexoai.app.data.ChatMessage
import com.nexoai.app.service.AIService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val aiService = AIService.getInstance()
    
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()
    
    private val _currentInput = MutableStateFlow("")
    val currentInput: StateFlow<String> = _currentInput.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _selectedAgent = MutableStateFlow("general")
    val selectedAgent: StateFlow<String> = _selectedAgent.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadChatHistory()
    }

    fun setInput(input: String) {
        _currentInput.value = input
    }

    fun selectAgent(agentName: String) {
        _selectedAgent.value = agentName
    }

    fun sendMessage(message: String) {
        if (message.isBlank()) return
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                // Adiciona mensagem do usuário ao histórico
                val userMessage = ChatMessage(
                    role = "user",
                    content = message,
                    agent = _selectedAgent.value
                )
                _chatMessages.value = _chatMessages.value + userMessage
                _currentInput.value = ""
                
                // Chama o serviço de IA
                val agent = _selectedAgent.value
                val response = aiService.generateResponse(
                    message,
                    agent,
                    _chatMessages.value
                )
                
                // Adiciona resposta ao histórico
                val assistantMessage = ChatMessage(
                    role = "assistant",
                    content = response,
                    agent = agent
                )
                _chatMessages.value = _chatMessages.value + assistantMessage
                
                // Salva no banco de dados
                aiService.saveMessage(userMessage)
                aiService.saveMessage(assistantMessage)
                
            } catch (e: Exception) {
                _errorMessage.value = "Erro: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadChatHistory() {
        viewModelScope.launch {
            try {
                val history = aiService.loadChatHistory()
                _chatMessages.value = history
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao carregar histórico: ${e.message}"
            }
        }
    }

    fun clearChatHistory() {
        _chatMessages.value = emptyList()
        aiService.clearChatHistory()
    }

    fun dismissError() {
        _errorMessage.value = null
    }
}
