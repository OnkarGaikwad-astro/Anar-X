package com.farmlens.anarai.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.farmlens.anarai.ml.OnDeviceLLMService
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val isTyping: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AskAurexScreen(viewModel: AskAurexViewModel) {
    val context = LocalContext.current
    val isModelDownloaded by viewModel.isModelDownloaded.collectAsState()
    val isModelInitializing by viewModel.isModelInitializing.collectAsState()
    val isModelReady by viewModel.isModelReady.collectAsState()
    
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    // NEW UN-GATED MIRROR URL
    val MODEL_URL = "https://huggingface.co/metsman/gemma-2b-it-cpu-int4-org/resolve/4cd87fa447d1620a8cb7a315c92f29b8d8c34269/gemma-2b-it-cpu-int4.bin"
    val MODEL_FILENAME = "gemma.bin"

    // Only initialize the model if it's downloaded
    LaunchedEffect(isModelDownloaded) {
        if (isModelDownloaded) {
            viewModel.initializeModel()
        }
    }

    if (!isModelDownloaded) {
        // Download Screen
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Aurex AI Model Required",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp),
                textAlign = TextAlign.Center
            )
            Text(
                "To run the AI completely offline, you need to download the core intelligence model (1.3 GB). " +
                        "This only needs to be done once.",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 32.dp),
                textAlign = TextAlign.Center
            )

            Button(
                onClick = {
                    isDownloading = true
                    errorMessage = null
                    coroutineScope.launch {
                        downloadModelCustom(context, MODEL_URL, MODEL_FILENAME,
                            onProgress = { progress ->
                                downloadProgress = progress
                            },
                            onComplete = {
                                viewModel.updateModelDownloadedStatus()
                                isDownloading = false
                            },
                            onError = { error ->
                                isDownloading = false
                                errorMessage = error
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isDownloading
            ) {
                if (isDownloading) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Downloading... ${(downloadProgress * 100).toInt()}%", fontSize = 16.sp)
                        LinearProgressIndicator(
                            progress = { downloadProgress },
                            modifier = Modifier.fillMaxWidth(0.5f).padding(top = 4.dp),
                            color = Color.White,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                    }
                } else {
                    Text("Download AI Model", fontSize = 18.sp)
                }
            }
            
            errorMessage?.let { error ->
                Text(
                    text = "Error: $error",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    } else if (isModelInitializing) {
        // Loading Screen
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Initializing On-Device Engine...", fontSize = 16.sp, color = Color.Gray)
            Text("This takes a few seconds on low-end processors.", fontSize = 14.sp, color = Color.Gray)
        }
    } else if (isModelReady) {
        // Chat UI
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Ask Aurex", fontWeight = FontWeight.Bold) },
                    actions = {
                        IconButton(onClick = { viewModel.clearChat() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear Conversation")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                ChatInterface(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInterface(viewModel: AskAurexViewModel) {
    val messages by viewModel.messages.collectAsState()
    var inputText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            reverseLayout = true
        ) {
            items(messages.reversed()) { message ->
                ChatBubble(message)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask Aurex...") },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            FloatingActionButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.isUser
    val backgroundColor = if (isUser) MaterialTheme.colorScheme.primary else Color(0xFFE5F1F3)
    val textColor = if (isUser) Color.White else Color.Black
    val alignment = if (isUser) Alignment.End else Alignment.Start

    val bubbleShape = if (isUser) {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 20.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFF0C617B), androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = "Aurex",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            Box(
                modifier = Modifier
                    .background(
                        color = backgroundColor,
                        shape = bubbleShape
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .widthIn(max = 260.dp)
            ) {
                Text(
                    text = message.text,
                    color = textColor,
                    fontSize = 16.sp,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

// Helper function to download manually using Coroutines to bypass buggy DownloadManager
private suspend fun downloadModelCustom(
    context: Context,
    urlStr: String,
    filename: String,
    onProgress: (Float) -> Unit,
    onComplete: () -> Unit,
    onError: (String) -> Unit
) {
    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val url = java.net.URL(urlStr)
            val connection = url.openConnection() as java.net.HttpURLConnection
            // Follow redirects if HuggingFace sends a 302
            connection.instanceFollowRedirects = true
            connection.connect()

            if (connection.responseCode != java.net.HttpURLConnection.HTTP_OK && connection.responseCode != 302) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onError("Server returned HTTP ${connection.responseCode}")
                }
                return@withContext
            }

            val fileLength = connection.contentLength
            val input = java.io.BufferedInputStream(connection.inputStream)
            val outputFile = File(context.getExternalFilesDir(null), filename)
            val output = java.io.FileOutputStream(outputFile)

            val data = ByteArray(1024 * 8)
            var total: Long = 0
            var count: Int

            while (input.read(data).also { count = it } != -1) {
                total += count.toLong()
                if (fileLength > 0) {
                    val progress = (total * 100 / fileLength).toFloat() / 100f
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        onProgress(progress)
                    }
                }
                output.write(data, 0, count)
            }

            output.flush()
            output.close()
            input.close()

            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                onComplete()
            }

        } catch (e: Exception) {
            val file = File(context.getExternalFilesDir(null), filename)
            if (file.exists()) file.delete()
            
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                onError(e.message ?: "Unknown error")
            }
        }
    }
}
