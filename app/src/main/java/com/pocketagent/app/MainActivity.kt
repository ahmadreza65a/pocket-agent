package com.pocketagent.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = SecureStore(applicationContext)
        setContent {
            MaterialTheme {
                var tab by remember { mutableStateOf(0) }
                Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
                    TabRow(selectedTabIndex = tab) {
                        Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Chat") })
                        Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Settings") })
                        Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text("About") })
                    }
                    when (tab) {
                        0 -> ChatScreen(store)
                        1 -> SettingsScreen(store)
                        else -> AboutScreen()
                    }
                }
            }
        }
    }
}
