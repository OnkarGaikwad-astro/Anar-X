package com.farmlens.anarai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.farmlens.anarai.data.remote.ForumReply
import com.farmlens.anarai.ui.viewmodels.PostDetailUiState
import com.farmlens.anarai.ui.viewmodels.PostDetailViewModel
import com.farmlens.anarai.util.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: String,
    onBack: () -> Unit,
    viewModel: PostDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var replyContent by remember { mutableStateOf("") }

    LaunchedEffect(postId) {
        viewModel.fetchPostDetails(postId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post Discussion", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = replyContent,
                        onValueChange = { replyContent = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Write a reply...") },
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (replyContent.isNotBlank()) {
                                viewModel.addReply(replyContent)
                                replyContent = ""
                            }
                        },
                        modifier = Modifier.background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            when (val state = uiState) {
                is PostDetailUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is PostDetailUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.fetchPostDetails(postId) }) {
                            Text("Retry")
                        }
                    }
                }
                is PostDetailUiState.Success -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            ForumPostCard(post = state.post, onClick = {}) // Original post, not clickable here
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Replies (${state.replies.size})",
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray,
                                fontSize = 16.sp
                            )
                        }

                        items(state.replies) { reply ->
                            ReplyCard(reply)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReplyCard(reply: ForumReply) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp), // Indent replies slightly
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp, bottomStart = 12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(reply.authorName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0C617B))
                Text(TimeUtils.formatSupabaseTime(reply.createdAt), fontSize = 10.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(reply.content, fontSize = 14.sp, color = Color.DarkGray, lineHeight = 20.sp)
        }
    }
}
