import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  FlatList,
  Platform,
  Alert,
  RefreshControl,
  TextInput,
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { Zap, Play, Square, Smartphone, Search, Plus, FileText, Wifi, Settings as SettingsIcon } from 'lucide-react-native';
import { NFCManager, NFCTag } from '@/components/NFCManager';
import { TagCard } from '@/components/TagCard';
import { router } from 'expo-router';

export default function EmulatorScreen() {
  const [tags, setTags] = useState<NFCTag[]>([]);
  const [filteredTags, setFilteredTags] = useState<NFCTag[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [emulatingTag, setEmulatingTag] = useState<NFCTag | null>(null);
  const [isEmulating, setIsEmulating] = useState(false);

  useEffect(() => {
    loadTags();
  }, []);

  useEffect(() => {
    filterTags();
  }, [searchQuery, tags]);

  const loadTags = async () => {
    try {
      const history = await NFCManager.getHistory();
      setTags(history);
    } catch (error) {
      console.error('Failed to load tags:', error);
      Alert.alert('Error', 'Failed to load tags for emulation');
    }
  };

  const onRefresh = async () => {
    setIsRefreshing(true);
    await loadTags();
    setIsRefreshing(false);
  };

  const filterTags = () => {
    if (searchQuery.trim()) {
      const query = searchQuery.toLowerCase();
      const filtered = tags.filter(tag => 
        tag.id.toLowerCase().includes(query) ||
        tag.type?.toLowerCase().includes(query) ||
        tag.uid?.toLowerCase().includes(query) ||
        tag.records.some(record => 
          record.data.toLowerCase().includes(query) ||
          record.recordType.toLowerCase().includes(query)
        )
      );
      setFilteredTags(filtered);
    } else {
      setFilteredTags(tags);
    }
  };

  const startEmulation = async (tag: NFCTag) => {
    if (isEmulating) {
      Alert.alert('Emulation Active', 'Please stop the current emulation before starting a new one.');
      return;
    }

    setIsEmulating(true);
    setEmulatingTag(tag);

    try {
      await NFCManager.emulateTag(tag);
      Alert.alert(
        'Emulation Started',
        `Now emulating tag "${tag.id}". Hold your device near another NFC-enabled device to transfer the data.`,
        [
          { text: 'Stop Emulation', onPress: stopEmulation },
          { text: 'Keep Running', style: 'cancel' }
        ]
      );
    } catch (error) {
      console.error('Emulation failed:', error);
      setIsEmulating(false);
      setEmulatingTag(null);
      Alert.alert('Emulation Failed', (error as Error).message);
    }
  };

  const stopEmulation = () => {
    try {
      NFCManager.stopScan(); // This also stops emulation
      setIsEmulating(false);
      setEmulatingTag(null);
      Alert.alert('Emulation Stopped', 'Tag emulation has been stopped.');
    } catch (error) {
      console.error('Failed to stop emulation:', error);
    }
  };

  const createVirtualTag = () => {
    Alert.alert(
      'Create Virtual Tag',
      'Choose how to create a new virtual tag:',
      [
        { text: 'Cancel', style: 'cancel' },
        { text: 'Text Message', onPress: () => createTextTag() },
        { text: 'URL', onPress: () => createUrlTag() },
        { text: 'Custom', onPress: () => createCustomTag() }
      ]
    );
  };

  const createTextTag = () => {
    Alert.prompt(
      'Create Text Tag',
      'Enter the text content for the virtual tag:',
      [
        { text: 'Cancel', style: 'cancel' },
        {
          text: 'Create',
          onPress: async (text) => {
            if (text && text.trim()) {
              const virtualTag: NFCTag = {
                id: `virtual_text_${Date.now()}`,
                timestamp: new Date().toISOString(),
                type: 'Virtual Text Tag',
                technologies: ['NDEF'],
                isWritable: true,
                records: [{
                  recordType: 'text',
                  data: text.trim(),
                  encoding: 'UTF-8',
                  language: 'en'
                }]
              };
              
              await NFCManager.saveTagToHistory(virtualTag);
              await loadTags();
              Alert.alert('Success', 'Virtual text tag created successfully!');
            }
          }
        }
      ],
      'plain-text'
    );
  };

  const createUrlTag = () => {
    Alert.prompt(
      'Create URL Tag',
      'Enter the URL for the virtual tag:',
      [
        { text: 'Cancel', style: 'cancel' },
        {
          text: 'Create',
          onPress: async (url) => {
            if (url && url.trim()) {
              const virtualTag: NFCTag = {
                id: `virtual_url_${Date.now()}`,
                timestamp: new Date().toISOString(),
                type: 'Virtual URL Tag',
                technologies: ['NDEF'],
                isWritable: true,
                records: [{
                  recordType: 'url',
                  data: url.trim(),
                  encoding: 'UTF-8',
                  language: 'en'
                }]
              };
              
              await NFCManager.saveTagToHistory(virtualTag);
              await loadTags();
              Alert.alert('Success', 'Virtual URL tag created successfully!');
            }
          }
        }
      ],
      'url'
    );
  };

  const createCustomTag = () => {
    // For now, create a simple custom tag - this could be expanded to a full form
    const virtualTag: NFCTag = {
      id: `virtual_custom_${Date.now()}`,
      timestamp: new Date().toISOString(),
      type: 'Virtual Custom Tag',
      technologies: ['NDEF'],
      isWritable: true,
      records: [{
        recordType: 'text',
        data: 'Custom virtual tag created in NFC Scanner',
        encoding: 'UTF-8',
        language: 'en'
      }]
    };
    
    NFCManager.saveTagToHistory(virtualTag);
    loadTags();
    Alert.alert('Success', 'Virtual custom tag created! You can edit it in the details view.');
  };

  const renderTagItem = ({ item }: { item: NFCTag }) => (
    <View style={styles.tagContainer}>
      <TagCard
        tag={item}
        onPress={() => router.push(`/(tabs)/details/${item.id}`)}
      />
      <TouchableOpacity
        style={[
          styles.emulateButton,
          isEmulating && emulatingTag?.id === item.id && styles.emulatingButton
        ]}
        onPress={() => {
          if (isEmulating && emulatingTag?.id === item.id) {
            stopEmulation();
          } else {
            startEmulation(item);
          }
        }}
        disabled={isEmulating && emulatingTag?.id !== item.id}
      >
        {isEmulating && emulatingTag?.id === item.id ? (
          <>
            <Square size={16} color="#ffffff" />
            <Text style={styles.emulateButtonText}>Stop</Text>
          </>
        ) : (
          <>
            <Play size={16} color="#ffffff" />
            <Text style={styles.emulateButtonText}>Emulate</Text>
          </>
        )}
      </TouchableOpacity>
    </View>
  );

  const renderEmptyState = () => (
    <View style={styles.emptyState}>
      <Zap size={64} color="#d1d5db" strokeWidth={1} />
      <Text style={styles.emptyTitle}>
        {searchQuery ? 'No Matching Tags' : 'No Tags Available'}
      </Text>
      <Text style={styles.emptySubtitle}>
        {searchQuery 
          ? 'Try adjusting your search terms'
          : 'Scan some NFC tags first or create virtual tags to start emulating'
        }
      </Text>
      {!searchQuery && (
        <TouchableOpacity style={styles.createButton} onPress={createVirtualTag}>
          <Plus size={20} color="#ffffff" />
          <Text style={styles.createButtonText}>Create Virtual Tag</Text>
        </TouchableOpacity>
      )}
    </View>
  );

  const renderHeader = () => (
    <View style={styles.headerContainer}>
      <View style={styles.header}>
        <Text style={styles.title}>NFC Emulator</Text>
        <Text style={styles.subtitle}>
          {isEmulating 
            ? `Emulating: ${emulatingTag?.id}`
            : `${tags.length} tag${tags.length !== 1 ? 's' : ''} available`
          }
          {searchQuery && ` • ${filteredTags.length} matching`}
        </Text>
      </View>

      {isEmulating && (
        <View style={styles.emulationStatus}>
          <View style={styles.emulationIndicator}>
            <Zap size={20} color="#10b981" />
            <Text style={styles.emulationText}>
              Emulating "{emulatingTag?.id}"
            </Text>
          </View>
          <TouchableOpacity style={styles.stopButton} onPress={stopEmulation}>
            <Square size={16} color="#ef4444" />
            <Text style={styles.stopButtonText}>Stop</Text>
          </TouchableOpacity>
        </View>
      )}

      <View style={styles.searchContainer}>
        <View style={styles.searchInputContainer}>
          <Search size={20} color="#6b7280" />
          <TextInput
            style={styles.searchInput}
            placeholder="Search tags to emulate..."
            value={searchQuery}
            onChangeText={setSearchQuery}
            placeholderTextColor="#9ca3af"
          />
        </View>
      </View>

      <View style={styles.controlsContainer}>
        <TouchableOpacity style={styles.controlButton} onPress={createVirtualTag}>
          <Plus size={16} color="#6366f1" />
          <Text style={styles.controlButtonText}>Create Virtual</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={styles.controlButton}
          onPress={() => router.push('/(tabs)/settings')}
        >
          <SettingsIcon size={16} color="#6366f1" />
          <Text style={styles.controlButtonText}>Settings</Text>
        </TouchableOpacity>
      </View>
    </View>
  );

  return (
    <LinearGradient
      colors={['#f8fafc', '#e2e8f0']}
      style={styles.container}
    >
      <FlatList
        data={filteredTags}
        renderItem={renderTagItem}
        keyExtractor={(item) => item.id}
        contentContainerStyle={styles.listContainer}
        showsVerticalScrollIndicator={false}
        ListHeaderComponent={renderHeader}
        ListEmptyComponent={renderEmptyState}
        refreshControl={
          <RefreshControl
            refreshing={isRefreshing}
            onRefresh={onRefresh}
            colors={['#6366f1']}
            tintColor="#6366f1"
          />
        }
      />
    </LinearGradient>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    paddingTop: Platform.OS === 'ios' ? 60 : 40,
  },
  headerContainer: {
    paddingHorizontal: 20,
    marginBottom: 20,
  },
  header: {
    marginBottom: 20,
  },
  title: {
    fontSize: 32,
    fontFamily: 'Inter-Bold',
    color: '#1f2937',
    marginBottom: 4,
  },
  subtitle: {
    fontSize: 16,
    fontFamily: 'Inter-Regular',
    color: '#6b7280',
  },
  emulationStatus: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    backgroundColor: '#f0fdf4',
    borderRadius: 12,
    padding: 16,
    marginBottom: 16,
    borderWidth: 1,
    borderColor: '#bbf7d0',
  },
  emulationIndicator: {
    flexDirection: 'row',
    alignItems: 'center',
    flex: 1,
  },
  emulationText: {
    fontSize: 16,
    fontFamily: 'Inter-Medium',
    color: '#059669',
    marginLeft: 8,
  },
  stopButton: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#fef2f2',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: '#fecaca',
  },
  stopButtonText: {
    fontSize: 14,
    fontFamily: 'Inter-Medium',
    color: '#ef4444',
    marginLeft: 4,
  },
  searchContainer: {
    marginBottom: 16,
  },
  searchInputContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#ffffff',
    borderRadius: 12,
    paddingHorizontal: 16,
    paddingVertical: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
    borderWidth: 1,
    borderColor: '#f3f4f6',
  },
  searchInput: {
    flex: 1,
    marginLeft: 12,
    fontSize: 16,
    fontFamily: 'Inter-Regular',
    color: '#1f2937',
  },
  controlsContainer: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  controlButton: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#ffffff',
    borderRadius: 8,
    paddingHorizontal: 12,
    paddingVertical: 8,
    marginRight: 8,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 2,
    elevation: 1,
    borderWidth: 1,
    borderColor: '#f3f4f6',
  },
  controlButtonText: {
    fontSize: 14,
    fontFamily: 'Inter-Medium',
    color: '#6366f1',
    marginLeft: 4,
  },
  listContainer: {
    paddingHorizontal: 20,
    paddingBottom: 100,
  },
  tagContainer: {
    marginBottom: 12,
  },
  emulateButton: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#6366f1',
    borderRadius: 8,
    paddingVertical: 12,
    paddingHorizontal: 16,
    marginTop: 8,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.2,
    shadowRadius: 4,
    elevation: 3,
  },
  emulatingButton: {
    backgroundColor: '#ef4444',
  },
  emulateButtonText: {
    fontSize: 16,
    fontFamily: 'Inter-SemiBold',
    color: '#ffffff',
    marginLeft: 6,
  },
  emptyState: {
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 80,
  },
  emptyTitle: {
    fontSize: 20,
    fontFamily: 'Inter-SemiBold',
    color: '#6b7280',
    marginTop: 16,
    marginBottom: 8,
  },
  emptySubtitle: {
    fontSize: 16,
    fontFamily: 'Inter-Regular',
    color: '#9ca3af',
    textAlign: 'center',
    lineHeight: 24,
    marginBottom: 20,
  },
  createButton: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#6366f1',
    borderRadius: 12,
    paddingVertical: 12,
    paddingHorizontal: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.2,
    shadowRadius: 4,
    elevation: 3,
  },
  createButtonText: {
    fontSize: 16,
    fontFamily: 'Inter-SemiBold',
    color: '#ffffff',
    marginLeft: 8,
  },
});