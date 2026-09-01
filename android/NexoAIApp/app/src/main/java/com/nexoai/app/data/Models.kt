package com.nexoai.app.data

import androidx.compose.runtime.Stable

@Stable
data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val role: String, // "user" ou "assistant"
    val content: String,
    val agent: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Stable
data class Project(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val agents: List<String>,
    val status: String = "draft",
    val createdAt: Long = System.currentTimeMillis()
)

@Stable
data class AgentConfig(
    val name: String,
    val emoji: String,
    val shortName: String,
    val description: String,
    val systemPrompt: String
)

// Configurações dos agentes
val AGENTS = mapOf(
    "prompt_engineer" to AgentConfig(
        name = "Prompt Engineer",
        emoji = "📝",
        shortName = "Prompt",
        description = "Especialista em refinamento de prompts",
        systemPrompt = "Você é o Prompt Engineer da NexoAI. Atue como especialista em craft de prompts, refinamento de instruções e otimização de instruções para IA."
    ),
    "design_director" to AgentConfig(
        name = "Design Director",
        emoji = "🎨",
        shortName = "Design",
        description = "Diretor de design e identidade visual",
        systemPrompt = "Você é o Design Director da NexoAI. Atue como diretor criativo especializado em identidade visual, UX/UI e direção estética."
    ),
    "developer" to AgentConfig(
        name = "Developer",
        emoji = "💻",
        shortName = "Dev",
        description = "Desenvolvedor full-stack sênior",
        systemPrompt = "Você é o Developer da NexoAI. Atue como desenvolvedor full-stack senior especializado em arquitetura, implementação e best practices."
    ),
    "web_creator" to AgentConfig(
        name = "Web Creator",
        emoji = "🌐",
        shortName = "Web",
        description = "Criador de web experiences",
        systemPrompt = "Você é o Web Creator da NexoAI. Atue como especialista em criação de pages, funções de conversão e web experiences."
    ),
    "marketing_strategist" to AgentConfig(
        name = "Marketing Strategist",
        emoji = "📊",
        shortName = "Marketing",
        description = "Estrategista de marketing e crescimento",
        systemPrompt = "Você é o Marketing Strategist da NexoAI. Atue como estrategista de marketing, posicionamento e crescimento."
    ),
    "general" to AgentConfig(
        name = "NexoAI General",
        emoji = "✨",
        shortName = "General",
        description = "IA pessoal multi-tarefa",
        systemPrompt = "Você é NexoAI, uma IA pessoal local para criação, design, desenvolvimento e marketing. Atue de forma multidisciplinar conforme necessário."
    )
)
