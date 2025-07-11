interface NFCTag {
  id: string;
  timestamp: string;
  records: Array<{
    recordType: string;
    data: string;
    encoding?: string;
    language?: string;
  }>;
  technologies?: string[];
  type?: string;
  uid?: string;
  size?: number;
  isWritable?: boolean;
  maxSize?: number;
  isVirtual?: boolean;
}

interface NFCWriteData {
  records: Array<{
    recordType: string;
    data: string;
    encoding?: string;
    language?: string;
  }>;
}

class NFCManagerClass {
  private storageKey = 'nfc_scan_history';
  private settingsKey = 'nfc_settings';
  private statsKey = 'nfc_stats';
  private reader: any = null;
  private writer: any = null;
  private isScanning = false;
  private isWriting = false;

  async isNFCSupported(): Promise<boolean> {
    if (typeof window !== 'undefined' && 'NDEFReader' in window) {
      return true;
    }
    return false;
  }

  async isNFCEnabled(): Promise<boolean> {
    try {
      if (typeof window !== 'undefined' && 'NDEFReader' in window) {
        // Try to create an NDEFReader to check if NFC is available
        // Note: This doesn't guarantee NFC is enabled, just that the API is available
        const ndef = new (window as any).NDEFReader();
        return true;
      }
    } catch (error) {
      console.error('NFC check failed:', error);
    }
    return false;
  }

  async requestNFCPermission(): Promise<boolean> {
    // Request permission to use NFC
    try {
      if (typeof window !== 'undefined' && 'NDEFReader' in window) {
        const ndef = new (window as any).NDEFReader();
        await ndef.scan();
        return true;
      }
    } catch (error) {
      console.error('NFC permission request failed:', error);
    }
    return false;
  }

  async startScan(): Promise<NFCTag> {
    // Start scanning for NFC tags
    if (typeof window === 'undefined' || !('NDEFReader' in window)) {
      throw new Error('NFC not supported on this device. Please use Chrome on Android with NFC enabled.');
    }

    if (this.isScanning) {
      throw new Error('Scan already in progress');
    }

    return new Promise((resolve, reject) => {
      this.reader = new (window as any).NDEFReader();
      this.isScanning = true;
      
      const timeout = setTimeout(() => {
        this.stopScan();
        reject(new Error('Scan timeout after 30 seconds. Please try again.'));
      }, 30000); // 30 second timeout

      this.reader.scan().then(() => {
        console.log('NFC scan started successfully');
        
        this.reader.addEventListener('reading', ({ message, serialNumber }: any) => {
          clearTimeout(timeout);
          this.isScanning = false;
          
          const tagData: NFCTag = {
            id: serialNumber || `tag_${Date.now()}`,
            timestamp: new Date().toISOString(),
            uid: serialNumber,
            type: 'NFC Forum Type',
            technologies: ['NDEF'],
            size: message.records.length,
            isWritable: true,
            maxSize: 8192, // Default max size
            isVirtual: false,
            records: message.records.map((record: any) => {
              let data = '';
              let encoding = 'UTF-8';
              let language = 'en';
              
              try {
                // Handle different record types
                if (record.recordType === 'text') {
                  const textDecoder = new TextDecoder();
                  const payload = new Uint8Array(record.data);
                  
                  if (payload.length > 0) {
                    // First byte contains encoding and language info
                    const statusByte = payload[0];
                    const isUTF16 = (statusByte & 0x80) !== 0;
                    const languageLength = statusByte & 0x3F;
                    
                    encoding = isUTF16 ? 'UTF-16' : 'UTF-8';
                    
                    if (languageLength > 0 && payload.length > languageLength + 1) {
                      language = textDecoder.decode(payload.slice(1, 1 + languageLength));
                      data = textDecoder.decode(payload.slice(1 + languageLength));
                    } else {
                      data = textDecoder.decode(payload.slice(1));
                    }
                  }
                } else if (record.recordType === 'url') {
                  const textDecoder = new TextDecoder();
                  data = textDecoder.decode(record.data);
                } else {
                  // For other types, try to decode as text
                  try {
                    data = new TextDecoder().decode(record.data);
                  } catch {
                    // If text decoding fails, show as hex
                    data = Array.from(new Uint8Array(record.data))
                      .map(b => b.toString(16).padStart(2, '0'))
                      .join(' ');
                  }
                }
              } catch (error) {
                console.error('Error decoding record:', error);
                data = 'Unable to decode data';
              }
              
              return {
                recordType: record.recordType || 'unknown',
                data: data || 'No data',
                encoding,
                language,
              };
            }),
          };
          
          this.saveTagToHistory(tagData);
          this.updateScanStats();
          resolve(tagData);
        });

        this.reader.addEventListener('readingerror', (error: any) => {
          clearTimeout(timeout);
          this.isScanning = false;
          console.error('NFC reading error:', error);
          reject(new Error('Failed to read NFC tag. Please try again.'));
        });

      }).catch((error: any) => {
        clearTimeout(timeout);
        this.isScanning = false;
        console.error('NFC scan start failed:', error);
        
        if (error.name === 'NotAllowedError') {
          reject(new Error('NFC permission denied. Please allow NFC access and try again.'));
        } else if (error.name === 'NotSupportedError') {
          reject(new Error('NFC not supported on this device.'));
        } else {
          reject(new Error(`NFC scan failed: ${error.message}`));
        }
      });
    });
  }

