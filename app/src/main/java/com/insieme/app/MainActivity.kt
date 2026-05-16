package com.insieme.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.insieme.app.ui.MainScreen
import com.insieme.app.ui.theme.InsiemeTheme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.insieme.app.ui.viewmodel.InsiemeViewModel
import com.insieme.app.ui.viewmodel.ThemeMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: InsiemeViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState()
            
            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            InsiemeTheme(darkTheme = darkTheme) {
                MainScreen(viewModel)
            }
        }
    }
}
