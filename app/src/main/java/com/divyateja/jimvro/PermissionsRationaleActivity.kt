package com.divyateja.jimvro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.divyateja.jimvro.ui.theme.JimvroTheme
import com.divyateja.jimvro.ui.theme.ThemeMode

class PermissionsRationaleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JimvroTheme(ThemeMode.SYSTEM) {
                Column(
                    Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 64.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    Text("Health Connect privacy", style = MaterialTheme.typography.headlineLarge)
                    Text(
                        "Jimvro writes finished workouts, weight, body-fat percentage, and nutrition to your on-device Health Connect store only when you choose Sync Health Connect.",
                    )
                    Text(
                        "Jimvro does not upload this health data to a server. You can revoke access or delete shared records from Health Connect at any time.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
