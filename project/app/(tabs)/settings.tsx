import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  Switch,
  Platform,
  Alert,
  ScrollView,
  TextInput,
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { Bell, Shield, Download, Trash2, Info, ChevronRight, Smartphone, Wifi, Upload, Settings as SettingsIcon, Moon, Sun, Volume2, VolumeX, Vibrate, FileText, CircleHelp as HelpCircle, Star, Github, Mail } from 'lucide-react-native';
import { NFCManager } from '@/components/NFCManager';

interface AppSettings {
  notifications: boolean;
  autoSave: boolean;
  vibration: boolean;
  soundEnabled: boolean;
  theme: 'light' | 'dark' | 'auto';
  scanTimeout: number;
  maxHistorySize: number;
}

export default function SettingsScreen() {
  const [settings, setSettings] = useState<AppSettings>({
    notifications: true,
    autoSave: true,
    vibration: true,
    soundEnabled: true,
    theme: 'auto',
    scanTimeout: 30,
    maxHistorySize: 100,
  });
  const [nfcSupported, setNfcSupported] = useState(false);
  const [nfcEnabled, setNfcEnabled] = useState(false);
  const [historyCount, setHistoryCount] = useState(0);

  useEffect(() => {
    loadSettings();
    checkNFCStatus();
    loadHistoryCount();
  }, []);

  const loadSettings = async () => {
    try {
      const savedSettings = await NFCManager.getSettings();
      setSettings({ ...settings, ...savedSettings });
    } catch (error) {
      console.error('Failed to load settings:', error);
    }
  };

  const saveSettings = async (newSettings: AppSettings) => {
    try {
      await NFCManager.saveSettings(newSettings);
      setSettings(newSettings);
    } catch (error) {
      console.error('Failed to save settings:', error);
      Alert.alert('Error', 'Failed to save settings');
    }
  };

  const checkNFCStatus = async () => {
    try {
      const supported = await NFCManager.isNFCSupported();
      const enabled = await NFCManager.isNFCEnabled();
      setNfcSupported(supported);
      setNfcEnabled(enabled);
    } catch (error) {
      console.error('Failed to check NFC status:', error);
    }
  };

  const loadHistoryCount = async () => {
    try {
      const history = await NFCManager.getHistory();
      setHistoryCount(history.length);
    } catch (error) {
      console.error('Failed to load history count:', error);
    }
  };

  const updateSetting = (key: keyof AppSettings, value: any) => {
    const newSettings = { ...settings, [key]: value };
    saveSettings(newSettings);
  };

  const exportData = async () => {
    try {
      const exportData = await NFCManager.exportHistory();
      
      if (Platform.OS === 'web') {
        const blob = new Blob([exportData], { type: 'application/json' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `nfc-scanner-backup-${new Date().toISOString().split('T')[0]}.json`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
        
        Alert.alert('Export Complete', 'Your NFC data has been downloaded as a backup file.');
      }
    } catch (error) {
      console.error('Export failed:', error);
      Alert.alert('Export Failed', 'Unable to export your data.');
    }
  };

  const importData = () => {
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
            await loadHistoryCount();
            Alert.alert(
              'Import Complete', 
              `Successfully imported ${importedCount} new tags from backup.`
            );
          } catch (error) {
            console.error('Import failed:', error);
            Alert.alert('Import Failed', 'Invalid backup file or corrupted data.');
          }
        }
      };
      input.click();
    }
  };

  const clearAllData = () => {
    Alert.alert(
      'Clear All Data',
      'This will permanently delete all your scanned NFC tags and reset all settings. This action cannot be undone.',
      [
        { text: 'Cancel', style: 'cancel' },
        {
          text: 'Clear All',
          style: 'destructive',
          onPress: async () => {
            try {
              await NFCManager.clearHistory();
              const defaultSettings: AppSettings = {
                notifications: true,
                autoSave: true,
                vibration: true,
                soundEnabled: true,
                theme: 'auto',
                scanTimeout: 30,
                maxHistorySize: 100,
              };
              await saveSettings(defaultSettings);
              await loadHistoryCount();
              Alert.alert('Data Cleared', 'All your data and settings have been reset.');
            } catch (error) {
              console.error('Clear failed:', error);
              Alert.alert('Clear Failed', 'Unable to clear your data.');
            }
          },
        },
      ]
    );
  };

  const showAbout = () => {
    Alert.alert(
      'About NFC Scanner',
      'NFC Scanner v2.0.0\n\nA comprehensive NFC tag scanner, manager, and emulator with advanced features.\n\nFeatures:\n• Scan and read NFC tags\n• View detailed tag information\n• Save scan history\n• Emulate existing tags\n• Write data to new tags\n• Export/import data\n• Advanced search and filtering\n\nBuilt with React Native, Expo, and Web NFC API.',
      [{ text: 'OK' }]
    );
  };

  const showPrivacyPolicy = () => {
    Alert.alert(
      'Privacy Policy',
      'Your privacy is our top priority:\n\n• All NFC data is stored locally on your device\n• No data is transmitted to external servers\n• No personal information is collected\n• Export/import features work entirely offline\n• You have full control over your data\n\nThis app does not require internet connectivity and works completely offline to protect your privacy.',
      [{ text: 'OK' }]
    );
  };

  const showTermsOfService = () => {
    Alert.alert(
      'Terms of Service',
      'By using NFC Scanner, you agree to:\n\n• Use the app responsibly and legally\n• Not use it for malicious purposes\n• Respect others\' privacy when scanning tags\n• Understand that NFC functionality depends on device capabilities\n• Take responsibility for any data you write to NFC tags\n\nThe app is provided "as is" without warranties. Use at your own risk.',
      [{ text: 'OK' }]
    );
  };

  const showHelp = () => {
    Alert.alert(
      'Help & Support',
      'Need help with NFC Scanner?\n\n• Make sure NFC is enabled on your device\n• Use Chrome browser on Android for best compatibility\n• Hold tags close to your device (within 4cm)\n• Some tags may be read-only or encrypted\n• Virtual tags are stored locally on your device\n\nFor technical support, please contact us through the app store or our website.',
      [{ text: 'Got it!' }]
    );
  };

  const rateApp = () => {
    Alert.alert(
      'Rate NFC Scanner',
      'Enjoying NFC Scanner? Please consider rating us on the app store to help other users discover our app!',
      [
        { text: 'Maybe Later', style: 'cancel' },
        { text: 'Rate Now', onPress: () => {
          // In a real app, this would open the app store
          Alert.alert('Thank you!', 'This would open the app store in a real app.');
        }}
      ]
    );
  };

  const contactSupport = () => {
    Alert.alert(
      'Contact Support',
      'Need help or have feedback? We\'d love to hear from you!\n\nEmail: support@nfcscanner.app\nWebsite: www.nfcscanner.app\n\nWe typically respond within 24 hours.',
      [{ text: 'OK' }]
    );
  };

  const SettingItem = ({ 
    icon, 
    title, 
    subtitle, 
    onPress, 
    rightElement, 
    destructive = false 
  }: {
    icon: React.ReactNode;
    title: string;
    subtitle?: string;
    onPress?: () => void;
    rightElement?: React.ReactNode;
    destructive?: boolean;
  }) => (
    <TouchableOpacity
      style={styles.settingItem}
      onPress={onPress}
      disabled={!onPress}
    >
      <View style={styles.settingLeft}>
        <View style={[
          styles.iconContainer,
          destructive && styles.destructiveIcon
        ]}>
          {icon}
        </View>
        <View style={styles.settingText}>
          <Text style={[
            styles.settingTitle,
            destructive && styles.destructiveText
          ]}>
            {title}
          </Text>
          {subtitle && (
            <Text style={styles.settingSubtitle}>{subtitle}</Text>
          )}
        </View>
      </View>
      {rightElement || (onPress && (
        <ChevronRight size={20} color="#9ca3af" />
      ))}
    </TouchableOpacity>
  );

  const getThemeIcon = () => {
    switch (settings.theme) {
      case 'light': return <Sun size={20} color="#6366f1" />;
      case 'dark': return <Moon size={20} color="#6366f1" />;
      default: return <SettingsIcon size={20} color="#6366f1" />;
    }
  };

  return (
    <LinearGradient
      colors={['#f8fafc', '#e2e8f0']}
      style={styles.container}
    >
      <View style={styles.header}>
        <Text style={styles.title}>Settings</Text>
        <Text style={styles.subtitle}>
          Customize your NFC scanning experience
        </Text>
      </View>

      <ScrollView style={styles.content} showsVerticalScrollIndicator={false}>
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Scanning Preferences</Text>
          
          <SettingItem
            icon={<Bell size={20} color="#6366f1" />}
            title="Notifications"
            subtitle="Get notified when tags are scanned"
            rightElement={
              <Switch
                value={settings.notifications}
                onValueChange={(value) => updateSetting('notifications', value)}
                trackColor={{ false: '#d1d5db', true: '#a5b4fc' }}
                thumbColor={settings.notifications ? '#6366f1' : '#f3f4f6'}
              />
            }
          />
          
          <SettingItem
            icon={<Download size={20} color="#6366f1" />}
            title="Auto-save Tags"
            subtitle="Automatically save scanned tags to history"
            rightElement={
              <Switch
                value={settings.autoSave}
                onValueChange={(value) => updateSetting('autoSave', value)}
                trackColor={{ false: '#d1d5db', true: '#a5b4fc' }}
                thumbColor={settings.autoSave ? '#6366f1' : '#f3f4f6'}
              />
            }
          />
          
          <SettingItem
            icon={<Vibrate size={20} color="#6366f1" />}
            title="Vibration Feedback"
            subtitle="Vibrate when tags are detected"
            rightElement={
              <Switch
                value={settings.vibration}
                onValueChange={(value) => updateSetting('vibration', value)}
                trackColor={{ false: '#d1d5db', true: '#a5b4fc' }}
                thumbColor={settings.vibration ? '#6366f1' : '#f3f4f6'}
              />
            }
          />

          <SettingItem
            icon={settings.soundEnabled ? <Volume2 size={20} color="#6366f1" /> : <VolumeX size={20} color="#6366f1" />}
            title="Sound Effects"
            subtitle="Play sounds for scan events"
            rightElement={
              <Switch
                value={settings.soundEnabled}
                onValueChange={(value) => updateSetting('soundEnabled', value)}
                trackColor={{ false: '#d1d5db', true: '#a5b4fc' }}
                thumbColor={settings.soundEnabled ? '#6366f1' : '#f3f4f6'}
              />
            }
          />
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Advanced Settings</Text>
          
          <View style={styles.settingItem}>
            <View style={styles.settingLeft}>
              <View style={styles.iconContainer}>
                <SettingsIcon size={20} color="#6366f1" />
              </View>
              <View style={styles.settingText}>
                <Text style={styles.settingTitle}>Scan Timeout</Text>
                <Text style={styles.settingSubtitle}>Seconds to wait for NFC tag</Text>
              </View>
            </View>
            <TextInput
              style={styles.numberInput}
              value={settings.scanTimeout.toString()}
              onChangeText={(text) => {
                const num = parseInt(text) || 30;
                updateSetting('scanTimeout', Math.max(5, Math.min(120, num)));
              }}
              keyboardType="numeric"
              maxLength={3}
            />
          </View>

          <View style={styles.settingItem}>
            <View style={styles.settingLeft}>
              <View style={styles.iconContainer}>
                <SettingsIcon size={20} color="#6366f1" />
              </View>
              <View style={styles.settingText}>
                <Text style={styles.settingTitle}>Max History Size</Text>
                <Text style={styles.settingSubtitle}>Maximum number of tags to keep</Text>
              </View>
            </View>
            <TextInput
              style={styles.numberInput}
              value={settings.maxHistorySize.toString()}
              onChangeText={(text) => {
                const num = parseInt(text) || 100;
                updateSetting('maxHistorySize', Math.max(10, Math.min(1000, num)));
              }}
              keyboardType="numeric"
              maxLength={4}
            />
          </View>
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Data Management</Text>
          
          <SettingItem
            icon={<Download size={20} color="#10b981" />}
            title="Export Data"
            subtitle={`Backup ${historyCount} scanned tags as JSON`}
            onPress={exportData}
          />
          
          <SettingItem
            icon={<Upload size={20} color="#6366f1" />}
            title="Import Data"
            subtitle="Restore from backup file"
            onPress={importData}
          />
          
          <SettingItem
            icon={<Trash2 size={20} color="#ef4444" />}
            title="Clear All Data"
            subtitle="Delete all tags and reset settings"
            onPress={clearAllData}
            destructive
          />
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Help & Feedback</Text>
          
          <SettingItem
            icon={<HelpCircle size={20} color="#6366f1" />}
            title="Help & Support"
            subtitle="Get help with using the app"
            onPress={showHelp}
          />
          
          <SettingItem
            icon={<Star size={20} color="#f59e0b" />}
            title="Rate This App"
            subtitle="Help others discover NFC Scanner"
            onPress={rateApp}
          />
          
          <SettingItem
            icon={<Mail size={20} color="#6366f1" />}
            title="Contact Support"
            subtitle="Send feedback or report issues"
            onPress={contactSupport}
          />
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>About & Legal</Text>
          
          <SettingItem
            icon={<Info size={20} color="#6366f1" />}
            title="About NFC Scanner"
            subtitle="Version 2.0.0 • Learn more"
            onPress={showAbout}
          />
          
          <SettingItem
            icon={<Shield size={20} color="#6366f1" />}
            title="Privacy Policy"
            subtitle="How we protect your data"
            onPress={showPrivacyPolicy}
          />

          <SettingItem
            icon={<FileText size={20} color="#6366f1" />}
            title="Terms of Service"
            subtitle="Usage terms and conditions"
            onPress={showTermsOfService}
          />

          <SettingItem
            icon={<Github size={20} color="#6366f1" />}
            title="Open Source"
            subtitle="View source code and contribute"
            onPress={() => Alert.alert('Open Source', 'This app is open source! Visit our GitHub repository to view the code and contribute.')}
          />
        </View>

        <View style={styles.deviceInfo}>
          <Text style={styles.deviceInfoTitle}>Device Information</Text>
          
          <View style={styles.deviceInfoItem}>
            <Wifi size={16} color="#6b7280" />
            <Text style={styles.deviceInfoText}>
              NFC Support: {nfcSupported ? 'Available' : 'Not Available'}
            </Text>
          </View>
          
          <View style={styles.deviceInfoItem}>
            <Smartphone size={16} color="#6b7280" />
            <Text style={styles.deviceInfoText}>
              NFC Status: {nfcEnabled ? 'Enabled' : 'Disabled'}
            </Text>
          </View>
          
          <View style={styles.deviceInfoItem}>
            <SettingsIcon size={16} color="#6b7280" />
            <Text style={styles.deviceInfoText}>
              Platform: {Platform.OS === 'web' ? 'Web (Chrome/Android)' : Platform.OS}
            </Text>
          </View>
          
          <View style={styles.deviceInfoItem}>
            <FileText size={16} color="#6b7280" />
            <Text style={styles.deviceInfoText}>
              Tags Stored: {historyCount} / {settings.maxHistorySize}
            </Text>
          </View>
        </View>

        <View style={styles.footer}>
          <Text style={styles.footerText}>
            NFC Scanner • Made with ❤️ for NFC enthusiasts
          </Text>
          <Text style={styles.footerSubtext}>
            Open source • Privacy focused • Offline first
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
  content: {
    flex: 1,
  },
  section: {
    marginBottom: 32,
  },
  sectionTitle: {
    fontSize: 18,
    fontFamily: 'Inter-SemiBold',
    color: '#374151',
    marginBottom: 16,
    marginHorizontal: 20,
  },
  settingItem: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    backgroundColor: '#ffffff',
    marginHorizontal: 20,
    marginBottom: 2,
    paddingVertical: 16,
    paddingHorizontal: 16,
    borderRadius: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 2,
    elevation: 1,
  },
  settingLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    flex: 1,
  },
  iconContainer: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: '#f3f4f6',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 12,
  },
  destructiveIcon: {
    backgroundColor: '#fef2f2',
  },
  settingText: {
    flex: 1,
  },
  settingTitle: {
    fontSize: 16,
    fontFamily: 'Inter-Medium',
    color: '#1f2937',
    marginBottom: 2,
  },
  destructiveText: {
    color: '#ef4444',
  },
  settingSubtitle: {
    fontSize: 14,
    fontFamily: 'Inter-Regular',
    color: '#6b7280',
  },
  numberInput: {
    backgroundColor: '#f9fafb',
    borderWidth: 1,
    borderColor: '#e5e7eb',
    borderRadius: 8,
    paddingHorizontal: 12,
    paddingVertical: 8,
    fontSize: 16,
    fontFamily: 'Inter-Regular',
    color: '#1f2937',
    textAlign: 'center',
    minWidth: 60,
  },
  deviceInfo: {
    marginHorizontal: 20,
    marginBottom: 20,
    padding: 16,
    backgroundColor: '#f9fafb',
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#e5e7eb',
  },
  deviceInfoTitle: {
    fontSize: 16,
    fontFamily: 'Inter-SemiBold',
    color: '#374151',
    marginBottom: 12,
  },
  deviceInfoItem: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 8,
  },
  deviceInfoText: {
    fontSize: 14,
    fontFamily: 'Inter-Regular',
    color: '#6b7280',
    marginLeft: 8,
  },
  footer: {
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingBottom: 40,
  },
  footerText: {
    fontSize: 14,
    fontFamily: 'Inter-Medium',
    color: '#6b7280',
    textAlign: 'center',
    marginBottom: 4,
  },
  footerSubtext: {
    fontSize: 12,
    fontFamily: 'Inter-Regular',
    color: '#9ca3af',
    textAlign: 'center',
  },
});