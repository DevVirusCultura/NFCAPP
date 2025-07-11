package com.nfcscanner.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nfcscanner.app.data.models.NFCTag
import com.nfcscanner.app.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Schermata dettagli tag con la stessa grafica dell'app React Native
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagDetailsScreen(
    tagId: String,
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val allTags by viewModel.allTags.observeAsState(emptyList())
    val tag = allTags.find { it.id == tagId }
    
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
        if (tag != null) {
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
                        onNavigateBack = onNavigateBack,
                        onShare = { /* Handle share */ },
                        onDelete = { /* Handle delete */ }
                    )
                }
                
                // Tag Card
                item {
                    TagInfoCard(tag = tag)
                }
                
                // Records Section
                if (tag.records.isNotEmpty()) {
                    item {
                        Text(
                            text = "NDEF Records (${tag.records.size})",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF374151)
                        )
                    }
                    
                    items(tag.records) { record ->
                        RecordCard(record = record)
                    }
                }
                
                // Actions Section
                item {
                    ActionsSection(
                        onEmulate = { /* Handle emulate */ },
                        onWrite = { /* Handle write */ }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        } else {
            // Tag not found
            TagNotFoundState(onNavigateBack = onNavigateBack)
        }
    }
}

@Composable
private fun HeaderSection(
    onNavigateBack: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF1f2937)
                )
            }
            
            Text(
                text = "Tag Details",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1f2937)
            )
        }
        
        Row {
            IconButton(onClick = onShare) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = Color(0xFF6366f1)
                )
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color(0xFFef4444)
                )
            }
        }
    }
}

@Composable
private fun TagInfoCard(tag: NFCTag) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Tag Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Card(
                    modifier = Modifier.size(60.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFeef2ff)
                    ),
                    shape = RoundedCornerShape(30.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Smartphone,
                            contentDescription = null,
                            tint = Color(0xFF6366f1),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tag.id,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1f2937)
                    )
                    
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
                            text = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                                .format(Date(tag.timestamp)),
                            fontSize = 14.sp,
                            color = Color(0xFF6b7280)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Tag Details
            Divider(color = Color(0xFFf3f4f6))
            
            Spacer(modifier = Modifier.height(16.dp))
            
            tag.uid?.let { uid ->
                DetailRow(label = "UID", value = uid)
            }
            
            tag.type?.let { type ->
                DetailRow(label = "Type", value = type)
            }
            
            if (tag.technologies.isNotEmpty()) {
                DetailRow(
                    label = "Technologies",
                    value = tag.technologies.joinToString(", ")
                )
            }
            
            tag.size?.let { size ->
                DetailRow(label = "Size", value = "$size bytes")
            }
            
            DetailRow(
                label = "Writable",
                value = if (tag.isWritable) "Yes" else "No"
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF6b7280),
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color(0xFF1f2937),
            modifier = Modifier.weight(2f)
        )
    }
}

@Composable
private fun RecordCard(record: com.nfcscanner.app.data.models.NFCRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = record.recordType.uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF6366f1)
                )
                
                IconButton(
                    onClick = { /* Handle copy */ },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = Color(0xFF6b7280),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = record.data,
                fontSize = 14.sp,
                color = Color(0xFF374151),
                lineHeight = 20.sp
            )
            
            if (record.encoding != null || record.language != null) {
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = buildString {
                        record.encoding?.let { append("Encoding: $it") }
                        if (record.encoding != null && record.language != null) {
                            append(" • ")
                        }
                        record.language?.let { append("Language: $it") }
                    },
                    fontSize = 12.sp,
                    color = Color(0xFF9ca3af)
                )
            }
        }
    }
}

@Composable
private fun ActionsSection(
    onEmulate: () -> Unit,
    onWrite: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onEmulate,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6366f1)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FlashOn,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Emulate Tag",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        Button(
            onClick = onWrite,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFf59e0b)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Write to Tag",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        Text(
            text = "Use emulation to share data or write to create a new tag",
            fontSize = 14.sp,
            color = Color(0xFF6b7280),
            lineHeight = 20.sp,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
    }
}

@Composable
private fun TagNotFoundState(onNavigateBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Smartphone,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color(0xFFd1d5db)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Tag Not Found",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF6b7280)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "The requested NFC tag could not be found in your history.",
            fontSize = 16.sp,
            color = Color(0xFF9ca3af),
            lineHeight = 24.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onNavigateBack,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6366f1)
            )
        ) {
            Text("Go Back")
        }
    }
}