import React, { useEffect } from 'react';
import { View, StyleSheet } from 'react-native';
import Animated, {
  useSharedValue,
  useAnimatedStyle,
  withRepeat,
  withTiming,
  withSequence,
  Easing,
} from 'react-native-reanimated';

interface ScanAnimationProps {
  isScanning: boolean;
  size?: number;
}

export function ScanAnimation({ isScanning, size = 200 }: ScanAnimationProps) {
  const scale = useSharedValue(1);
  const opacity = useSharedValue(0.3);
  const rotation = useSharedValue(0);

  useEffect(() => {
    if (isScanning) {
      // Pulse animation
      scale.value = withRepeat(
        withSequence(
          withTiming(1.2, { duration: 1000, easing: Easing.inOut(Easing.ease) }),
          withTiming(1, { duration: 1000, easing: Easing.inOut(Easing.ease) })
        ),
        -1,
        false
      );

      // Opacity animation
      opacity.value = withRepeat(
        withSequence(
          withTiming(0.8, { duration: 1000, easing: Easing.inOut(Easing.ease) }),
          withTiming(0.3, { duration: 1000, easing: Easing.inOut(Easing.ease) })
        ),
        -1,
        false
      );

      // Rotation animation
      rotation.value = withRepeat(
        withTiming(360, { duration: 3000, easing: Easing.linear }),
        -1,
        false
      );
    } else {
      scale.value = withTiming(1, { duration: 300 });
      opacity.value = withTiming(0.3, { duration: 300 });
      rotation.value = withTiming(0, { duration: 300 });
    }
  }, [isScanning]);

  const animatedOuterStyle = useAnimatedStyle(() => ({
    transform: [
      { scale: scale.value },
      { rotate: `${rotation.value}deg` },
    ],
    opacity: opacity.value,
  }));

  const animatedInnerStyle = useAnimatedStyle(() => ({
    transform: [
      { scale: isScanning ? 1.1 : 1 },
    ],
  }));

  return (
    <View style={[styles.container, { width: size, height: size }]}>
      <Animated.View
        style={[
          styles.outerCircle,
          { width: size, height: size, borderRadius: size / 2 },
          animatedOuterStyle,
        ]}
      />
      <Animated.View
        style={[
          styles.innerCircle,
          { 
            width: size * 0.8, 
            height: size * 0.8, 
            borderRadius: (size * 0.8) / 2 
          },
          animatedInnerStyle,
        ]}
      />
      <View
        style={[
          styles.centerDot,
          { 
            width: size * 0.1, 
            height: size * 0.1, 
            borderRadius: (size * 0.1) / 2 
          },
        ]}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    alignItems: 'center',
    justifyContent: 'center',
  },
  outerCircle: {
    position: 'absolute',
    borderWidth: 2,
    borderColor: 'rgba(255, 255, 255, 0.4)',
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
  },
  innerCircle: {
    position: 'absolute',
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.6)',
    backgroundColor: 'rgba(255, 255, 255, 0.05)',
  },
  centerDot: {
    backgroundColor: 'rgba(255, 255, 255, 0.8)',
  },
});