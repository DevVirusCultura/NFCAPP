import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { Smartphone, Calendar, FileText, Wifi, Zap, CreditCard as Edit } from 'lucide-react-native';
import { NFCTag } from './NFCManager';

interface TagCardProps {
  tag: NFCTag;
  onPress: () => void;
  onDelete?: () => void;
  showEmulateButton?: boolean;
  onEmulate?: () => void;
}

export function TagCard({ tag, onPress, onDelete, showEmulateButton = false, onEmulate }: TagCardProps) {
  const formatTimestamp = (timestamp: string) => {
    const date = new Date(timestamp);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / (1000 * 60));
    const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins}m ago`;
    if (diffHours < 24) return `${diffHours}h ago`;
    if (diffDays < 7) return `${diffDays}d ago`;
    
    return date.toLocaleDateString();
  };

  const getRecordPreview = () => {
    if (tag.records.length === 0) return 'No NDEF records';
    
    const firstRecord = tag.records[0];
    const preview = firstRecord.data.length > 50 
      ? firstRecord.data.substring(0, 50) + '...'
      : firstRecord.data;
    
    return `${firstRecord.recordType}: ${preview}`;
  };

  const getTechnologyIcons = () => {
    return tag.technologies?.map((tech, index) => (
      <View key={index} style={styles.techBadge}>
        <Wifi size={12} color="#6366f1" />
        <Text style={styles.techText}>{tech}</Text>
      </View>
    ));
  };

  return (
    <TouchableOpacity style={styles.card} onPress={onPress} activeOpacity={0.7}>
      <View style={styles.header}>
        <View style={styles.iconContainer}>
          <Smartphone size={24} color="#6366f1" />
        </View>
        <View style={styles.headerInfo}>
          <Text style={styles.tagId} numberOfLines={1}>
            {tag.id}
          </Text>
          <View style={styles.timestampRow}>
            <Calendar size={14} color="#6b7280" />
            <Text style={styles.timestamp}>
              {formatTimestamp(tag.timestamp)}
            </Text>
          </View>
        </View>
        {tag.isWritable && (
          <View style={styles.writableBadge}>
            <Text style={styles.writableText}>W</Text>
          </View>
        )}
        {tag.isVirtual && (
          <View style={styles.virtualBadge}>
            <Zap size={12} color="#ffffff" />
          </View>
        )}
      </View>

      <View style={styles.content}>
        <View style={styles.recordsInfo}>
          <FileText size={16} color="#6b7280" />
          <Text style={styles.recordsCount}>
            {tag.records.length} record{tag.records.length !== 1 ? 's' : ''}
          </Text>
        </View>
        
        <Text style={styles.recordPreview} numberOfLines={2}>
          {getRecordPreview()}
        </Text>

        {tag.technologies && tag.technologies.length > 0 && (
          <View style={styles.technologiesContainer}>
            {getTechnologyIcons()}
          </View>
        )}
      </View>

      <View style={styles.footer}>
        <View style={styles.metaInfo}>
          {tag.size && (
            <Text style={styles.metaText}>
              Size: {tag.size} bytes
            </Text>
          )}
          {tag.type && (
            <Text style={styles.metaText}>
              {tag.isVirtual ? 'Virtual' : 'Type'}: {tag.type}
            </Text>
          )}
        </View>
        
        {showEmulateButton && onEmulate && (
          <TouchableOpacity style={styles.emulateButton} onPress={onEmulate}>
            <Zap size={14} color="#6366f1" />
            <Text style={styles.emulateButtonText}>Emulate</Text>
          </TouchableOpacity>
        )}
      </View>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: '#ffffff',
    borderRadius: 16,
    padding: 16,
    marginBottom: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 8,
    elevation: 4,
    borderWidth: 1,
    borderColor: '#f3f4f6',
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 12,
  },
  iconContainer: {
    width: 48,
    height: 48,
    borderRadius: 24,
    backgroundColor: '#eef2ff',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 12,
  },
  headerInfo: {
    flex: 1,
  },
  tagId: {
    fontSize: 16,
    fontFamily: 'Inter-SemiBold',
    color: '#1f2937',
    marginBottom: 4,
  },
  timestampRow: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  timestamp: {
    fontSize: 14,
    fontFamily: 'Inter-Regular',
    color: '#6b7280',
    marginLeft: 6,
  },
  writableBadge: {
    width: 24,
    height: 24,
    borderRadius: 12,
    backgroundColor: '#10b981',
    alignItems: 'center',
    justifyContent: 'center',
  },
  writableText: {
    fontSize: 12,
    fontFamily: 'Inter-Bold',
    color: '#ffffff',
  },
  virtualBadge: {
    width: 24,
    height: 24,
    borderRadius: 12,
    backgroundColor: '#8b5cf6',
    alignItems: 'center',
    justifyContent: 'center',
    marginLeft: 8,
  },
  content: {
    borderTopWidth: 1,
    borderTopColor: '#f3f4f6',
    paddingTop: 12,
  },
  recordsInfo: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 8,
  },
  recordsCount: {
    fontSize: 14,
    fontFamily: 'Inter-Medium',
    color: '#6b7280',
    marginLeft: 6,
  },
  recordPreview: {
    fontSize: 14,
    fontFamily: 'Inter-Regular',
    color: '#374151',
    lineHeight: 20,
    marginBottom: 8,
  },
  technologiesContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    marginBottom: 8,
  },
  techBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#eef2ff',
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 6,
    marginRight: 6,
    marginBottom: 4,
  },
  techText: {
    fontSize: 12,
    fontFamily: 'Inter-Medium',
    color: '#6366f1',
    marginLeft: 4,
  },
  footer: {
    borderTopWidth: 1,
    borderTopColor: '#f3f4f6',
    paddingTop: 8,
  },
  metaInfo: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  metaText: {
    fontSize: 12,
    fontFamily: 'Inter-Regular',
    color: '#9ca3af',
  },
  emulateButton: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#eef2ff',
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 6,
  },
  emulateButtonText: {
    fontSize: 12,
    fontFamily: 'Inter-Medium',
    color: '#6366f1',
    marginLeft: 4,
  },
});