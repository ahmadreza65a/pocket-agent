package com.pocketagent.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = SecureStore(applicationContext)
        setContent {
            MaterialTheme {
                var tab by remember { mutableStateOf(0) }
                val titles = listOf("Chat", "Hosts", "Project", "Settings", "About")
                Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
                    ScrollableTabRow(selectedTabIndex = tab, edgePadding = 8.dp) {
                        titles.forEachIndexed { i, title ->
                            Tab(
                                selected = tab == i,
                                onClick = { tab = i },
                                text = { Text(title) }
                            )
                        }
                    }
                    when (tab) {
                        0 -> ChatScreen(store)
                        1 -> HostsScreen(store)
                        2 -> ProjectScreen(store)
                        3 -> SettingsScreen(store)
                        else -> AboutScreen()
                    }
                }
            }
        }
    }
}
