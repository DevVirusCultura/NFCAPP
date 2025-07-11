package com.nfcscanner.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nfcscanner.app.viewmodel.MainViewModel

/**
 * Schermata impostazioni con la stessa grafica dell'app React Native
 */
@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val appSettings by viewModel.appSettings.observeAsState()
    val allTags by viewModel.allTags.observeAsState(emptyList())
    val nfcSupported by viewModel.nfcSupported.observeAsState(false)
    val nfcEnabled by viewModel.nfcEnabled.observeAsState(false)
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFf8fafc),
                        Color(0xFFe2e8f0)
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
                HeaderSection()
            }
            
            // Scanning Preferences
            item {
                SettingsSection(
                    title = "Scanning Preferences",
                    items = listOf(
                        SettingItem.Switch(
                            icon = Icons.Default.Notifications,
                            title = "Notifications",
                            subtitle = "Get notified when tags are scanned",
                            checked = appSettings?.notifications ?: true,
                            onCheckedChange = { /* Handle change */ }
                        ),
                        SettingItem.Switch(
                            icon = Icons.Default.Download,
                            title = "Auto-save Tags",
                            subtitle = "Automatically save scanned tags to history",
                            checked = appSettings?.autoSave ?: true,
                            onCheckedChange = { /* Handle change */ }
                        ),
                        SettingItem.Switch(
                            icon = Icons.Default.Vibration,
                            title = "Vibration Feedback",
                            subtitle = "Vibrate when tags are detected",
                            checked = appSettings?.vibration ?: true,
                            onCheckedChange = { /* Handle change */ }
                        ),
                        SettingItem.Switch(
                            icon = Icons.Default.VolumeUp,
                            title = "Sound Effects",
                            subtitle = "Play sounds for scan events",
                            checked = appSettings?.soundEnabled ?: true,
                            onCheckedChange = { /* Handle change */ }
                        )
                    )
                )
            }
            
            // Data Management
            item {
                SettingsSection(
                    title = "Data Management",
                    items = listOf(
                        SettingItem.Action(
                            icon = Icons.Default.Download,
                            title = "Export Data",
                            subtitle = "Backup ${allTags.size} scanned tags as JSON",
                            onClick = { /* Handle export */ }
                        ),
                        SettingItem.Action(
                            icon = Icons.Default.Upload,
                            title = "Import Data",
                            subtitle = "Restore from backup file",
                            onClick = { /* Handle import */ }
                        ),
                        SettingItem.Action(
                            icon = Icons.Default.Delete,
                            title = "Clear All Data",
                            subtitle = "Delete all tags and reset settings",
                            onClick = { /* Handle clear */ },
                            isDestructive = true
                        )
                    )
                )
            }
            
            // Help & Feedback
            item {
                SettingsSection(
                    title = "Help & Feedback",
                    items = listOf(
                        SettingItem.Action(
                            icon = Icons.Default.Help,
                            title = "Help & Support",
                            subtitle = "Get help with using the app",
                            onClick = { /* Handle help */ }
                        ),
                        SettingItem.Action(
                            icon = Icons.Default.Star,
                            title = "Rate This App",
                            subtitle = "Help others discover NFC Scanner",
                            onClick = { /* Handle rate */ }
                        ),
                        SettingItem.Action(
                            icon = Icons.Default.Email,
                            title = "Contact Support",
                            subtitle = "Send feedback or report issues",
                            onClick = { /* Handle contact */ }
                        )
                    )
                )
            }
            
            // About & Legal
            item {
                SettingsSection(
                    title = "About & Legal",
                    items = listOf(
                        SettingItem.Action(
                            icon = Icons.Default.Info,
                            title = "About NFC Scanner",
                            subtitle = "Version 2.0.0 • Learn more",
                            onClick = { /* Handle about */ }
                        ),
                        SettingItem.Action(
                            icon = Icons.Default.Security,
                            title = "Privacy Policy",
                            subtitle = "How we protect your data",
                            onClick = { /* Handle privacy */ }
                        ),
                        SettingItem.Action(
                            icon = Icons.Default.Description,
                            title = "Terms of Service",
                            subtitle = "Usage terms and conditions",
                            onClick = { /* Handle terms */ }
                        )
                    )
                )
            }
            
            // Device Information
            item {
                DeviceInfoSection(
                    nfcSupported = nfcSupported,
                    nfcEnabled = nfcEnabled,
                    tagCount = allTags.size,
                    maxHistorySize = appSettings?.maxHistorySize ?: 100
                )
            }
            
            // Footer
            item {
                FooterSection()
            }
            
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun HeaderSection() {
    Column {
        Text(
            text = "Settings",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1f2937)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "Customize your NFC scanning experience",
            fontSize = 16.sp,
            color = Color(0xFF6b7280)
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    items: List<SettingItem>
) {
    Column {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF374151),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column {
                items.forEachIndexed { index, item ->
                    SettingItemRow(item = item)
                    
                    if (index < items.size - 1) {
                        Divider(
                            color = Color(0xFFf3f4f6),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingItemRow(item: SettingItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Card(
            modifier = Modifier.size(40.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (item.isDestructive) Color(0xFFfef2f2) else Color(0xFFf3f4f6)
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = if (item.isDestructive) Color(0xFFef4444) else Color(0xFF6366f1),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Content
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (item.isDestructive) Color(0xFFef4444) else Color(0xFF1f2937)
            )
            
            item.subtitle?.let { subtitle ->
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = Color(0xFF6b7280)
                )
            }
        }
        
        // Action
        when (item) {
            is SettingItem.Switch -> {
                Switch(
                    checked = item.checked,
                    onCheckedChange = item.onCheckedChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF6366f1),
                        checkedTrackColor = Color(0xFFa5b4fc),
                        uncheckedThumbColor = Color(0xFFf3f4f6),
                        uncheckedTrackColor = Color(0xFFd1d5db)
                    )
                )
            }
            is SettingItem.Action -> {
                IconButton(onClick = item.onClick) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color(0xFF9ca3af),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DeviceInfoSection(
    nfcSupported: Boolean,
    nfcEnabled: Boolean,
    tagCount: Int,
    maxHistorySize: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFf9fafb)
        ),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color(0xFFe5e7eb)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Device Information",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF374151),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            DeviceInfoItem(
                icon = Icons.Default.Wifi,
                text = "NFC Support: ${if (nfcSupported) "Available" else "Not Available"}"
            )
            
            DeviceInfoItem(
                icon = Icons.Default.Smartphone,
                text = "NFC Status: ${if (nfcEnabled) "Enabled" else "Disabled"}"
            )
            
            DeviceInfoItem(
                icon = Icons.Default.Settings,
                text = "Platform: Android"
            )
            
            DeviceInfoItem(
                icon = Icons.Default.Description,
                text = "Tags Stored: $tagCount / $maxHistorySize"
            )
        }
    }
}

@Composable
private fun DeviceInfoItem(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF6b7280),
            modifier = Modifier.size(16.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color(0xFF6b7280)
        )
    }
}

@Composable
private fun FooterSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "NFC Scanner • Made with ❤️ for NFC enthusiasts",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF6b7280)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "Open source • Privacy focused • Offline first",
            fontSize = 12.sp,
            color = Color(0xFF9ca3af)
        )
    }
}

sealed class SettingItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String? = null,
    val isDestructive: Boolean = false
) {
    class Switch(
        icon: ImageVector,
        title: String,
        subtitle: String? = null,
        val checked: Boolean,
        val onCheckedChange: (Boolean) -> Unit,
        isDestructive: Boolean = false
    ) : SettingItem(icon, title, subtitle, isDestructive)
    
    class Action(
        icon: ImageVector,
        title: String,
        subtitle: String? = null,
        val onClick: () -> Unit,
        isDestructive: Boolean = false
    ) : SettingItem(icon, title, subtitle, isDestructive)
}