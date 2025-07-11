package com.nfcscanner.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nfcscanner.app.data.models.NFCTag
import java.text.SimpleDateFormat
import java.util.*

/**
 * Card component per visualizzare i tag NFC
 * Mantiene la stessa grafica dell'app React Native
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagCard(
    tag: NFCTag,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Icon Container
                Card(
                    modifier = Modifier.size(48.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFeef2ff)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Smartphone,
                            contentDescription = null,
                            tint = Color(0xFF6366f1),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Tag Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tag.id,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1f2937),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = Color(0xFF6b7280),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = formatTimestamp(tag.timestamp),
                            fontSize = 14.sp,
                            color = Color(0xFF6b7280)
                        )
                    }
                }
                
                // Badges
                Row {
                    if (tag.isWritable) {
                        Card(
                            modifier = Modifier.size(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF10b981)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "W",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                    
                    if (tag.isVirtual) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Card(
                            modifier = Modifier.size(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF8b5cf6)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FlashOn,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Content
            Divider(color = Color(0xFFf3f4f6))
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Records Info
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = Color(0xFF6b7280),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${tag.records.size} record${if (tag.records.size != 1) "s" else ""}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF6b7280)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Record Preview
            if (tag.records.isNotEmpty()) {
                val firstRecord = tag.records[0]
                val preview = if (firstRecord.data.length > 50) {
                    firstRecord.data.substring(0, 50) + "..."
                } else {
                    firstRecord.data
                }
                
                Text(
                    text = "${firstRecord.recordType}: $preview",
                    fontSize = 14.sp,
                    color = Color(0xFF374151),
                    lineHeight = 20.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    text = "No NDEF records",
                    fontSize = 14.sp,
                    color = Color(0xFF374151),
                    lineHeight = 20.sp
                )
            }
            
            // Technologies
            if (tag.technologies.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tag.technologies.take(3).forEach { tech ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFeef2ff)
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Wifi,
                                    contentDescription = null,
                                    tint = Color(0xFF6366f1),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = tech,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF6366f1)
                                )
                            }
                        }
                    }
                    
                    if (tag.technologies.size > 3) {
                        Text(
                            text = "+${tag.technologies.size - 3}",
                            fontSize = 12.sp,
                            color = Color(0xFF9ca3af)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Footer
            Divider(color = Color(0xFFf3f4f6))
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                tag.size?.let { size ->
                    Text(
                        text = "Size: $size bytes",
                        fontSize = 12.sp,
                        color = Color(0xFF9ca3af)
                    )
                }
                
                tag.type?.let { type ->
                    Text(
                        text = "${if (tag.isVirtual) "Virtual" else "Type"}: $type",
                        fontSize = 12.sp,
                        color = Color(0xFF9ca3af)
                    )
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val now = Date()
    val diffMs = now.time - date.time
    val diffMins = diffMs / (1000 * 60)
    val diffHours = diffMs / (1000 * 60 * 60)
    val diffDays = diffMs / (1000 * 60 * 60 * 24)

    return when {
        diffMins < 1 -> "Just now"
        diffMins < 60 -> "${diffMins}m ago"
        diffHours < 24 -> "${diffHours}h ago"
        diffDays < 7 -> "${diffDays}d ago"
        else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(date)
    }
}