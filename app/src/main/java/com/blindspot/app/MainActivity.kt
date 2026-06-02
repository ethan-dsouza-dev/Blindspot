package com.blindspot.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.blindspot.app.ui.BlindspotApp
import com.blindspot.app.ui.theme.BlindspotTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BlindspotTheme {
                BlindspotApp()
            }
        }
    }
}