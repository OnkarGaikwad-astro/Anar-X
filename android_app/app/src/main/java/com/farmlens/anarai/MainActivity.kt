package com.farmlens.anarai

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.farmlens.anarai.ml.MLService
import com.farmlens.anarai.ui.screens.MainScreen
import com.farmlens.anarai.ui.screens.ResultScreen
import com.farmlens.anarai.ui.screens.SplashScreen
import com.farmlens.anarai.ui.theme.FarmLensTheme

enum class Screen {
    HOME,
    SCAN,
    RESULT,
    HISTORY,
    SPRAY_SCHEDULE,
    ASK_AUREX
}

class MainActivity : ComponentActivity() {
    private lateinit var mlService: MLService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        createNotificationChannel()
        
        mlService = MLService(this)

        setContent {
            FarmLensTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "splash") {
                        composable("splash") {
                            SplashScreen(onNavigateToHome = {
                                navController.navigate("home") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            })
                        }
                        composable("home") {
                            MainScreen(mlService = mlService, globalNavController = navController)
                        }
                        composable(
                            route = "result/{imageUri}?fromHistory={fromHistory}",
                            arguments = listOf(
                                androidx.navigation.navArgument("fromHistory") {
                                    type = androidx.navigation.NavType.BoolType
                                    defaultValue = false
                                }
                            )
                        ) { backStackEntry ->
                            val uriString = backStackEntry.arguments?.getString("imageUri")
                            val fromHistory = backStackEntry.arguments?.getBoolean("fromHistory") ?: false
                            val uri = uriString?.let { Uri.parse(it) }
                            if (uri != null) {
                                ResultScreen(
                                    imageUri = uri,
                                    isHistory = fromHistory,
                                    mlService = mlService,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mlService.close()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Treatment Reminders"
            val descriptionText = "Notifications for scheduled chemical sprays"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("treatment_reminders", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