  stopScan(): void {
    // Stop the current NFC scan
    if (this.reader && this.isScanning) {
      try {
        // Note: Web NFC API doesn't have a direct stop method
        // The scan will stop when the reader is garbage collected
        this.reader = null;
        this.isScanning = false;
      } catch (error) {
        console.error('Error stopping scan:', error);
      }
    }
  }

  async writeTag(data: NFCWriteData): Promise<void> {
    // Write data to an NFC tag
    if (typeof window === 'undefined' || !('NDEFWriter' in window)) {
      throw new Error('NFC writing not supported on this device');
    }

    if (this.isWriting) {
      throw new Error('Write operation already in progress');
    }

    try {
      this.isWriting = true;
      this.writer = new (window as any).NDEFWriter();
      
      const message = {
        records: data.records.map(record => ({
          recordType: record.recordType,
          data: record.data,
          encoding: record.encoding || 'utf-8',
          lang: record.language || 'en',
        })),
      };
      
      await this.writer.write(message);
      this.isWriting = false;
    } catch (error) {
      this.isWriting = false;
      throw new Error(`Failed to write tag: ${(error as Error).message}`);
    }
  }

  async emulateTag(tag: NFCTag): Promise<void> {
    // Emulate an existing NFC tag
    if (typeof window === 'undefined' || !('NDEFWriter' in window)) {
      throw new Error('NFC emulation not supported on this device');
    }

    try {
      const writeData: NFCWriteData = {
        records: tag.records.map(record => ({
          recordType: record.recordType,
          data: record.data,
          encoding: record.encoding || 'utf-8',
          language: record.language || 'en',
        })),
      };
      
      await this.writeTag(writeData);
    } catch (error) {
      throw new Error(`Failed to emulate tag: ${(error as Error).message}`);
    }
  }

  async saveTagToHistory(tag: NFCTag): Promise<void> {
    // Save a scanned tag to local history
    try {
      const history = await this.getHistory();
      const existingIndex = history.findIndex(t => t.id === tag.id);
      
      if (existingIndex >= 0) {
        // Update existing tag
        history[existingIndex] = { ...history[existingIndex], ...tag, timestamp: new Date().toISOString() };
      } else {
        // Add new tag to the beginning
        history.unshift(tag);
      }
      
      // Get max history size from settings
      const settings = await this.getSettings();
      const maxSize = settings.maxHistorySize || 100;
      
      const limitedHistory = history.slice(0, maxSize);
      
      if (typeof window !== 'undefined') {
        localStorage.setItem(this.storageKey, JSON.stringify(limitedHistory));
      }
    } catch (error) {
      console.error('Failed to save tag to history:', error);
    }
  }

  async getHistory(): Promise<NFCTag[]> {
    // Retrieve scan history from local storage
    try {
      if (typeof window !== 'undefined') {
        const stored = localStorage.getItem(this.storageKey);
        return stored ? JSON.parse(stored) : [];
      }
      return [];
    } catch (error) {
      console.error('Failed to get history:', error);
      return [];
    }
  }

  async deleteTag(tagId: string): Promise<void> {
    // Delete a specific tag from history
    try {
      const history = await this.getHistory();
      const updatedHistory = history.filter(tag => tag.id !== tagId);
      
      if (typeof window !== 'undefined') {
        localStorage.setItem(this.storageKey, JSON.stringify(updatedHistory));
      }
    } catch (error) {
      console.error('Failed to delete tag:', error);
      throw error;
    }
  }

