package com.insieme.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.insieme.app.ui.activities.ActivitiesScreen
import com.insieme.app.ui.media.MediaScreen
import com.insieme.app.ui.games.GamesScreen
import com.insieme.app.ui.profile.ProfileScreen
import com.insieme.app.ui.viewmodel.InsiemeViewModel
import com.insieme.app.ui.wishlist.WishlistScreen

sealed class Screen(
    val route: String, 
    val title: String, 
    val icon: ImageVector, 
    val color: Color,
    val bgColor: Color
) {
    object Activities : Screen(
        "activities", "Andiamo!", Icons.Default.Favorite, 
        Color(0xFFA5D6A7), Color(0xFFF9FBF9) // Ultra-Pastel Green
    )
    object Media : Screen(
        "media", "Vedere", Icons.Default.PlayArrow, 
        Color(0xFFBCB1A1), Color(0xFFFDFCFB) // Ultra-Pastel Beige
    )
    object Games : Screen(
        "games", "Giochi", Icons.Default.Star, 
        Color(0xFF90CAF9), Color(0xFFF0F7FF) // Ultra-Pastel Blue
    )
    object Wishlist : Screen(
        "wishlist", "Desideri", Icons.Default.List, 
        Color(0xFFF48FB1), Color(0xFFFFF5F8) // Ultra-Pastel Pink
    )
    object Profile : Screen(
        "profile", "Profilo", Icons.Default.Person, 
        Color(0xFFFFE082), Color(0xFFFFFFF0) // Ultra-Pastel Yellow
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: InsiemeViewModel = viewModel()) {
    val navController = rememberNavController()
    val items = listOf(
        Screen.Activities,
        Screen.Media,
        Screen.Games,
        Screen.Wishlist,
        Screen.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentScreen = items.find { screen -> 
        currentDestination?.hierarchy?.any { it.route == screen.route } == true 
    } ?: Screen.Activities

    val barColor by animateColorAsState(targetValue = currentScreen.bgColor)

    val navigateToProfile = {
        navController.navigate(Screen.Profile.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = barColor,
                tonalElevation = 0.dp
            ) {
                items.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null, modifier = Modifier.size(24.dp)) },
                        label = { Text(screen.title, style = MaterialTheme.typography.labelSmall) },
                        selected = selected,
                        onClick = {
                            viewModel.finalizeName()
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = screen.color,
                            selectedTextColor = screen.color,
                            indicatorColor = screen.color.copy(alpha = 0.15f),
                            unselectedIconColor = Color.LightGray.copy(alpha = 0.5f),
                            unselectedTextColor = Color.LightGray.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController, 
            startDestination = Screen.Activities.route, 
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Activities.route) { 
                ActivitiesScreen(viewModel, onNavigateToProfile = navigateToProfile) 
            }
            composable(Screen.Media.route) { 
                MediaScreen(viewModel, onNavigateToProfile = navigateToProfile) 
            }
            composable(Screen.Games.route) { 
                GamesScreen(viewModel, onNavigateToProfile = navigateToProfile) 
            }
            composable(Screen.Wishlist.route) { 
                WishlistScreen(viewModel, onNavigateToProfile = navigateToProfile) 
            }
            composable(Screen.Profile.route) { ProfileScreen(viewModel) }
        }
    }
}
