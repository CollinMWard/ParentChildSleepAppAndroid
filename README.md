# Sleep Tracker Android Wrapper

This Android application serves as a wrapper for a web-based sleep tracking application, providing integration with Samsung Health SDK. The wrapper uses WebView to host the web application and provides a JavaScript bridge to access Samsung Health data.

## Prerequisites

- Android Studio 4.2 or higher
- Android SDK 29 or higher
- Samsung Health SDK (included in dependencies)
- A Samsung device with Samsung Health app installed
- Samsung Health SDK Partner ID (obtain from Samsung Developer Portal)

## Setup

1. Clone the repository
2. Open the project in Android Studio
3. Update the `app/build.gradle` file with your application ID and Samsung Health SDK credentials
4. Update the web application URL in the debug and release build configurations
5. Build and run the application

## Web Application Integration

The wrapper provides a JavaScript interface that matches the following TypeScript definition:

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

The bridge uses custom events to communicate results back to the web application. Subscribe to these events in your web application:

```javascript
// Success events
window.addEventListener('SamsungHealthBridge_initialize_success', (e) => {
  console.log('Initialization success:', e.detail);
});

window.addEventListener('SamsungHealthBridge_requestPermissions_success', (e) => {
  console.log('Permissions granted:', e.detail);
});

// Error events
window.addEventListener('SamsungHealthBridge_initialize_error', (e) => {
  console.error('Initialization error:', e.detail);
});

window.addEventListener('SamsungHealthBridge_requestPermissions_error', (e) => {
  console.error('Permissions error:', e.detail);
});
```

### Usage Example

```javascript
// Initialize Samsung Health connection
await window.SamsungHealthBridge.initialize();

// Request necessary permissions
const permissions = await window.SamsungHealthBridge.requestPermissions([
  'sleep',
  'heart_rate',
  'motion'
]);

// Fetch sleep data for the last 24 hours
const endTime = Date.now();
const startTime = endTime - (24 * 60 * 60 * 1000);
const sleepData = await window.SamsungHealthBridge.getSleepData(startTime, endTime);
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

## Security Considerations

1. The wrapper uses HTTPS for production builds
2. JavaScript interface methods are properly annotated with @JavascriptInterface
3. WebView security settings are configured to prevent common vulnerabilities
4. Samsung Health permissions are requested at runtime
5. Sensitive data is not cached or stored locally

## Development Notes

- For development, the web application runs on `http://10.0.2.2:3000` (localhost for Android emulator)
- For production, update the `WEB_APP_URL` in the release build configuration
- Test the application on a real Samsung device with Samsung Health installed
- Ensure proper error handling in the web application for cases when Samsung Health is not available

## Troubleshooting

1. Samsung Health SDK not connecting:
   - Ensure Samsung Health app is installed and updated
   - Verify Samsung Health permissions are granted
   - Check if the device supports Samsung Health

2. WebView not loading:
   - Check internet connectivity
   - Verify the web application URL is correct
   - Check WebView console for JavaScript errors

3. Data not syncing:
   - Verify Samsung Health permissions are granted
   - Check time range parameters
   - Ensure Samsung Health has data for the requested period 