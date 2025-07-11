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
 * Schermata emulatore con la stessa grafica dell'app React Native
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmulatorScreen(
    viewModel: MainViewModel,
    onTagClick: (String) -> Unit
) {
    val allTags by viewModel.allTags.observeAsState(emptyList())
    val filteredTags by viewModel.filteredTags.observeAsState(emptyList())
    val searchQuery by viewModel.searchQuery.observeAsState("")
    var isEmulating by remember { mutableStateOf(false) }
    var emulatingTagId by remember { mutableStateOf<String?>(null) }
    
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
                HeaderSection(
                    tagCount = allTags.size,
                    filteredCount = filteredTags.size,
                    searchQuery = searchQuery,
                    isEmulating = isEmulating,
                    emulatingTagId = emulatingTagId
                )
            }
            
            // Emulation Status
            if (isEmulating && emulatingTagId != null) {
                item {
                    EmulationStatusCard(
                        tagId = emulatingTagId!!,
                        onStop = {
                            isEmulating = false
                            emulatingTagId = null
                        }
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
                    onCreateVirtual = { /* Handle create virtual */ }
                )
            }
            
            // Tags List
            if (filteredTags.isNotEmpty()) {
                items(filteredTags) { tag ->
                    TagCardWithEmulation(
                        tag = tag,
                        isEmulating = isEmulating && emulatingTagId == tag.id,
                        onTagClick = { onTagClick(tag.id) },
                        onEmulate = {
                            if (isEmulating && emulatingTagId == tag.id) {
                                // Stop emulation
                                isEmulating = false
                                emulatingTagId = null
                            } else {
                                // Start emulation
                                isEmulating = true
                                emulatingTagId = tag.id
                            }
                        }
                    )
                }
            } else {
                item {
                    EmptyEmulatorState(hasSearch = searchQuery.isNotEmpty())
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun HeaderSection(
    tagCount: Int,
    filteredCount: Int,
    searchQuery: String,
    isEmulating: Boolean,
    emulatingTagId: String?
) {
    Column {
        Text(
            text = "NFC Emulator",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1f2937)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = if (isEmulating) {
                "Emulating: $emulatingTagId"
            } else {
                "$tagCount tag${if (tagCount != 1) "s" else ""} available" +
                        if (searchQuery.isNotEmpty()) " • $filteredCount matching" else ""
            },
            fontSize = 16.sp,
            color = Color(0xFF6b7280)
        )
    }
}

@Composable
private fun EmulationStatusCard(
    tagId: String,
    onStop: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFf0fdf4)
        ),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color(0xFFbbf7d0)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = null,
                    tint = Color(0xFF059669),
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Emulating \"$tagId\"",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF059669)
                )
            }
            
            Button(
                onClick = onStop,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFfef2f2),
                    contentColor = Color(0xFFef4444)
                ),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Color(0xFFfecaca)
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Stop",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
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
        placeholder = { Text("Search tags to emulate...") },
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
private fun ControlsSection(onCreateVirtual: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onCreateVirtual,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF6366f1)
            ),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                Color(0xFFf3f4f6)
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Create Virtual",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        Button(
            onClick = { /* Handle settings */ },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF6366f1)
            ),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                Color(0xFFf3f4f6)
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Settings",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun TagCardWithEmulation(
    tag: com.nfcscanner.app.data.models.NFCTag,
    isEmulating: Boolean,
    onTagClick: () -> Unit,
    onEmulate: () -> Unit
) {
    Column {
        TagCard(
            tag = tag,
            onClick = onTagClick
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Button(
            onClick = onEmulate,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isEmulating) Color(0xFFef4444) else Color(0xFF6366f1)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                imageVector = if (isEmulating) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isEmulating) "Stop" else "Emulate",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun EmptyEmulatorState(hasSearch: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.FlashOn,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color(0xFFd1d5db)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = if (hasSearch) "No Matching Tags" else "No Tags Available",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF6b7280)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = if (hasSearch) 
                "Try adjusting your search terms" 
            else 
                "Scan some NFC tags first or create virtual tags to start emulating",
            fontSize = 16.sp,
            color = Color(0xFF9ca3af),
            lineHeight = 24.sp
        )
        
        if (!hasSearch) {
            Spacer(modifier = Modifier.height(20.dp))
            
            Button(
                onClick = { /* Handle create virtual */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6366f1)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Create Virtual Tag",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}