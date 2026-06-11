package com.farmlens.anarai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.farmlens.anarai.data.MarketPriceDto
import com.farmlens.anarai.data.MarketPriceService
import com.farmlens.anarai.data.AgmarknetRequest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketPricesScreen(onBack: () -> Unit) {
    var prices by remember { mutableStateOf<List<MarketPriceDto>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    var selectedDate by remember { mutableStateOf(sdf.format(Date())) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())

    LaunchedEffect(selectedDate) {
        prices = null
        error = null
        coroutineScope.launch {
            try {
                val service = MarketPriceService.create()
                val request = AgmarknetRequest(from_date = selectedDate, to_date = selectedDate)
                val response = service.getLivePrices(request)
                if (response.status && response.data != null) {
                    prices = response.data.records.flatMap { it.data }
                } else {
                    error = response.message
                }
            } catch (e: Exception) {
                error = "Failed to fetch live market prices: ${e.message}"
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        selectedDate = sdf.format(Date(it))
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Market Prices (per Quintal)", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select Date")
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
            Text(
                "Variety: Bhagwa | Date: $selectedDate",
                modifier = Modifier.padding(16.dp),
                color = Color.Gray,
                fontWeight = FontWeight.Bold
            )
            
            if (prices == null && error == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF388E3C))
                }
            } else if (error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(error!!, color = Color.Red, fontSize = 16.sp, modifier = Modifier.padding(16.dp))
                }
            } else if (prices!!.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No market prices available for today.", color = Color.Gray, fontSize = 16.sp)
                }
            } else {
                val currentPrices = prices ?: emptyList()
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(currentPrices) { price ->
                        MarketPriceCard(price)
                    }
                }
            }
        }
    }
}

@Composable
fun MarketPriceCard(price: MarketPriceDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(price.market_name ?: "Unknown Market", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1B5E20))
                Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = Color(0xFF388E3C))
            }
            Text("${price.district_name ?: ""}, ${price.state_name ?: ""}", fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Min", fontSize = 12.sp, color = Color.Gray)
                    Text("₹${price.min_price ?: "N/A"}", fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Modal (Average)", fontSize = 12.sp, color = Color.Gray)
                    Text("₹${price.model_price ?: "N/A"}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF388E3C))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Max", fontSize = 12.sp, color = Color.Gray)
                    Text("₹${price.max_price ?: "N/A"}", fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                 Text("Arrival: ${price.arrival_qty ?: "0"} Tonnes", fontSize = 12.sp, color = Color.Gray)
                 Text(price.arrival_date ?: "", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}
