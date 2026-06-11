package com.farmlens.anarai.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.farmlens.anarai.data.AppDatabase
import com.farmlens.anarai.data.ScanHistoryEntity
import com.farmlens.anarai.ml.MLService
import com.farmlens.anarai.ml.PredictionResult
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(imageUri: Uri, isHistory: Boolean = false, mlService: MLService, onBack: () -> Unit) {
    val context = LocalContext.current
    var prediction by remember { mutableStateOf<PredictionResult?>(null) }
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    // Run inference once when screen opens
    LaunchedEffect(imageUri) {
        val bitmap: Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, imageUri)) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.isMutableRequired = true
            }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
        }
        
        // Copy to software bitmap if hardware, just in case
        val softwareBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        prediction = mlService.predict(softwareBitmap)

        // Initialize TTS
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Try English
                tts?.language = Locale.ENGLISH
                
                prediction?.let { res ->
                    val speechText = if (res.disease == "Healthy") {
                        "Your pomegranate crop is healthy."
                    } else {
                        "Your pomegranate crop has ${getMarathiDiseaseName(res.disease)} with ${getMarathiSeverity(res.severity)} severity."
                    }
                    tts?.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, null)
                
                // Save to database only if this is a new scan
                if (!isHistory) {
                    val db = AppDatabase.getDatabase(context)
                    coroutineScope.launch {
                        db.scanHistoryDao().insert(
                            ScanHistoryEntity(
                                imageUri = imageUri.toString(),
                                disease = res.disease,
                                severity = res.severity,
                                confidence = res.confidence,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                }
                }
            }
        }
    }

    // Removed duplicate DisposableEffect from earlier version

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Result", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            prediction?.let { res ->
                // Image Preview
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Captured Pomegranate",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                val isHealthy = res.disease == "Healthy"
                
                // Color coding based on severity
                val severityColor = when (res.severity) {
                    0 -> Color(0xFF388E3C) // Green (Healthy)
                    1 -> Color(0xFFFBC02D) // Yellow (Early)
                    2 -> Color(0xFFF57C00) // Orange (Moderate)
                    3 -> Color(0xFFD32F2F) // Red (Severe)
                    else -> Color.Gray
                }

                // Disease Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF5F6)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Disease", fontSize = 14.sp, color = Color.Gray)
                        Text(
                            text = getMarathiDiseaseName(res.disease),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isHealthy) Color(0xFF388E3C) else Color(0xFFD32F2F),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Confidence: ${String.format("%.1f", res.confidence)}%",
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                // Severity Card
                if (!isHealthy) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = severityColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Severity", fontSize = 14.sp, color = Color.White.copy(alpha=0.8f))
                            Text(
                                text = getMarathiSeverity(res.severity),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Recommendations
                    Text(
                        text = "Recommendations",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20),
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF5F6)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Text(
                            text = getRecommendation(res.disease),
                            modifier = Modifier.padding(16.dp),
                            fontSize = 16.sp,
                            color = Color.DarkGray,
                            lineHeight = 24.sp
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF388E3C)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Excellent! Your crop is healthy.",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } ?: run {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(64.dp),
                            color = Color(0xFF2E7D32),
                            strokeWidth = 6.dp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Analyzing crop health...",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}

fun getMarathiDiseaseName(disease: String): String {
    return when (disease) {
        "Healthy" -> "Healthy"
        "Bacterial_Blight" -> "Bacterial Blight"
        "Anthracnose" -> "Anthracnose"
        "Cercospora_Fruit_Spot" -> "Cercospora Fruit Spot"
        "Alternaria_Fruit_Spot" -> "Alternaria Fruit Spot"
        else -> disease
    }
}

fun getMarathiSeverity(severity: Int): String {
    return when (severity) {
        0 -> "Healthy"
        1 -> "Early"
        2 -> "Moderate"
        3 -> "Severe"
        else -> "Unknown"
    }
}

fun getRecommendation(disease: String): String {
    return when (disease) {
        "Bacterial_Blight" -> "• Cut and burn affected branches.\n• Spray Copper Oxychloride 25g + Streptocycline 2.5g in 10L water."
        "Anthracnose" -> "• Spray Mancozeb 20g or Propiconazole 10ml in 10L water."
        "Cercospora_Fruit_Spot" -> "• Spray Captan 20g or Difenoconazole 10ml in 10L water."
        "Alternaria_Fruit_Spot" -> "• Spray Chlorothalonil 20g per 10L water."
        else -> "Consult an agricultural expert."
    }
}
