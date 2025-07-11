import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  TouchableOpacity,
  TextInput,
  Platform,
  Alert,
  RefreshControl,
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { router } from 'expo-router';
import { Search, Trash2, Calendar, Smartphone, FileText, Filter, Download, Upload, Import as SortAsc, Dessert as SortDesc } from 'lucide-react-native';
import { NFCManager, NFCTag } from '@/components/NFCManager';
import { TagCard } from '@/components/TagCard';

type SortOption = 'newest' | 'oldest' | 'name' | 'records';

export default function HistoryScreen() {
  const [tags, setTags] = useState<NFCTag[]>([]);
  const [filteredTags, setFilteredTags] = useState<NFCTag[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [sortBy, setSortBy] = useState<SortOption>('newest');
  const [showSortOptions, setShowSortOptions] = useState(false);

  useEffect(() => {
    loadHistory();
  }, []);

  useEffect(() => {
    filterAndSortTags();
  }, [searchQuery, tags, sortBy]);

  const loadHistory = async () => {
    try {
      const history = await NFCManager.getHistory();
      setTags(history);
    } catch (error) {
      console.error('Failed to load history:', error);
      Alert.alert('Error', 'Failed to load scan history');
    }
  };

  const onRefresh = async () => {
    setIsRefreshing(true);
    await loadHistory();
    setIsRefreshing(false);
  };

  const filterAndSortTags = () => {
    let filtered = tags;

    // Apply search filter
    if (searchQuery.trim()) {
      const query = searchQuery.toLowerCase();
      filtered = tags.filter(tag => 
        tag.id.toLowerCase().includes(query) ||
        tag.type?.toLowerCase().includes(query) ||
        tag.uid?.toLowerCase().includes(query) ||
        tag.records.some(record => 
          record.data.toLowerCase().includes(query) ||
          record.recordType.toLowerCase().includes(query)
        ) ||
        tag.technologies?.some(tech => 
          tech.toLowerCase().includes(query)
        )
      );
    }

    // Apply sorting
    filtered.sort((a, b) => {
      switch (sortBy) {
        case 'newest':
          return new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime();
        case 'oldest':
          return new Date(a.timestamp).getTime() - new Date(b.timestamp).getTime();
        case 'name':
          return a.id.localeCompare(b.id);
        case 'records':
          return b.records.length - a.records.length;
        default:
          return 0;
      }
    });

    setFilteredTags(filtered);
  };

  const deleteTag = async (tagId: string) => {
    Alert.alert(
      'Delete Tag',
      'Are you sure you want to delete this tag from history?',
      [
        { text: 'Cancel', style: 'cancel' },
        {
          text: 'Delete',
          style: 'destructive',
          onPress: async () => {
            try {
              await NFCManager.deleteTag(tagId);
              await loadHistory();
            } catch (error) {
              console.error('Failed to delete tag:', error);
              Alert.alert('Error', 'Failed to delete tag');
            }
          },
        },
      ]
    );
  };

  const clearAllHistory = () => {
    Alert.alert(
      'Clear All History',
      'Are you sure you want to delete all scanned tags? This action cannot be undone.',
      [
        { text: 'Cancel', style: 'cancel' },
        {
          text: 'Clear All',
          style: 'destructive',
          onPress: async () => {
            try {
              await NFCManager.clearHistory();
              await loadHistory();
            } catch (error) {
              console.error('Failed to clear history:', error);
              Alert.alert('Error', 'Failed to clear history');
            }
          },
        },
      ]
    );
  };

  const exportHistory = async () => {
    try {
      const exportData = await NFCManager.exportHistory();
      
      if (Platform.OS === 'web') {
        const blob = new Blob([exportData], { type: 'application/json' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `nfc-history-${new Date().toISOString().split('T')[0]}.json`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
        
        Alert.alert('Export Complete', 'Your NFC history has been downloaded.');
      }
    } catch (error) {
      console.error('Export failed:', error);
      Alert.alert('Export Failed', 'Unable to export your data.');
    }
  };

  const importHistory = () => {
    if (Platform.OS === 'web') {
      const input = document.createElement('input');
      input.type = 'file';
      input.accept = '.json';
      input.onchange = async (e: any) => {
        const file = e.target.files[0];
        if (file) {
          try {
            const text = await file.text();
            const importedCount = await NFCManager.importHistory(text);
            await loadHistory();
            Alert.alert(
              'Import Complete', 
              `Successfully imported ${importedCount} new tags.`
            );
          } catch (error) {
            console.error('Import failed:', error);
            Alert.alert('Import Failed', 'Invalid file format or corrupted data.');
          }
        }
      };
      input.click();
    }
  };

  const getSortIcon = () => {
    switch (sortBy) {
      case 'newest':
      case 'records':
        return <SortDesc size={16} color="#6366f1" />;
      case 'oldest':
      case 'name':
        return <SortAsc size={16} color="#6366f1" />;
      default:
        return <SortAsc size={16} color="#6366f1" />;
    }
  };

  const getSortLabel = () => {
    switch (sortBy) {
      case 'newest': return 'Newest First';
      case 'oldest': return 'Oldest First';
      case 'name': return 'Name A-Z';
      case 'records': return 'Most Records';
      default: return 'Sort';
    }
  };

  const renderTagItem = ({ item }: { item: NFCTag }) => (
    <TagCard
      tag={item}
      onPress={() => router.push(`/(tabs)/details/${item.id}`)}
      onDelete={() => deleteTag(item.id)}
    />
  );

  const renderEmptyState = () => (
    <View style={styles.emptyState}>
      <Smartphone size={64} color="#d1d5db" strokeWidth={1} />
      <Text style={styles.emptyTitle}>
        {searchQuery ? 'No Matching Tags' : 'No Tags Scanned'}
      </Text>
      <Text style={styles.emptySubtitle}>
        {searchQuery 
          ? 'Try adjusting your search terms'
          : 'Start scanning NFC tags to see them appear here'
        }
      </Text>
    </View>
  );

  const renderSortOptions = () => {
    if (!showSortOptions) return null;

    const options: { key: SortOption; label: string }[] = [
      { key: 'newest', label: 'Newest First' },
      { key: 'oldest', label: 'Oldest First' },
      { key: 'name', label: 'Name A-Z' },
      { key: 'records', label: 'Most Records' },
    ];

    return (
      <View style={styles.sortOptionsContainer}>
        {options.map((option) => (
          <TouchableOpacity
            key={option.key}
            style={[
              styles.sortOption,
              sortBy === option.key && styles.sortOptionActive
            ]}
            onPress={() => {
              setSortBy(option.key);
              setShowSortOptions(false);
            }}
          >
            <Text style={[
              styles.sortOptionText,
              sortBy === option.key && styles.sortOptionTextActive
            ]}>
              {option.label}
            </Text>
          </TouchableOpacity>
        ))}
      </View>
    );
  };

  return (
    <LinearGradient
      colors={['#f8fafc', '#e2e8f0']}
      style={styles.container}
    >
      <View style={styles.header}>
        <Text style={styles.title}>Scan History</Text>
        <Text style={styles.subtitle}>
          {tags.length} tag{tags.length !== 1 ? 's' : ''} scanned
          {searchQuery && ` • ${filteredTags.length} matching`}
        </Text>
      </View>

      <View style={styles.searchContainer}>
        <View style={styles.searchInputContainer}>
          <Search size={20} color="#6b7280" />
          <TextInput
            style={styles.searchInput}
            placeholder="Search tags, records, or technologies..."
            value={searchQuery}
            onChangeText={setSearchQuery}
            placeholderTextColor="#9ca3af"
          />
        </View>
      </View>

      <View style={styles.controlsContainer}>
        <TouchableOpacity
          style={styles.controlButton}
          onPress={() => setShowSortOptions(!showSortOptions)}
        >
          {getSortIcon()}
          <Text style={styles.controlButtonText}>{getSortLabel()}</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={styles.controlButton}
          onPress={exportHistory}
          disabled={tags.length === 0}
        >
          <Download size={16} color={tags.length > 0 ? "#6366f1" : "#9ca3af"} />
          <Text style={[
            styles.controlButtonText,
            tags.length === 0 && styles.controlButtonTextDisabled
          ]}>
            Export
          </Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={styles.controlButton}
          onPress={importHistory}
        >
          <Upload size={16} color="#6366f1" />
          <Text style={styles.controlButtonText}>Import</Text>
        </TouchableOpacity>

        {tags.length > 0 && (
          <TouchableOpacity
            style={[styles.controlButton, styles.deleteButton]}
            onPress={clearAllHistory}
          >
            <Trash2 size={16} color="#ef4444" />
          </TouchableOpacity>
        )}
      </View>

      {renderSortOptions()}

      <FlatList
        data={filteredTags}
        renderItem={renderTagItem}
        keyExtractor={(item) => item.id}
        contentContainerStyle={styles.listContainer}
        showsVerticalScrollIndicator={false}
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
  header: {
    paddingHorizontal: 20,
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
  searchContainer: {
    paddingHorizontal: 20,
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
    paddingHorizontal: 20,
    marginBottom: 16,
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
  controlButtonTextDisabled: {
    color: '#9ca3af',
  },
  deleteButton: {
    backgroundColor: '#fef2f2',
    borderColor: '#fecaca',
  },
  sortOptionsContainer: {
    backgroundColor: '#ffffff',
    marginHorizontal: 20,
    marginBottom: 16,
    borderRadius: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
    borderWidth: 1,
    borderColor: '#f3f4f6',
  },
  sortOption: {
    paddingHorizontal: 16,
    paddingVertical: 12,
    borderBottomWidth: 1,
    borderBottomColor: '#f3f4f6',
  },
  sortOptionActive: {
    backgroundColor: '#eef2ff',
  },
  sortOptionText: {
    fontSize: 16,
    fontFamily: 'Inter-Regular',
    color: '#374151',
  },
  sortOptionTextActive: {
    fontFamily: 'Inter-Medium',
    color: '#6366f1',
  },
  listContainer: {
    paddingHorizontal: 20,
    paddingBottom: 100,
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
  },
});