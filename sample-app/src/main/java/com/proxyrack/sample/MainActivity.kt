package com.proxyrack.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proxyrack.sample.ui.theme.ProxySDKSampleTheme
import com.proxyrack.sdk.ProxySDK
import com.proxyrack.sdk.ProxyStatus
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize SDK
        ProxySDK.initialize(this, "your-client-key-here")

        setContent {
            ProxySDKSampleTheme {
                ProxyControlScreen()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ProxySDK.clearCallbacks()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProxyControlScreen() {
    var status by remember { mutableStateOf(ProxySDK.getStatus()) }
    var logs by remember { mutableStateOf(listOf<String>()) }
    var deviceId by remember { mutableStateOf(ProxySDK.getDeviceId()) }

    val scope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    // Set up SDK callbacks
    LaunchedEffect(Unit) {
        ProxySDK.setStatusCallback { newStatus ->
            status = newStatus
        }

        ProxySDK.setLogCallback { message ->
            val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                .format(Date())
            val logEntry = "[$timestamp] $message"
            logs = logs + logEntry

            // Auto-scroll to bottom
            scope.launch {
                if (logs.isNotEmpty()) {
                    lazyListState.animateScrollToItem(logs.size - 1)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Proxyrack SDK Sample",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Card
            StatusCard(
                status = status,
                deviceId = deviceId.take(12) + "..."
            )

            // Control Buttons
            ControlButtons(
                status = status,
                onStart = {
                    val success = ProxySDK.start()
                    if (!success) {
                        logs = logs + "[${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())}] Failed to start proxy service"
                    }
                },
                onStop = { ProxySDK.stop() }
            )

            // Logs Section
            LogsSection(
                logs = logs,
                lazyListState = lazyListState,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatusCard(
    status: ProxyStatus,
    deviceId: String
) {
    val statusColor = when (status) {
        ProxyStatus.CONNECTED -> Color(0xFF4CAF50)
        ProxyStatus.CONNECTING -> Color(0xFFFF9800)
        ProxyStatus.DISCONNECTED -> Color(0xFF757575)
        ProxyStatus.ERROR -> Color(0xFFF44336)
    }

    val statusText = when (status) {
        ProxyStatus.CONNECTED -> "Connected - Earning Money! 💰"
        ProxyStatus.CONNECTING -> "Connecting... 🔄"
        ProxyStatus.DISCONNECTED -> "Disconnected"
        ProxyStatus.ERROR -> "Error - Auto-retrying ⚠️"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Circle,
                    contentDescription = "Status",
                    tint = statusColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Status",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = statusText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = "Device ID: $deviceId",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun ControlButtons(
    status: ProxyStatus,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Start Button
        Button(
            onClick = onStart,
            enabled = status == ProxyStatus.DISCONNECTED || status == ProxyStatus.ERROR,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50)
            )
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Start",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Start Proxy")
        }

        // Stop Button
        Button(
            onClick = onStop,
            enabled = status != ProxyStatus.DISCONNECTED,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF44336)
            )
        ) {
            Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = "Stop",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Stop Proxy")
        }
    }
}

@Composable
fun LogsSection(
    logs: List<String>,
    lazyListState: LazyListState,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Logs (${logs.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxSize(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E1E1E)
            )
        ) {
            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No logs yet. Start the proxy to see activity.",
                        color = Color(0xFF888888),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(logs) { log ->
                        Text(
                            text = log,
                            color = Color(0xFF00FF88),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
