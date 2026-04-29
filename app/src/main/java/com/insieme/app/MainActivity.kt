package com.insieme.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.insieme.app.ui.MainScreen
import com.insieme.app.ui.theme.InsiemeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InsiemeTheme {
                MainScreen()
            }
        }
    }
}
