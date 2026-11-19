package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.core.configureAppCheck
import com.example.ui.navigation.HonoursSolutionApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Configure Firebase App Check BEFORE initializing any ViewModels or Firebase AI/Firestore calls
        configureAppCheck(this, intent)
        enableEdgeToEdge()
        setContent {
            HonoursSolutionApp()
        }
    }
}
