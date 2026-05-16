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

import com.insieme.app.ui.theme.*

sealed class Screen(
    val route: String, 
    val title: String, 
    val icon: ImageVector, 
    val color: Color
) {
    object Activities : Screen("activities", "Andiamo!", Icons.Default.Favorite, PastelGreen)
    object Media : Screen("media", "Vedere", Icons.Default.PlayArrow, SoftMint)
    object Games : Screen("games", "Giochi", Icons.Default.Star, SoftBlue)
    object Wishlist : Screen("wishlist", "Desideri", Icons.Default.List, SoftPink)
    object Profile : Screen("profile", "Profilo", Icons.Default.Person, SoftYellow)
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

    val barColor by animateColorAsState(targetValue = MaterialTheme.colorScheme.background)

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
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
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
