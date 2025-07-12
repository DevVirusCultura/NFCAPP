# Keep all classes in the creditCardNfcReader package
-keep class com.peerbits.creditCardNfcReader.** { *; }

# Keep NFC related classes
-keep class android.nfc.** { *; }

# Keep Apache Commons classes
-keep class org.apache.commons.** { *; }

# Keep SLF4J classes
-keep class org.slf4j.** { *; }