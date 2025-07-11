package com.nfcscanner.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nfcscanner.app.data.models.NFCScannerState
import com.nfcscanner.app.ui.components.TagCard
import com.nfcscanner.app.viewmodel.MainViewModel

/**
 * Schermata principale di scansione NFC
 * Replica la grafica dell'app React Native con gradiente e animazioni
 */
@Composable
fun ScannerScreen(viewModel: MainViewModel) {
    val nfcSupported by viewModel.nfcSupported.observeAsState(false)
    val nfcEnabled by viewModel.nfcEnabled.observeAsState(false)
    val scannerState by viewModel.scannerState.observeAsState(NFCScannerState.IDLE)
    val allTags by viewModel.allTags.observeAsState(emptyList())
    val selectedTag by viewModel.selectedTag.observeAsState()
    val errorMessage by viewModel.errorMessage.observeAsState()
    
    val isScanning = scannerState == NFCScannerState.SCANNING
    val recentTags = allTags.take(3)
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF667eea),
                        Color(0xFF764ba2)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
            
            // Header
            item {
                HeaderSection(
                    tagCount = allTags.size,
                    recentCount = recentTags.size
                )
            }
            
            // Scan Area
            item {
                ScanAreaSection(
                    isScanning = isScanning,
                    nfcSupported = nfcSupported,
                    nfcEnabled = nfcEnabled,
                    errorMessage = errorMessage,
                    onStartScan = { viewModel.startScanning() },
                    onStopScan = { viewModel.stopScanning() }
                )
            }
            
            // Quick Actions
            item {
                QuickActionsSection()
            }
            
            // Last Scanned Tag
            selectedTag?.let { tag ->
                item {
                    LastScannedSection(tag = tag)
                }
            }
            
            // Recent Tags
            if (recentTags.isNotEmpty()) {
                item {
                    Text(
                        text = "Recent Scans",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                items(recentTags) { tag ->
                    TagCard(
                        tag = tag,
                        onClick = { /* Navigate to details */ }
                    )
                }
            }
            
            // Empty State
            if (allTags.isEmpty()) {
                item {
                    EmptyStateSection()
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun HeaderSection(tagCount: Int, recentCount: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "NFC Scanner",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Hold an NFC tag near your device to scan",
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Stats Container
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    number = tagCount.toString(),
                    label = "Tags Scanned"
                )
                
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(Color.White.copy(alpha = 0.2f))
                )
                
                StatItem(
                    number = recentCount.toString(),
                    label = "Recent"
                )
            }
        }
    }
}

@Composable
private fun StatItem(number: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = number,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun ScanAreaSection(
    isScanning: Boolean,
    nfcSupported: Boolean,
    nfcEnabled: Boolean,
    errorMessage: String?,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Scan Animation
        ScanAnimation(isScanning = isScanning)
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Status Indicator
        StatusIndicator(
            nfcSupported = nfcSupported,
            nfcEnabled = nfcEnabled
        )
        
        // Error Message
        errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(12.dp))
            ErrorMessage(message = error)
        }
        
        Spacer(modifier = Modifier.height(30.dp))
        
        // Scan Button
        ScanButton(
            isScanning = isScanning,
            enabled = nfcSupported && nfcEnabled,
            onStartScan = onStartScan,
            onStopScan = onStopScan
        )
    }
}

@Composable
private fun ScanAnimation(isScanning: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isScanning) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (isScanning) 0.8f else 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    Box(
        modifier = Modifier.size(220.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .scale(scale)
        ) {
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = size.minDimension / 2,
                style = Stroke(width = 4.dp.toPx())
            )
        }
        
        Icon(
            imageVector = Icons.Default.Smartphone,
            contentDescription = "NFC",
            modifier = Modifier.size(48.dp),
            tint = Color.White
        )
    }
}

@Composable
private fun StatusIndicator(nfcSupported: Boolean, nfcEnabled: Boolean) {
    val (statusText, statusColor, statusIcon) = when {
        !nfcSupported -> Triple("Not Supported", Color(0xFFef4444), Icons.Default.WifiOff)
        !nfcEnabled -> Triple("Disabled", Color(0xFFf59e0b), Icons.Default.Warning)
        else -> Triple("Ready", Color(0xFF10b981), Icons.Default.CheckCircle)
    }
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = statusColor
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = statusIcon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "NFC $statusText",
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ErrorMessage(message: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFef4444).copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color(0xFFef4444).copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFfca5a5),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = message,
                color = Color(0xFFfca5a5),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ScanButton(
    isScanning: Boolean,
    enabled: Boolean,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit
) {
    Button(
        onClick = if (isScanning) onStopScan else onStartScan,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isScanning) Color(0xFFef4444) else Color.White.copy(alpha = 0.2f),
            disabledContainerColor = Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Icon(
            imageVector = if (isScanning) Icons.Default.Stop else Icons.Default.Search,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (isScanning) "Stop Scanning" else "Start Scan",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun QuickActionsSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickActionButton(
            icon = Icons.Default.History,
            text = "History",
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            icon = Icons.Default.FlashOn,
            text = "Emulator",
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            icon = Icons.Default.Settings,
            text = "Settings",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = { /* Handle navigation */ },
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun LastScannedSection(tag: com.nfcscanner.app.data.models.NFCTag) {
    Column {
        Text(
            text = "Last Scanned Tag",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        TagCard(
            tag = tag,
            onClick = { /* Navigate to details */ }
        )
    }
}

@Composable
private fun EmptyStateSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 60.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Smartphone,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color.White.copy(alpha = 0.3f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No Tags Scanned Yet",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.8f)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Tap the scan button above to start detecting NFC tags",
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}