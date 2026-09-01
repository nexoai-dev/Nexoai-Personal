package com.nexoai.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nexoai.app.data.AGENTS
import com.nexoai.app.data.ChatMessage
import com.nexoai.app.ui.ChatViewModel
import com.nexoai.app.ui.theme.NexoAITheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NexoAITheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NexoAIApp()
                }
            }
        }
    }
}

@Composable
fun NexoAIApp(viewModel: ChatViewModel = viewModel()) {
    val chatMessages by viewModel.chatMessages.collectAsState(initial = emptyList())
    val currentInput by viewModel.currentInput.collectAsState(initial = "")
    val isLoading by viewModel.isLoading.collectAsState(initial = false)
    val selectedAgent by viewModel.selectedAgent.collectAsState(initial = "general")
    val errorMessage by viewModel.errorMessage.collectAsState(initial = null)
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(chatMessages.size - 1)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1419))
    ) {
        // Header
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            color = Color(0xFF1A1F26),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "NexoAI",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFF00D9FF)
                )
                Text(
                    "IA Local | Criação • Design • Dev • Marketing",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFB0B9C1),
                    modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AGENTS.forEach { (key, agent) ->
                        AgentButton(
                            agent.emoji,
                            agent.shortName,
                            selected = selectedAgent == key,
                            onClick = { viewModel.selectAgent(key) }
                        )
                    }
                }
            }
        }

        // Error Message
        if (errorMessage != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                color = Color(0xFF3D1F1F),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        errorMessage!!,
                        color = Color(0xFFFF6B6B),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = { viewModel.dismissError() },
                        modifier = Modifier.size(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00D9FF)
                        )
                    ) {
                        Text("✕", color = Color.Black)
                    }
                }
            }
        }

        // Chat Messages
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            state = listState,
            reverseLayout = false
        ) {
            if (chatMessages.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "✨",
                            style = MaterialTheme.typography.displayLarge,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Text(
                            "Bem-vindo ao NexoAI",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color(0xFF00D9FF)
                        )
                        Text(
                            "Sua IA pessoal local",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFB0B9C1),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            items(chatMessages) { message ->
                ChatBubble(message)
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color(0xFF00D9FF),
                            strokeWidth = 2.dp
                        )
                        Text(
                            "NexoAI está pensando...",
                            color = Color(0xFFB0B9C1),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                }
            }
        }

        // Input Area
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            color = Color(0xFF1A1F26),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = currentInput,
                    onValueChange = { viewModel.setInput(it) },
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp)),
                    placeholder = {
                        Text("Digite sua mensagem...", color = Color(0xFF707A8C))
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00D9FF),
                        unfocusedBorderColor = Color(0xFF2A3038),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true,
                    enabled = !isLoading
                )

                IconButton(
                    onClick = {
                        if (currentInput.isNotBlank()) {
                            viewModel.sendMessage(currentInput)
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF00D9FF)),
                    enabled = !isLoading && currentInput.isNotBlank()
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Enviar",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.role == "user"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .clip(RoundedCornerShape(12.dp)),
            color = if (isUser) Color(0xFF00D9FF) else Color(0xFF2A3038),
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    message.content,
                    color = if (isUser) Color.Black else Color(0xFFFFFFFF),
                    style = MaterialTheme.typography.bodyMedium
                )
                if (message.agent != null && !isUser) {
                    Text(
                        "via ${message.agent}",
                        color = Color(0xFF00D9FF),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AgentButton(emoji: String, name: String, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .height(32.dp)
            .clip(RoundedCornerShape(6.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color(0xFF00D9FF) else Color(0xFF2A3038)
        ),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
    ) {
        Text(
            "$emoji $name",
            color = if (selected) Color.Black else Color(0xFFB0B9C1),
            style = MaterialTheme.typography.labelSmall
        )
    }
}
