import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  ScrollView,
  Platform,
  Alert,
  Share,
  TextInput,
} from 'react-native';
import { useLocalSearchParams, router } from 'expo-router';
import { LinearGradient } from 'expo-linear-gradient';
import { ArrowLeft, Smartphone, Calendar, FileText, Share2, Trash2, Wifi, Copy, Zap, CreditCard as Edit3, Save, X, Plus, Minus } from 'lucide-react-native';
import { NFCManager, NFCTag, NFCWriteData } from '@/components/NFCManager';

export default function TagDetailsScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const [tag, setTag] = useState<NFCTag | null>(null);
  const [isEmulating, setIsEmulating] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [editedTag, setEditedTag] = useState<NFCTag | null>(null);
  const [isWriting, setIsWriting] = useState(false);

  useEffect(() => {
    loadTagDetails();
  }, [id]);

  const loadTagDetails = async () => {
    try {
      const history = await NFCManager.getHistory();
      const foundTag = history.find(t => t.id === id);
      setTag(foundTag || null);
      setEditedTag(foundTag || null);
    } catch (error) {
      console.error('Failed to load tag details:', error);
    }
  };

  const handleEmulate = async () => {
    if (!tag) return;

    setIsEmulating(true);
    try {
      await NFCManager.emulateTag(tag);
      Alert.alert(
        'Emulation Started',
        'Hold your device near another NFC-enabled device to transfer the data. The emulation will continue until you stop it.',
        [
          { text: 'Stop', onPress: () => setIsEmulating(false) },
          { text: 'Keep Running', style: 'cancel' }
        ]
      );
    } catch (error) {
      console.error('Emulation failed:', error);
      Alert.alert('Emulation Failed', (error as Error).message);
      setIsEmulating(false);
    }
  };

  const handleWriteToTag = async () => {
    if (!editedTag) return;

    Alert.alert(
      'Write to NFC Tag',
      'This will write the current data to a new NFC tag. Make sure you have a writable NFC tag ready.',
      [
        { text: 'Cancel', style: 'cancel' },
        {
          text: 'Write',
          onPress: async () => {
            setIsWriting(true);
            try {
              const writeData: NFCWriteData = {
                records: editedTag.records.map(record => ({
                  recordType: record.recordType,
                  data: record.data,
                  encoding: record.encoding || 'utf-8',
                  language: record.language || 'en',
                })),
              };
              
              await NFCManager.writeTag(writeData);
              Alert.alert(
                'Write Successful',
                'The data has been written to the NFC tag successfully.'
              );
            } catch (error) {
              console.error('Write failed:', error);
              Alert.alert('Write Failed', (error as Error).message);
            } finally {
              setIsWriting(false);
            }
          }
        }
      ]
    );
  };

  const handleShare = async () => {
    if (!tag) return;

    const shareData = {
      title: `NFC Tag: ${tag.id}`,
      message: `NFC Tag Details:\n\nID: ${tag.id}\nUID: ${tag.uid || 'N/A'}\nType: ${tag.type || 'Unknown'}\nScanned: ${new Date(tag.timestamp).toLocaleString()}\nTechnologies: ${tag.technologies?.join(', ') || 'N/A'}\n\nRecords (${tag.records.length}):\n${tag.records.map((r, i) => `${i + 1}. ${r.recordType}: ${r.data}`).join('\n')}`,
    };

    try {
      if (Platform.OS === 'web') {
        if (navigator.share) {
          await navigator.share(shareData);
        } else {
          await navigator.clipboard.writeText(shareData.message);
          Alert.alert('Copied', 'Tag details copied to clipboard');
        }
      } else {
        await Share.share(shareData);
      }
    } catch (error) {
      console.error('Share failed:', error);
    }
  };

  const handleDelete = () => {
    if (!tag) return;

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
              await NFCManager.deleteTag(tag.id);
              router.back();
            } catch (error) {
              console.error('Delete failed:', error);
              Alert.alert('Delete Failed', 'Unable to delete the tag.');
            }
          },
        },
      ]
    );
  };

  const copyToClipboard = async (text: string) => {
    try {
      if (Platform.OS === 'web') {
        await navigator.clipboard.writeText(text);
      }
      Alert.alert('Copied', 'Text copied to clipboard');
    } catch (error) {
      console.error('Copy failed:', error);
    }
  };

  const handleSaveEdit = async () => {
    if (!editedTag) return;

    try {
      await NFCManager.saveTagToHistory(editedTag);
      setTag(editedTag);
      setIsEditing(false);
      Alert.alert('Saved', 'Tag details have been updated');
    } catch (error) {
      console.error('Save failed:', error);
      Alert.alert('Save Failed', 'Unable to save changes');
    }
  };

  const handleCancelEdit = () => {
    setEditedTag(tag);
    setIsEditing(false);
  };

  const addRecord = () => {
    if (!editedTag) return;
    
    const newRecord = {
      recordType: 'text',
      data: '',
      encoding: 'utf-8',
      language: 'en',
    };
    
    setEditedTag({
      ...editedTag,
      records: [...editedTag.records, newRecord],
    });
  };

  const removeRecord = (index: number) => {
    if (!editedTag) return;
    
    const newRecords = editedTag.records.filter((_, i) => i !== index);
    setEditedTag({
      ...editedTag,
      records: newRecords,
    });
  };

  const updateRecord = (index: number, field: string, value: string) => {
    if (!editedTag) return;
    
    const newRecords = [...editedTag.records];
    newRecords[index] = { ...newRecords[index], [field]: value };
    
    setEditedTag({
      ...editedTag,
      records: newRecords,
    });
  };

  if (!tag) {
    return (
      <LinearGradient colors={['#f8fafc', '#e2e8f0']} style={styles.container}>
        <View style={styles.header}>
          <TouchableOpacity style={styles.backButton} onPress={() => router.back()}>
            <ArrowLeft size={24} color="#1f2937" />
          </TouchableOpacity>
          <Text style={styles.title}>Tag Not Found</Text>
        </View>
        <View style={styles.emptyState}>
          <Smartphone size={64} color="#d1d5db" strokeWidth={1} />
          <Text style={styles.emptyTitle}>Tag Not Found</Text>
          <Text style={styles.emptySubtitle}>
            The requested NFC tag could not be found in your history.
          </Text>
        </View>
      </LinearGradient>
    );
  }

  const currentTag = isEditing ? editedTag : tag;

  return (
    <LinearGradient colors={['#f8fafc', '#e2e8f0']} style={styles.container}>
      <View style={styles.header}>
        <TouchableOpacity style={styles.backButton} onPress={() => router.back()}>
          <ArrowLeft size={24} color="#1f2937" />
        </TouchableOpacity>
        <Text style={styles.title}>Tag Details</Text>
        <View style={styles.headerActions}>
          {isEditing ? (
            <>
              <TouchableOpacity style={styles.actionButton} onPress={handleSaveEdit}>
                <Save size={20} color="#10b981" />
              </TouchableOpacity>
              <TouchableOpacity style={styles.actionButton} onPress={handleCancelEdit}>
                <X size={20} color="#ef4444" />
              </TouchableOpacity>
            </>
          ) : (
            <>
              <TouchableOpacity style={styles.actionButton} onPress={() => setIsEditing(true)}>
                <Edit3 size={20} color="#6366f1" />
              </TouchableOpacity>
              <TouchableOpacity style={styles.actionButton} onPress={handleShare}>
                <Share2 size={20} color="#6366f1" />
              </TouchableOpacity>
              <TouchableOpacity style={styles.actionButton} onPress={handleDelete}>
                <Trash2 size={20} color="#ef4444" />
              </TouchableOpacity>
            </>
          )}
        </View>
      </View>

      <ScrollView style={styles.content} showsVerticalScrollIndicator={false}>
        <View style={styles.tagCard}>
          <View style={styles.tagHeader}>
            <View style={styles.tagIcon}>
              <Smartphone size={32} color="#6366f1" />
            </View>
            <View style={styles.tagInfo}>
              <Text style={styles.tagId}>{currentTag?.id}</Text>
              <View style={styles.tagMeta}>
                <Calendar size={14} color="#6b7280" />
                <Text style={styles.tagTime}>
                  {new Date(currentTag?.timestamp || '').toLocaleString()}
                </Text>
              </View>
            </View>
          </View>

          <View style={styles.tagDetails}>
            {currentTag?.uid && (
              <View style={styles.detailRow}>
                <Text style={styles.detailLabel}>UID</Text>
                <TouchableOpacity
                  style={styles.detailValue}
                  onPress={() => copyToClipboard(currentTag.uid!)}
                >
                  <Text style={styles.detailText}>{currentTag.uid}</Text>
                  <Copy size={16} color="#6b7280" />
                </TouchableOpacity>
              </View>
            )}

            {currentTag?.type && (
              <View style={styles.detailRow}>
                <Text style={styles.detailLabel}>Type</Text>
                <Text style={styles.detailText}>{currentTag.type}</Text>
              </View>
            )}

            {currentTag?.technologies && currentTag.technologies.length > 0 && (
              <View style={styles.detailRow}>
                <Text style={styles.detailLabel}>Technologies</Text>
                <View style={styles.technologiesContainer}>
                  {currentTag.technologies.map((tech, index) => (
                    <View key={index} style={styles.technologyTag}>
                      <Text style={styles.technologyText}>{tech}</Text>
                    </View>
                  ))}
                </View>
              </View>
            )}

            {currentTag?.size && (
              <View style={styles.detailRow}>
                <Text style={styles.detailLabel}>Size</Text>
                <Text style={styles.detailText}>{currentTag.size} bytes</Text>
              </View>
            )}

            <View style={styles.detailRow}>
              <Text style={styles.detailLabel}>Writable</Text>
              <Text style={styles.detailText}>
                {currentTag?.isWritable ? 'Yes' : 'No'}
              </Text>
            </View>
          </View>
        </View>

        {currentTag && currentTag.records.length > 0 && (
          <View style={styles.recordsSection}>
            <View style={styles.sectionHeader}>
              <FileText size={20} color="#374151" />
              <Text style={styles.sectionTitle}>
                NDEF Records ({currentTag.records.length})
              </Text>
              {isEditing && (
                <TouchableOpacity style={styles.addButton} onPress={addRecord}>
                  <Plus size={20} color="#10b981" />
                </TouchableOpacity>
              )}
            </View>

            {currentTag.records.map((record, index) => (
              <View key={index} style={styles.recordCard}>
                <View style={styles.recordHeader}>
                  {isEditing ? (
                    <TextInput
                      style={styles.recordTypeInput}
                      value={record.recordType}
                      onChangeText={(text) => updateRecord(index, 'recordType', text)}
                      placeholder="Record Type"
                    />
                  ) : (
                    <Text style={styles.recordType}>{record.recordType}</Text>
                  )}
                  <View style={styles.recordActions}>
                    <TouchableOpacity
                      onPress={() => copyToClipboard(record.data)}
                      style={styles.copyButton}
                    >
                      <Copy size={16} color="#6b7280" />
                    </TouchableOpacity>
                    {isEditing && (
                      <TouchableOpacity
                        onPress={() => removeRecord(index)}
                        style={styles.removeButton}
                      >
                        <Minus size={16} color="#ef4444" />
                      </TouchableOpacity>
                    )}
                  </View>
                </View>
                
                {isEditing ? (
                  <TextInput
                    style={styles.recordDataInput}
                    value={record.data}
                    onChangeText={(text) => updateRecord(index, 'data', text)}
                    placeholder="Record Data"
                    multiline
                  />
                ) : (
                  <Text style={styles.recordData}>{record.data}</Text>
                )}

                {record.encoding && (
                  <Text style={styles.recordMeta}>
                    Encoding: {record.encoding}
                    {record.language && ` • Language: ${record.language}`}
                  </Text>
                )}
              </View>
            ))}
          </View>
        )}

        <View style={styles.actionsSection}>
          <TouchableOpacity
            style={[styles.actionButtonLarge, styles.emulateButton, isEmulating && styles.emulatingButton]}
            onPress={handleEmulate}
            disabled={isEmulating || isEditing}
          >
            <Zap size={24} color="#ffffff" />
            <Text style={styles.actionButtonText}>
              {isEmulating ? 'Emulating...' : 'Emulate Tag'}
            </Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={[styles.actionButtonLarge, styles.writeButton]}
            onPress={handleWriteToTag}
            disabled={isWriting || isEditing}
          >
            <FileText size={24} color="#ffffff" />
            <Text style={styles.actionButtonText}>
              {isWriting ? 'Writing...' : 'Write to Tag'}
            </Text>
          </TouchableOpacity>

          <Text style={styles.actionHint}>
            {isEmulating
              ? 'Hold your device near another NFC device to transfer data'
              : isWriting
              ? 'Hold a writable NFC tag near your device'
              : 'Use emulation to share data or write to create a new tag'
            }
          </Text>
        </View>
      </ScrollView>
    </LinearGradient>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    paddingTop: Platform.OS === 'ios' ? 60 : 40,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 20,
    marginBottom: 20,
  },
  backButton: {
    padding: 8,
    marginRight: 12,
  },
  title: {
    fontSize: 24,
    fontFamily: 'Inter-Bold',
    color: '#1f2937',
    flex: 1,
  },
  headerActions: {
    flexDirection: 'row',
  },
  actionButton: {
    padding: 8,
    marginLeft: 8,
  },
  content: {
    flex: 1,
    paddingHorizontal: 20,
  },
  tagCard: {
    backgroundColor: '#ffffff',
    borderRadius: 16,
    padding: 20,
    marginBottom: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 8,
    elevation: 4,
  },
  tagHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 20,
  },
  tagIcon: {
    width: 60,
    height: 60,
    borderRadius: 30,
    backgroundColor: '#eef2ff',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 16,
  },
  tagInfo: {
    flex: 1,
  },
  tagId: {
    fontSize: 20,
    fontFamily: 'Inter-Bold',
    color: '#1f2937',
    marginBottom: 4,
  },
  tagMeta: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  tagTime: {
    fontSize: 14,
    fontFamily: 'Inter-Regular',
    color: '#6b7280',
    marginLeft: 6,
  },
  tagDetails: {
    borderTopWidth: 1,
    borderTopColor: '#f3f4f6',
    paddingTop: 16,
  },
  detailRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 12,
  },
  detailLabel: {
    fontSize: 14,
    fontFamily: 'Inter-Medium',
    color: '#6b7280',
    flex: 1,
  },
  detailValue: {
    flexDirection: 'row',
    alignItems: 'center',
    flex: 2,
    justifyContent: 'flex-end',
  },
  detailText: {
    fontSize: 14,
    fontFamily: 'Inter-Regular',
    color: '#1f2937',
    textAlign: 'right',
    marginRight: 8,
  },
  technologiesContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    flex: 2,
    justifyContent: 'flex-end',
  },
  technologyTag: {
    backgroundColor: '#eef2ff',
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 6,
    marginLeft: 4,
    marginBottom: 4,
  },
  technologyText: {
    fontSize: 12,
    fontFamily: 'Inter-Medium',
    color: '#6366f1',
  },
  recordsSection: {
    marginBottom: 20,
  },
  sectionHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 16,
  },
  sectionTitle: {
    fontSize: 18,
    fontFamily: 'Inter-SemiBold',
    color: '#374151',
    marginLeft: 8,
    flex: 1,
  },
  addButton: {
    padding: 4,
  },
  recordCard: {
    backgroundColor: '#ffffff',
    borderRadius: 12,
    padding: 16,
    marginBottom: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 4,
    elevation: 2,
  },
  recordHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
  },
  recordType: {
    fontSize: 12,
    fontFamily: 'Inter-SemiBold',
    color: '#6366f1',
    textTransform: 'uppercase',
  },
  recordTypeInput: {
    fontSize: 12,
    fontFamily: 'Inter-SemiBold',
    color: '#6366f1',
    textTransform: 'uppercase',
    borderBottomWidth: 1,
    borderBottomColor: '#e5e7eb',
    paddingVertical: 2,
    flex: 1,
    marginRight: 8,
  },
  recordActions: {
    flexDirection: 'row',
  },
  copyButton: {
    padding: 4,
    marginLeft: 4,
  },
  removeButton: {
    padding: 4,
    marginLeft: 4,
  },
  recordData: {
    fontSize: 14,
    fontFamily: 'Inter-Regular',
    color: '#374151',
    lineHeight: 20,
  },
  recordDataInput: {
    fontSize: 14,
    fontFamily: 'Inter-Regular',
    color: '#374151',
    lineHeight: 20,
    borderWidth: 1,
    borderColor: '#e5e7eb',
    borderRadius: 8,
    padding: 8,
    minHeight: 40,
  },
  recordMeta: {
    fontSize: 12,
    fontFamily: 'Inter-Regular',
    color: '#9ca3af',
    marginTop: 4,
  },
  actionsSection: {
    alignItems: 'center',
    paddingBottom: 40,
  },
  actionButtonLarge: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 16,
    paddingHorizontal: 32,
    borderRadius: 16,
    marginBottom: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 8,
    elevation: 6,
    width: '100%',
  },
  emulateButton: {
    backgroundColor: '#6366f1',
  },
  emulatingButton: {
    backgroundColor: '#10b981',
  },
  writeButton: {
    backgroundColor: '#f59e0b',
  },
  actionButtonText: {
    fontSize: 18,
    fontFamily: 'Inter-SemiBold',
    color: '#ffffff',
    marginLeft: 8,
  },
  actionHint: {
    fontSize: 14,
    fontFamily: 'Inter-Regular',
    color: '#6b7280',
    textAlign: 'center',
    lineHeight: 20,
    paddingHorizontal: 20,
  },
  emptyState: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 40,
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