  async clearHistory(): Promise<void> {
    // Clear all scan history
    try {
      if (typeof window !== 'undefined') {
        localStorage.removeItem(this.storageKey);
      }
    } catch (error) {
      console.error('Failed to clear history:', error);
      throw error;
    }
  }

  async exportHistory(): Promise<string> {
    // Export scan history as JSON
    try {
      const history = await this.getHistory();
      return JSON.stringify({
        exportDate: new Date().toISOString(),
        version: '1.0.0',
        totalTags: history.length,
        tags: history,
        stats: await this.getStats(),
      }, null, 2);
    } catch (error) {
      console.error('Failed to export history:', error);
      throw error;
    }
  }

  async importHistory(jsonData: string): Promise<number> {
    // Import scan history from JSON
    try {
      const importData = JSON.parse(jsonData);
      
      if (!importData.tags || !Array.isArray(importData.tags)) {
        throw new Error('Invalid import data format');
      }
      
      const currentHistory = await this.getHistory();
      const importedTags = importData.tags as NFCTag[];
      
      // Merge with existing history, avoiding duplicates
      const mergedHistory = [...currentHistory];
      let importedCount = 0;
      
      for (const tag of importedTags) {
        if (!mergedHistory.find(t => t.id === tag.id)) {
          mergedHistory.push(tag);
          importedCount++;
        }
      }
      
      // Sort by timestamp (newest first)
      mergedHistory.sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime());
      
      if (typeof window !== 'undefined') {
        localStorage.setItem(this.storageKey, JSON.stringify(mergedHistory));
      }
      
      return importedCount;
    } catch (error) {
      console.error('Failed to import history:', error);
      throw error;
    }
  }

  async getSettings(): Promise<any> {
    // Get app settings
    try {
      if (typeof window !== 'undefined') {
        const stored = localStorage.getItem(this.settingsKey);
        return stored ? JSON.parse(stored) : {
          notifications: true,
          autoSave: true,
          vibration: true,
          soundEnabled: true,
          theme: 'auto',
          scanTimeout: 30,
          maxHistorySize: 100,
        };
      }
      return {};
    } catch (error) {
      console.error('Failed to get settings:', error);
      return {};
    }
  }

  async saveSettings(settings: any): Promise<void> {
    // Save app settings
    try {
      if (typeof window !== 'undefined') {
        localStorage.setItem(this.settingsKey, JSON.stringify(settings));
      }
    } catch (error) {
      console.error('Failed to save settings:', error);
      throw error;
    }
  }

  async getStats(): Promise<any> {
    // Get usage statistics
    try {
      if (typeof window !== 'undefined') {
        const stored = localStorage.getItem(this.statsKey);
        return stored ? JSON.parse(stored) : {
          totalScans: 0,
          firstScanDate: null,
          lastScanDate: null,
          tagsPerDay: {},
          recordTypes: {},
        };
      }
      return {};
    } catch (error) {
      console.error('Failed to get stats:', error);
      return {};
    }
  }

  async updateScanStats(): Promise<void> {
    // Update usage statistics after a scan
    try {
      const stats = await this.getStats();
      const today = new Date().toISOString().split('T')[0];
      
      stats.totalScans = (stats.totalScans || 0) + 1;
      stats.lastScanDate = new Date().toISOString();
      if (!stats.firstScanDate) {
        stats.firstScanDate = new Date().toISOString();
      }
      stats.tagsPerDay[today] = (stats.tagsPerDay[today] || 0) + 1;
      
      await this.saveStats(stats);
    } catch (error) {
      console.error('Failed to update scan stats:', error);
    }
  }

  async saveStats(stats: any): Promise<void> {
    // Save usage statistics
    try {
      if (typeof window !== 'undefined') {
        localStorage.setItem(this.statsKey, JSON.stringify(stats));
      }
    } catch (error) {
      console.error('Failed to save stats:', error);
    }
  }

  getIsScanning(): boolean {
    return this.isScanning;
  }

  getIsWriting(): boolean {
    return this.isWriting;
  }
}

export const NFCManager = new NFCManagerClass();
export type { NFCTag, NFCWriteData };