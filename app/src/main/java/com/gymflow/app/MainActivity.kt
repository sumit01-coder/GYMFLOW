package com.gymflow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.gymflow.core.theme.GymFlowTheme
import com.gymflow.feature.navigation.AppNavGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            GymFlowTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = GymFlowTheme.colors.background
                ) {
                    AppNavGraph()
                }
            }
        }
    }
}
