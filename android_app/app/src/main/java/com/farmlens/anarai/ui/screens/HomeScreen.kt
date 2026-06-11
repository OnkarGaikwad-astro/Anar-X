package com.farmlens.anarai.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import android.Manifest
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import java.io.File

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Spa
import android.annotation.SuppressLint
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.farmlens.anarai.data.WeatherService
import com.farmlens.anarai.data.WeatherResponse
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onImageSelected: (Uri) -> Unit,
    onYieldSelected: (Uri) -> Unit,
    onMarketPricesClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    val context = LocalContext.current
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    var isYieldMode by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempImageUri?.let { if (isYieldMode) onYieldSelected(it) else onImageSelected(it) }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { if (isYieldMode) onYieldSelected(it) else onImageSelected(it) }
    }

    var weatherResponse by remember { mutableStateOf<WeatherResponse?>(null) }
    var weatherError by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        coroutineScope.launch {
                            try {
                                val service = WeatherService.create()
                                weatherResponse = service.getCurrentWeather(location.latitude, location.longitude)
                            } catch (e: Exception) {
                                weatherError = "Failed to fetch weather: ${e.message}"
                            }
                        }
                    } else {
                        weatherError = "Location not found"
                    }
                }
            } catch (e: SecurityException) {
                weatherError = "Security Exception"
            }
        } else {
            weatherError = "Location permission denied"
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    fun launchCamera() {
        try {
            val tempFile = File.createTempFile("capture_", ".jpg", context.cacheDir)
            val uri = FileProvider.getUriForFile(
                context,
                "com.farmlens.anarai.fileprovider",
                tempFile
            )
            tempImageUri = uri
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error setting up camera", Toast.LENGTH_SHORT).show()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(context, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show()
        }
    }

    var showSourceDialog by remember { mutableStateOf(false) }

    if (showSourceDialog) {
        AlertDialog(
            onDismissRequest = { showSourceDialog = false },
            title = { Text(if (isYieldMode) "Estimate Yield" else "Scan Disease") },
            text = { Text("Choose image source") },
            confirmButton = {
                TextButton(onClick = {
                    showSourceDialog = false
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }) {
                    Text("Camera")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSourceDialog = false
                    galleryLauncher.launch("image/*")
                }) {
                    Text("Gallery")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Anar Lens Dashboard", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { (context as? android.app.Activity)?.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Exit App")
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
                .background(Color(0xFFF5F5F5))
        ) {
            // Weather Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF03A9F4)),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        if (weatherResponse != null && weatherResponse!!.current_condition.isNotEmpty()) {
                            val current = weatherResponse!!.current_condition.first()
                            Text("Current Location", color = Color.White.copy(alpha=0.8f), fontSize = 14.sp)
                            Text("${current.temp_C}°C", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                            Text("Humidity: ${current.humidity}%", color = Color.White, fontSize = 14.sp)
                        } else if (weatherError != null) {
                            Text(weatherError!!, color = Color.White, fontSize = 14.sp)
                        } else {
                            Text("Fetching live weather...", color = Color.White, fontSize = 14.sp)
                        }
                    }
                    Icon(Icons.Default.Cloud, contentDescription = "Weather", tint = Color.White, modifier = Modifier.size(64.dp))
                }
            }
            
            if (weatherResponse != null && weatherResponse!!.current_condition.isNotEmpty()) {
                val current = weatherResponse!!.current_condition.first()
                val temp = current.temp_C.toIntOrNull() ?: 25
                val hum = current.humidity.toIntOrNull() ?: 50
                
                val (suggestion, bgColor, textColor, iconColor) = when {
                    hum > 75 -> listOf("High humidity detected. Consider spraying fungicide to prevent fungal diseases.", Color(0xFFFFEBEE), Color(0xFFC62828), Color(0xFFE53935))
                    temp > 35 -> listOf("High temperatures. Ensure adequate irrigation and avoid spraying during peak sun hours.", Color(0xFFFFF8E1), Color(0xFFFF8F00), Color(0xFFFFB300))
                    temp < 15 -> listOf("Low temperatures detected. Protect crops from cold stress.", Color(0xFFE3F2FD), Color(0xFF1565C0), Color(0xFF1E88E5))
                    else -> listOf("Weather is optimal for pomegranate growth. Routine care recommended.", Color(0xFFE8F5E9), Color(0xFF2E7D32), Color(0xFF4CAF50))
                }
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = bgColor as Color),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = "Tip", tint = iconColor as Color)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(suggestion as String, color = textColor as Color, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
            
            Text(
                "Quick Actions",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.DarkGray
            )

            val actions = listOf(
                DashboardAction("Scan Disease", Icons.Default.CameraAlt, MaterialTheme.colorScheme.secondary) {
                    isYieldMode = false
                    showSourceDialog = true
                },
                DashboardAction("Estimate Yield", Icons.Default.Spa, Color(0xFF8BC34A)) {
                    isYieldMode = true
                    showSourceDialog = true
                },
                DashboardAction("Market Prices", Icons.Default.ShoppingCart, Color(0xFFFF9800)) {
                    onMarketPricesClick()
                },
                DashboardAction("History", Icons.Default.History, Color(0xFF9E9E9E)) {
                    onHistoryClick()
                }
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(actions) { action ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clickable { action.onClick() },
                        colors = CardDefaults.cardColors(containerColor = action.color),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(action.icon, contentDescription = action.title, tint = Color.White, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(action.title, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}

data class DashboardAction(val title: String, val icon: ImageVector, val color: Color, val onClick: () -> Unit)
