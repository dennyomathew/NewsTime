package com.dennymathew.newstime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.dennymathew.newstime.ui.headlines.HeadlinesRoute
import com.dennymathew.newstime.ui.theme.NewsTimeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            NewsTimeTheme {
                HeadlinesRoute()
            }
        }
    }
}
