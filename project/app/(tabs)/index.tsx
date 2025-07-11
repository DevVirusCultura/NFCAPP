import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  Platform,
  Alert,
  Dimensions,
  ScrollView,
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { Scan, Wifi, WifiOff, Smartphone, CircleAlert as AlertCircle, CircleCheck as CheckCircle, Settings, History, Zap, Shield, Info } from 'lucide-react-native';
import { NFCManager, NFCTag } from '@/components/NFCManager';
import { ScanAnimation } from '@/components/ScanAnimation';
import { TagCard } from '@/components/TagCard';
import { router } from 'expo-router';

const { width, height } = Dimensions.get('window');

export default function ScannerScreen() {
  const [isScanning, setIsScanning] = useState(false);
  const [nfcSupported, setNfcSupported] = useState(false);
  const [nfcEnabled, setNfcEnabled] = useState(false);
  const [lastScan, setLastScan] = useState<NFCTag | null>(null);
  const [recentTags, setRecentTags] = useState<NFCTag[]>([]);
  const [scanCount, setScanCount] = useState(0);
  const [error, setError] = useState<string | null>(null);
  const [showWelcome, setShowWelcome] = useState(false);

  useEffect(() => {
    checkNFCSupport();
    loadRecentTags();
    loadScanCount();
    checkFirstTime();
  }, []);

  const checkFirstTime = async () => {
    try {
      const history = await NFCManager.getHistory();
      if (history.length === 0) {
        setShowWelcome(true);
      }
    } catch (error) {
      console.error('Failed to check first time:', error);
    }
  };

  const checkNFCSupport = async () => {
    try {
      const supported = await NFCManager.isNFCSupported();
      const enabled = await NFCManager.isNFCEnabled();
      
      setNfcSupported(supported);
      setNfcEnabled(enabled);
      
      if (!supported) {
        setError('NFC not supported on this device. Please use Chrome on Android with NFC enabled.');
      } else if (!enabled) {
        setError('NFC is disabled. Please enable NFC in your device settings.');
      } else {
        setError(null);
      }
    } catch (error) {
      console.error('NFC check failed:', error);
      setNfcSupported(false);
      setNfcEnabled(false);
      setError('Unable to check NFC status');
    }
  };

  const loadRecentTags = async () => {
    try {
      const history = await NFCManager.getHistory();
      setRecentTags(history.slice(0, 3)); // Show last 3 tags
    } catch (error) {
      console.error('Failed to load recent tags:', error);
    }
  };

  const loadScanCount = async () => {
    try {
      const history = await NFCManager.getHistory();
      setScanCount(history.length);
    } catch (error) {
      console.error('Failed to load scan count:', error);
    }
  };

  const handleStartScan = async () => {
    if (!nfcSupported) {
      Alert.alert(
        'NFC Not Supported',
        'Your browser does not support Web NFC API. Please use Chrome on Android with NFC enabled.',
        [{ text: 'OK' }]
      );
      return;
    }

    if (!nfcEnabled) {
      Alert.alert(
        'NFC Disabled',
        'Please enable NFC in your device settings and refresh the page.',
        [
          { text: 'Cancel' },
          { text: 'Refresh', onPress: () => window.location.reload() }
        ]
      );
      return;
    }

    setIsScanning(true);
    setError(null);
    
    try {
      const tagData = await NFCManager.startScan();
      setLastScan(tagData);
      setIsScanning(false);
      await loadRecentTags();
      await loadScanCount();
      
      // Show success feedback
      Alert.alert(
        'Tag Scanned Successfully!',
        `Successfully scanned tag "${tagData.id}" with ${tagData.records.length} NDEF record${tagData.records.length !== 1 ? 's' : ''}`,
        [
          { text: 'View Details', onPress: () => router.push(`/(tabs)/details/${tagData.id}`) },
          { text: 'Scan Another', style: 'cancel' }
        ]
      );
      setShowWelcome(false);
    } catch (error) {
      console.error('NFC scan error:', error);
      setIsScanning(false);
      setError((error as Error).message);
      
      Alert.alert(
        'Scan Failed',
        (error as Error).message,
        [{ text: 'Try Again' }]
      );
    }
  };

  const stopScan = () => {
    NFCManager.stopScan();
    setIsScanning(false);
  };

  const handleTagPress = (tagId: string) => {
    router.push(`/(tabs)/details/${tagId}`);
  };

  const getStatusColor = () => {
    if (!nfcSupported) return '#ef4444';
    if (!nfcEnabled) return '#f59e0b';
    return '#10b981';
  };

  const getStatusText = () => {
    if (!nfcSupported) return 'Not Supported';
    if (!nfcEnabled) return 'Disabled';
    return 'Ready';
  };

  const getStatusIcon = () => {
    if (!nfcSupported) return <WifiOff size={20} color="#ffffff" />;
    if (!nfcEnabled) return <AlertCircle size={20} color="#ffffff" />;
    return <CheckCircle size={20} color="#ffffff" />;
  };

  const showNFCInfo = () => {
    Alert.alert(
      'About NFC Technology',
      'Near Field Communication (NFC) is a short-range wireless technology that enables communication between devices when they are brought close together (typically within 4cm).\n\nThis app can:\n• Read NFC tags and cards\n• Display tag information and data\n• Save scan history\n• Emulate existing tags\n• Create virtual tags\n\nNote: This app uses Web NFC API and works best on Chrome for Android with NFC enabled.',
      [{ text: 'Got it!' }]
    );
  };

  const showPrivacyInfo = () => {
    Alert.alert(
      'Privacy & Security',
      'Your privacy is our priority:\n\n• All NFC data is stored locally on your device\n• No data is transmitted to external servers\n• No personal information is collected\n• You have full control over your data\n• Export/import features work offline\n\nThis app does not require internet connectivity and works completely offline to protect your privacy.',
      [{ text: 'Understood' }]
    );
  };

  const dismissWelcome = () => setShowWelcome(false);

  return (
    <LinearGradient
      colors={['#667eea', '#764ba2']}
      style={styles.container}
    >
      <ScrollView 
        style={styles.scrollView}
        contentContainerStyle={styles.scrollContent}
        showsVerticalScrollIndicator={false}
      >
        {showWelcome && (
          <View style={styles.welcomeCard}>
            <View style={styles.welcomeHeader}>
              <Smartphone size={32} color="#6366f1" />
              <Text style={styles.welcomeTitle}>Welcome to NFC Scanner!</Text>
            </View>
            <Text style={styles.welcomeText}>
              Discover the power of NFC technology. Scan tags, save history, and emulate existing tags with ease.
            </Text>
            <View style={styles.welcomeFeatures}>
              <View style={styles.featureItem}>
                <Scan size={16} color="#10b981" />
                <Text style={styles.featureText}>Scan NFC tags & cards</Text>
              </View>
              <View style={styles.featureItem}>
                <History size={16} color="#10b981" />
                <Text style={styles.featureText}>Save scan history</Text>
              </View>
              <View style={styles.featureItem}>
                <Zap size={16} color="#10b981" />
                <Text style={styles.featureText}>Emulate existing tags</Text>
              </View>
            </View>
            <View style={styles.welcomeActions}>
              <TouchableOpacity style={styles.infoButton} onPress={showNFCInfo}>
                <Info size={16} color="#6366f1" />
                <Text style={styles.infoButtonText}>Learn More</Text>
              </TouchableOpacity>
              <TouchableOpacity style={styles.privacyButton} onPress={showPrivacyInfo}>
                <Shield size={16} color="#6366f1" />
                <Text style={styles.privacyButtonText}>Privacy</Text>
              </TouchableOpacity>
              <TouchableOpacity style={styles.dismissButton} onPress={dismissWelcome}>
                <Text style={styles.dismissButtonText}>Get Started</Text>
              </TouchableOpacity>
            </View>
          </View>
        )}

        <View style={styles.header}>
          <Text style={styles.title}>NFC Scanner</Text>
          <Text style={styles.subtitle}>
            {nfcSupported 
              ? 'Hold an NFC tag near your device to scan'
              : 'NFC not available on this device'
            }
          </Text>
          
          <View style={styles.statsContainer}>
            <View style={styles.statItem}>
              <Text style={styles.statNumber}>{scanCount}</Text>
              <Text style={styles.statLabel}>Tags Scanned</Text>
            </View>
            <View style={styles.statDivider} />
            <View style={styles.statItem}>
              <Text style={styles.statNumber}>{recentTags.length}</Text>
              <Text style={styles.statLabel}>Recent</Text>
            </View>
          </View>
        </View>

        <View style={styles.scanArea}>
          <ScanAnimation isScanning={isScanning} size={220} />
          
          <View style={styles.statusContainer}>
            <View style={[styles.statusIndicator, { backgroundColor: getStatusColor() }]}>
              {getStatusIcon()}
            </View>
            <Text style={styles.statusText}>
              NFC {getStatusText()}
            </Text>
          </View>

          {error && (
            <View style={styles.errorContainer}>
              <AlertCircle size={16} color="#ef4444" />
              <Text style={styles.errorText}>{error}</Text>
            </View>
          )}
        </View>

        <View style={styles.controls}>
          {isScanning ? (
            <TouchableOpacity
              style={[styles.button, styles.stopButton]}
              onPress={stopScan}
            >
              <Text style={styles.buttonText}>Stop Scanning</Text>
            </TouchableOpacity>
          ) : (
            <TouchableOpacity
              style={[
                styles.button,
                (!nfcSupported || !nfcEnabled) && styles.disabledButton
              ]}
              onPress={handleStartScan}
              disabled={!nfcSupported || !nfcEnabled}
            >
              <Scan size={24} color="#ffffff" strokeWidth={2} />
              <Text style={styles.buttonText}>Start Scan</Text>
            </TouchableOpacity>
          )}

          <View style={styles.quickActions}>
            <TouchableOpacity
              style={styles.quickActionButton}
              onPress={() => router.push('/(tabs)/history')}
            >
              <History size={20} color="#ffffff" />
              <Text style={styles.quickActionText}>History</Text>
            </TouchableOpacity>
            
            <TouchableOpacity
              style={styles.quickActionButton}
              onPress={() => router.push('/(tabs)/emulator')}
            >
              <Zap size={20} color="#ffffff" />
              <Text style={styles.quickActionText}>Emulator</Text>
            </TouchableOpacity>
            
            <TouchableOpacity
              style={styles.quickActionButton}
              onPress={() => router.push('/(tabs)/settings')}
            >
              <Settings size={20} color="#ffffff" />
              <Text style={styles.quickActionText}>Settings</Text>
            </TouchableOpacity>
          </View>
        </View>

        {lastScan && (
          <View style={styles.lastScanContainer}>
            <Text style={styles.sectionTitle}>Last Scanned Tag</Text>
            <TagCard
              tag={lastScan}
              onPress={() => handleTagPress(lastScan.id)}
            />
          </View>
        )}

        {recentTags.length > 0 && (
          <View style={styles.recentTagsContainer}>
            <View style={styles.sectionHeader}>
              <Text style={styles.sectionTitle}>Recent Scans</Text>
              <TouchableOpacity onPress={() => router.push('/(tabs)/history')}>
                <Text style={styles.viewAllText}>View All</Text>
              </TouchableOpacity>
            </View>
            
            {recentTags.map((tag) => (
              <TagCard
                key={tag.id}
                tag={tag}
                onPress={() => handleTagPress(tag.id)}
              />
            ))}
          </View>
        )}

        {recentTags.length === 0 && !lastScan && (
          <View style={styles.emptyState}>
            <Smartphone size={64} color="rgba(255, 255, 255, 0.3)" strokeWidth={1} />
            <Text style={styles.emptyTitle}>No Tags Scanned Yet</Text>
            <Text style={styles.emptySubtitle}>
              Tap the scan button above to start detecting NFC tags
            </Text>
          </View>
        )}
      </ScrollView>
    </LinearGradient>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    paddingTop: Platform.OS === 'ios' ? 60 : 40,
  },
  scrollView: {
    flex: 1,
  },
  scrollContent: {
    paddingBottom: 100,
  },
  welcomeCard: {
    backgroundColor: 'rgba(255, 255, 255, 0.95)',
    borderRadius: 16,
    padding: 20,
    marginHorizontal: 20,
    marginBottom: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.15,
    shadowRadius: 12,
    elevation: 6,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.3)',
  },
  welcomeHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 12,
  },
  welcomeTitle: {
    fontSize: 20,
    fontFamily: 'Inter-Bold',
    color: '#1f2937',
    marginLeft: 12,
  },
  welcomeText: {
    fontSize: 16,
    fontFamily: 'Inter-Regular',
    color: '#4b5563',
    lineHeight: 24,
    marginBottom: 16,
  },
  welcomeFeatures: {
    marginBottom: 20,
  },
  featureItem: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 8,
  },
  featureText: {
    fontSize: 14,
    fontFamily: 'Inter-Medium',
    color: '#374151',
    marginLeft: 8,
  },
  welcomeActions: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  infoButton: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#f3f4f6',
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderRadius: 8,
  },
  infoButtonText: {
    fontSize: 14,
    fontFamily: 'Inter-Medium',
    color: '#6366f1',
    marginLeft: 4,
  },
  privacyButton: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#f3f4f6',
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderRadius: 8,
  },
  privacyButtonText: {
    fontSize: 14,
    fontFamily: 'Inter-Medium',
    color: '#6366f1',
    marginLeft: 4,
  },
  dismissButton: {
    backgroundColor: '#6366f1',
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 8,
  },
  dismissButtonText: {
    fontSize: 14,
    fontFamily: 'Inter-SemiBold',
    color: '#ffffff',
  },
  header: {
    alignItems: 'center',
    paddingHorizontal: 20,
    marginBottom: 30,
  },
  title: {
    fontSize: 32,
    fontFamily: 'Inter-Bold',
    color: '#ffffff',
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 16,
    fontFamily: 'Inter-Regular',
    color: '#e0e7ff',
    textAlign: 'center',
    lineHeight: 24,
    marginBottom: 20,
  },
  statsContainer: {
    flexDirection: 'row',
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    borderRadius: 16,
    padding: 16,
    alignItems: 'center',
  },
  statItem: {
    alignItems: 'center',
    flex: 1,
  },
  statNumber: {
    fontSize: 24,
    fontFamily: 'Inter-Bold',
    color: '#ffffff',
  },
  statLabel: {
    fontSize: 14,
    fontFamily: 'Inter-Regular',
    color: '#e0e7ff',
    marginTop: 4,
  },
  statDivider: {
    width: 1,
    height: 40,
    backgroundColor: 'rgba(255, 255, 255, 0.2)',
    marginHorizontal: 20,
  },
  scanArea: {
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 30,
  },
  statusContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 20,
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 20,
  },
  statusIndicator: {
    width: 32,
    height: 32,
    borderRadius: 16,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 8,
  },
  statusText: {
    fontSize: 16,
    fontFamily: 'Inter-Medium',
    color: '#ffffff',
  },
  errorContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: 'rgba(239, 68, 68, 0.1)',
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 8,
    marginTop: 12,
    borderWidth: 1,
    borderColor: 'rgba(239, 68, 68, 0.3)',
  },
  errorText: {
    fontSize: 14,
    fontFamily: 'Inter-Regular',
    color: '#fca5a5',
    marginLeft: 8,
    textAlign: 'center',
  },
  controls: {
    paddingHorizontal: 20,
    marginBottom: 30,
  },
  button: {
    backgroundColor: 'rgba(255, 255, 255, 0.2)',
    borderRadius: 16,
    paddingVertical: 16,
    paddingHorizontal: 32,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.3)',
    marginBottom: 16,
  },
  stopButton: {
    backgroundColor: 'rgba(239, 68, 68, 0.8)',
    borderColor: 'rgba(239, 68, 68, 0.6)',
  },
  disabledButton: {
    backgroundColor: 'rgba(156, 163, 175, 0.3)',
    borderColor: 'rgba(156, 163, 175, 0.3)',
  },
  buttonText: {
    fontSize: 18,
    fontFamily: 'Inter-SemiBold',
    color: '#ffffff',
    marginLeft: 8,
  },
  quickActions: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  quickActionButton: {
    flex: 1,
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    borderRadius: 12,
    paddingVertical: 12,
    paddingHorizontal: 16,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    marginHorizontal: 4,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.2)',
  },
  quickActionText: {
    fontSize: 14,
    fontFamily: 'Inter-Medium',
    color: '#ffffff',
    marginLeft: 6,
  },
  lastScanContainer: {
    paddingHorizontal: 20,
    marginBottom: 20,
  },
  recentTagsContainer: {
    paddingHorizontal: 20,
    marginBottom: 20,
  },
  sectionHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 16,
  },
  sectionTitle: {
    fontSize: 20,
    fontFamily: 'Inter-SemiBold',
    color: '#ffffff',
  },
  viewAllText: {
    fontSize: 16,
    fontFamily: 'Inter-Medium',
    color: '#e0e7ff',
  },
  emptyState: {
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 60,
    paddingHorizontal: 40,
  },
  emptyTitle: {
    fontSize: 20,
    fontFamily: 'Inter-SemiBold',
    color: 'rgba(255, 255, 255, 0.8)',
    marginTop: 16,
    marginBottom: 8,
  },
  emptySubtitle: {
    fontSize: 16,
    fontFamily: 'Inter-Regular',
    color: 'rgba(255, 255, 255, 0.6)',
    textAlign: 'center',
    lineHeight: 24,
  },
});