package com.farmlens.anarai.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.farmlens.anarai.ml.MLService

sealed class BottomNavItem(val route: String, val icon: ImageVector, val title: String) {
    object Home : BottomNavItem("home", Icons.Default.Dashboard, "Home")
    object Expenses : BottomNavItem("expenses", Icons.Default.AttachMoney, "Expenses")
    object Forum : BottomNavItem("forum", Icons.Default.Forum, "Forum")
    object Calendar : BottomNavItem("calendar", Icons.Default.CalendarMonth, "Schedule")
}

@Composable
fun MainScreen(mlService: MLService, globalNavController: NavController) {
    val navController = rememberNavController()
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Expenses,
        BottomNavItem.Forum,
        BottomNavItem.Calendar
    )

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            NavigationBar(
                containerColor = Color.White,
                modifier = Modifier.height(72.dp)
            ) {
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = currentRoute == item.route,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF0C617B),
                            selectedTextColor = Color(0xFF0C617B),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color(0xFFEEF5F6)
                        ),
                        onClick = {
                            navController.navigate(item.route) {
                                navController.graph.startDestinationRoute?.let { route ->
                                    popUpTo(route) { saveState = true }
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }
        ) {
            composable(BottomNavItem.Home.route) {
                HomeScreen(
                    onImageSelected = { uri ->
                        globalNavController.navigate("result/${Uri.encode(uri.toString())}")
                    },
                    onYieldSelected = { uri ->
                        globalNavController.navigate("yield_result/${Uri.encode(uri.toString())}")
                    },
                    onMarketPricesClick = {
                        globalNavController.navigate("market_prices")
                    },
                    onHistoryClick = {
                        globalNavController.navigate("history_view")
                    }
                )
            }
            composable(BottomNavItem.Expenses.route) {
                ExpenseScreen(onBack = { 
                    navController.navigate(BottomNavItem.Home.route) { 
                        popUpTo(navController.graph.startDestinationRoute!!) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    } 
                })
            }
            composable(BottomNavItem.Forum.route) {
                ForumScreen(onBack = { 
                    navController.navigate(BottomNavItem.Home.route) { 
                        popUpTo(navController.graph.startDestinationRoute!!) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    } 
                })
            }
            composable(BottomNavItem.Calendar.route) {
                CalendarScreen(onBack = { 
                    navController.navigate(BottomNavItem.Home.route) { 
                        popUpTo(navController.graph.startDestinationRoute!!) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    } 
                })
            }
        }
    }
}
