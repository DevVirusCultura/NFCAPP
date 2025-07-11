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
import com.nfcscanner.app.ui.components.TagCard
import com.nfcscanner.app.viewmodel.MainViewModel

/**
 * Schermata cronologia con la stessa grafica dell'app React Native
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: MainViewModel,
    onTagClick: (String) -> Unit
) {
    val allTags by viewModel.allTags.observeAsState(emptyList())
    val filteredTags by viewModel.filteredTags.observeAsState(emptyList())
    val searchQuery by viewModel.searchQuery.observeAsState("")
    
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
            
            // Header
            item {
                Column {
                    Text(
                        text = "Scan History",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1f2937)
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "${allTags.size} tag${if (allTags.size != 1) "s" else ""} scanned" +
                                if (searchQuery.isNotEmpty()) " • ${filteredTags.size} matching" else "",
                        fontSize = 16.sp,
                        color = Color(0xFF6b7280)
                    )
                }
            }
            
            // Search Bar
            item {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.setSearchQuery(it) }
                )
            }
            
            // Controls
            item {
                ControlsSection(
                    tagCount = allTags.size,
                    onExport = { /* Handle export */ },
                    onImport = { /* Handle import */ },
                    onClearAll = { /* Handle clear all */ }
                )
            }
            
            // Tags List
            if (filteredTags.isNotEmpty()) {
                items(filteredTags) { tag ->
                    TagCard(
                        tag = tag,
                        onClick = { onTagClick(tag.id) }
                    )
                }
            } else {
                item {
                    EmptyHistoryState(hasSearch = searchQuery.isNotEmpty())
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Search tags, records, or technologies...") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color(0xFF6b7280)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = Color(0xFF6b7280)
                    )
                }
            }
        },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = Color(0xFF6366f1),
            unfocusedBorderColor = Color(0xFFf3f4f6)
        )
    )
}

@Composable
private fun ControlsSection(
    tagCount: Int,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onClearAll: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ControlButton(
            icon = Icons.Default.Download,
            text = "Export",
            enabled = tagCount > 0,
            onClick = onExport
        )
        
        ControlButton(
            icon = Icons.Default.Upload,
            text = "Import",
            onClick = onImport
        )
        
        if (tagCount > 0) {
            ControlButton(
                icon = Icons.Default.Delete,
                text = "Clear",
                isDestructive = true,
                onClick = onClearAll
            )
        }
    }
}

@Composable
private fun ControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    enabled: Boolean = true,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isDestructive) Color(0xFFfef2f2) else Color.White,
            contentColor = if (isDestructive) Color(0xFFef4444) else Color(0xFF6366f1),
            disabledContainerColor = Color.White,
            disabledContentColor = Color(0xFF9ca3af)
        ),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isDestructive) Color(0xFFfecaca) else Color(0xFFf3f4f6)
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun EmptyHistoryState(hasSearch: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Smartphone,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color(0xFFd1d5db)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = if (hasSearch) "No Matching Tags" else "No Tags Scanned",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF6b7280)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = if (hasSearch) 
                "Try adjusting your search terms" 
            else 
                "Start scanning NFC tags to see them appear here",
            fontSize = 16.sp,
            color = Color(0xFF9ca3af),
            lineHeight = 24.sp
        )
    }
}