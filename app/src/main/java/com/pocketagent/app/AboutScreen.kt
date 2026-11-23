package com.pocketagent.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp

@Composable
fun AboutScreen() {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Pocket Agent", style = MaterialTheme.typography.headlineMedium)
        Text("Version 0.3", style = MaterialTheme.typography.bodyMedium)

        Text(
            "Pocket Agent یک دستیار برنامه‌نویسی هوشمند است که در جیب شما جا می‌گیرد. هدف آن این است که با اتصال به مدل‌های مختلف هوش مصنوعی، ساخت و ویرایش پروژه‌های برنامه‌نویسی را مستقیم از روی گوشی ممکن کند؛ از راه‌اندازی یک وب‌سایت تا مدیریت پروژه‌های آنلاین روی هاست.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth()
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "درباره سازنده",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "احمدرضا یزدان‌دوست",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "توسعه‌دهندهٔ وب و اپلیکیشن. او ایده‌ها را به محصولی واقعی و قابل استفاده تبدیل می‌کند؛ از طراحی و راه‌اندازی وب‌سایت تا ساخت اپلیکیشن‌های اندروید. باور او این است که فناوری باید ساده، سریع و در دسترس همه باشد. Pocket Agent هم برای همین ساخته شد: تا هر جا که هستید، پروژه‌تان همراه شما باشد.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "ارتباط با سازنده",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth()
                )
                TextButton(onClick = { uriHandler.openUri("https://yazdandoost.ir") }) {
                    Text("yazdandoost.ir")
                }
                TextButton(onClick = { uriHandler.openUri("mailto:info@yazdandoost.ir") }) {
                    Text("info@yazdandoost.ir")
                }
            }
        }
    }
}
