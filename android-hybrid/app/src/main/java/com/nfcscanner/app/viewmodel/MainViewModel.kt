package com.nfcscanner.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfcscanner.app.data.models.*
import com.nfcscanner.app.data.repository.NFCRepository
import com.nfcscanner.app.nfc.NFCManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel principale per gestione stato app 2025
 * Aggiornato con le ultime best practices Android
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: NFCRepository,
    private val nfcManager: NFCManager
) : ViewModel() {
    
    // ==================== NFC STATE ====================
    
    private val _nfcSupported = MutableLiveData<Boolean>(false)
    val nfcSupported: LiveData<Boolean> = _nfcSupported
    
    private val _nfcEnabled = MutableLiveData<Boolean>(false)
    val nfcEnabled: LiveData<Boolean> = _nfcEnabled
    
    private val _isScanningEnabled = MutableLiveData<Boolean>(false)
    val isScanningEnabled: LiveData<Boolean> = _isScanningEnabled
    
    private val _scannerState = MutableLiveData<NFCScannerState>(NFCScannerState.IDLE)
    val scannerState: LiveData<NFCScannerState> = _scannerState
    
    // ==================== TAG DATA ====================
    
    private val _allTags = MutableLiveData<List<NFCTag>>(emptyList())
    val allTags: LiveData<List<NFCTag>> = _allTags
    
    private val _filteredTags = MutableLiveData<List<NFCTag>>(emptyList())
    val filteredTags: LiveData<List<NFCTag>> = _filteredTags
    
    private val _selectedTag = MutableLiveData<NFCTag?>(null)
    val selectedTag: LiveData<NFCTag?> = _selectedTag
    
    private val _searchQuery = MutableLiveData("")
    val searchQuery: LiveData<String> = _searchQuery
    
    // ==================== APP STATE ====================
    
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage
    
    private val _appSettings = MutableLiveData(AppSettings())
    val appSettings: LiveData<AppSettings> = _appSettings
    
    private val _stats = MutableLiveData(NFCStats())
    val stats: LiveData<NFCStats> = _stats
    
    init {
        loadInitialData()
        observeNFCManager()
    }
    
    // ==================== INITIALIZATION ====================
    
    private fun loadInitialData() {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                // Carica tags
                repository.getAllTags().collect { tags ->
                    _allTags.value = tags
                    filterTags()
                }
                
                // Carica impostazioni
                _appSettings.value = repository.getSettings()
                
                // Carica statistiche
                _stats.value = repository.getStats()
                
            } catch (e: Exception) {
                _errorMessage.value = "Error loading data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun observeNFCManager() {
        viewModelScope.launch {
            // Osserva stato scanner
            nfcManager.scannerState.observeForever { state ->
                _scannerState.value = state
            }
            
            // Osserva risultati scansione
            nfcManager.nfcResult.observeForever { result ->
                when (result) {
                    is NFCResult.Success -> {
                        handleTagScanned(result.data)
                    }
                    is NFCResult.Error -> {
                        _errorMessage.value = "Scan error: ${result.exception.message}"
                    }
                    is NFCResult.Loading -> {
                        _isLoading.value = true
                    }
                }
            }
        }
    }
    
    // ==================== NFC OPERATIONS ====================
    
    fun setNFCSupported(supported: Boolean) {
        _nfcSupported.value = supported
    }
    
    fun setNFCEnabled(enabled: Boolean) {
        _nfcEnabled.value = enabled
    }
    
    fun setScanningEnabled(enabled: Boolean) {
        _isScanningEnabled.value = enabled
    }
    
    fun startScanning() {
        _isScanningEnabled.value = true
        _scannerState.value = NFCScannerState.SCANNING
    }
    
    fun stopScanning() {
        _isScanningEnabled.value = false
        _scannerState.value = NFCScannerState.IDLE
    }
    
    private fun handleTagScanned(tag: NFCTag) {
        viewModelScope.launch {
            try {
                // Salva tag se auto-save è abilitato
                if (_appSettings.value.autoSave) {
                    repository.insertTag(tag)
                }
                
                _selectedTag.value = tag
                _errorMessage.value = null
                
            } catch (e: Exception) {
                _errorMessage.value = "Error saving tag: ${e.message}"
            }
        }
    }
    
    // ==================== TAG MANAGEMENT ====================
    
    suspend fun getTagById(tagId: String): NFCTag? {
        return repository.getTagById(tagId)
    }
    
    fun selectTag(tag: NFCTag) {
        _selectedTag.value = tag
    }
    
    fun deleteTag(tagId: String) {
        viewModelScope.launch {
            try {
                repository.deleteTagById(tagId)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Error deleting tag: ${e.message}"
            }
        }
    }
    
    fun deleteAllTags() {
        viewModelScope.launch {
            try {
                repository.deleteAllTags()
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Error deleting all tags: ${e.message}"
            }
        }
    }
    
    fun updateTag(tag: NFCTag) {
        viewModelScope.launch {
            try {
                repository.updateTag(tag)
                _selectedTag.value = tag
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Error updating tag: ${e.message}"
            }
        }
    }
    
    // ==================== SEARCH & FILTER ====================
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        filterTags()
    }
    
    private fun filterTags() {
        val query = _searchQuery.value?.lowercase() ?: ""
        val allTags = _allTags.value ?: emptyList()
        
        _filteredTags.value = if (query.isBlank()) {
            allTags
        } else {
            allTags.filter { tag ->
                tag.id.lowercase().contains(query) ||
                tag.type?.lowercase()?.contains(query) == true ||
                tag.uid?.lowercase()?.contains(query) == true ||
                tag.records.any { record ->
                    record.data.lowercase().contains(query) ||
                    record.recordType.lowercase().contains(query)
                }
            }
        }
    }
    
    // ==================== SETTINGS ====================
    
    fun updateSettings(settings: AppSettings) {
        viewModelScope.launch {
            try {
                repository.updateSettings(settings)
                _appSettings.value = settings
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Error updating settings: ${e.message}"
            }
        }
    }
    
    // ==================== EXPORT/IMPORT ====================
    
    fun exportData(): String? {
        return try {
            viewModelScope.launch {
                repository.exportData()
            }
            "Export completed successfully"
        } catch (e: Exception) {
            _errorMessage.value = "Export error: ${e.message}"
            null
        }
    }
    
    fun importData(jsonData: String) {
        viewModelScope.launch {
            try {
                val importedCount = repository.importData(jsonData)
                _errorMessage.value = null
                // Ricarica dati dopo import
                loadInitialData()
            } catch (e: Exception) {
                _errorMessage.value = "Import error: ${e.message}"
            }
        }
    }
    
    // ==================== ERROR HANDLING ====================
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    // ==================== EMULATION ====================
    
    fun startEmulation(tag: NFCTag) {
        viewModelScope.launch {
            try {
                // Implementa logica di emulazione
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Emulation error: ${e.message}"
            }
        }
    }
    
    fun stopEmulation() {
        // Implementa stop emulazione
    }
}