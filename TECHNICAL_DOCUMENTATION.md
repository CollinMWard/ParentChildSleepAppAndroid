# Sleep Tracker Android Wrapper - Technical Documentation

## System Overview

The Sleep Tracker Android Wrapper is a native Android application that serves as a bridge between a web-based sleep tracking application and the Samsung Health SDK. This wrapper enables the web application to access Samsung Health data (sleep, heart rate, and motion data) through a secure and controlled interface.

## Architecture

### Components

1. **WebView Container**
   - Hosts the web application
   - Provides secure communication between web and native code
   - Handles JavaScript interface injection

2. **Samsung Health Bridge**
   - Manages Samsung Health SDK integration
   - Handles permissions and data access
   - Provides JavaScript interface for web app communication

3. **Data Flow**
   ```
   Web Application <-> JavaScript Bridge <-> Samsung Health Bridge <-> Samsung Health SDK
   ```

### Key Features

1. **WebView Integration**
   - Secure WebView configuration
   - JavaScript interface injection
   - Custom event system for async operations

2. **Samsung Health Integration**
   - Permission management
   - Data type handling
   - Error handling and recovery

3. **Security Features**
   - HTTPS enforcement in production
   - Permission-based access control
   - Secure data transmission

## Integration Guide

### Web Application Integration

The web application can interact with Samsung Health through the provided JavaScript interface:

```typescript
interface Window {
  SamsungHealthBridge: {
    initialize(): Promise<boolean>;
    requestPermissions(permissions: string[]): Promise<Record<string, string>>;
    getSleepData(startTime: number, endTime: number): Promise<any>;
    getHeartRateData(startTime: number, endTime: number): Promise<any>;
    getMotionData(startTime: number, endTime: number): Promise<any>;
  };
}
```

### Event Handling

The bridge uses custom events to communicate results:

```javascript
// Success events
window.addEventListener('SamsungHealthBridge_initialize_success', (e) => {
  console.log('Initialization success:', e.detail);
});

// Error events
window.addEventListener('SamsungHealthBridge_initialize_error', (e) => {
  console.error('Initialization error:', e.detail);
});
```

## Data Types

### Sleep Data
```typescript
interface SleepData {
  startTime: number;  // milliseconds since epoch
  endTime: number;    // milliseconds since epoch
  stage: number;      // sleep stage (0: unknown, 1: awake, 2: light, 3: deep, 4: REM)
}
```

### Heart Rate Data
```typescript
interface HeartRateData {
  time: number;       // milliseconds since epoch
  rate: number;       // beats per minute
}
```

### Motion Data
```typescript
interface MotionData {
  startTime: number;  // milliseconds since epoch
  endTime: number;    // milliseconds since epoch
  type: number;       // activity type
  duration: number;   // duration in milliseconds
}
```

## Implementation Details

### SamsungHealthBridge Class

The `SamsungHealthBridge` class handles all communication with Samsung Health:

1. **Initialization**
   - Establishes connection with Samsung Health
   - Sets up data resolver
   - Handles connection events

2. **Permission Management**
   - Maps web app permissions to Samsung Health permissions
   - Handles permission requests
   - Provides permission status feedback

3. **Data Retrieval**
   - Implements time-range based queries
   - Handles data type conversion
   - Provides error handling

### MainActivity

The `MainActivity` manages the WebView and bridge:

1. **WebView Setup**
   - Configures security settings
   - Injects JavaScript interface
   - Handles navigation

2. **Bridge Integration**
   - Creates and manages bridge instance
   - Handles lifecycle events
   - Manages WebView callbacks

## Security Considerations

1. **WebView Security**
   - JavaScript interface protection
   - Mixed content handling
   - File access restrictions

2. **Data Security**
   - HTTPS enforcement
   - Permission validation
   - Data sanitization

3. **Samsung Health Security**
   - Permission-based access
   - Secure data transmission
   - Connection management

## Development Guidelines

### Building the Application

1. **Debug Build**
   ```bash
   ./gradlew assembleDebug
   ```
   - Uses localhost URL (10.0.2.2:3000)
   - Enables debugging features

2. **Release Build**
   ```bash
   ./gradlew assembleRelease
   ```
   - Uses production URL
   - Optimized for production

### Testing

1. **Development Testing**
   - Use Android emulator with localhost
   - Test with Samsung Health app installed
   - Verify permission handling

2. **Production Testing**
   - Test on real Samsung devices
   - Verify HTTPS connectivity
   - Test error scenarios

## Troubleshooting

### Common Issues

1. **Connection Issues**
   - Verify Samsung Health app installation
   - Check internet connectivity
   - Validate URL configuration

2. **Permission Issues**
   - Check Samsung Health permissions
   - Verify permission mapping
   - Test permission requests

3. **Data Access Issues**
   - Validate time ranges
   - Check data availability
   - Verify data format

## Maintenance

### Version Updates

1. **Web Application Updates**
   - Update URL in build.gradle
   - Test new features
   - Verify compatibility

2. **Samsung Health SDK Updates**
   - Update SDK version
   - Test new features
   - Verify backward compatibility

### Error Monitoring

1. **Logging**
   - Use TAG "SamsungHealthBridge"
   - Monitor connection events
   - Track permission changes

2. **Error Handling**
   - Implement proper error callbacks
   - Provide user feedback
   - Log error details

## Future Considerations

1. **Feature Additions**
   - Additional health data types
   - Enhanced error handling
   - Performance optimizations

2. **Security Enhancements**
   - Additional security measures
   - Enhanced data validation
   - Improved error reporting

3. **Integration Improvements**
   - Additional health platforms
   - Enhanced data processing
   - Improved user experience 