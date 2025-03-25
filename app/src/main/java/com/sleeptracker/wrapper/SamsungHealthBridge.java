package com.sleeptracker.wrapper;

import android.app.Activity;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.samsung.android.sdk.healthdata.HealthConnectionErrorResult;
import com.samsung.android.sdk.healthdata.HealthConstants;
import com.samsung.android.sdk.healthdata.HealthData;
import com.samsung.android.sdk.healthdata.HealthDataResolver;
import com.samsung.android.sdk.healthdata.HealthDataStore;
import com.samsung.android.sdk.healthdata.HealthPermissionManager;
import com.samsung.android.sdk.healthdata.HealthResultHolder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

/**
 * SamsungHealthBridge provides a bridge between the web application and Samsung Health SDK.
 * This class handles all communication between the web app and Samsung Health data,
 * including initialization, permission management, and data retrieval.
 */
public class SamsungHealthBridge {
    private static final String TAG = "SamsungHealthBridge";
    private final Activity activity;
    private final HealthDataStore healthDataStore;
    private HealthDataResolver healthDataResolver;
    private final WebViewCallback callback;

    /**
     * Interface for handling callbacks from Samsung Health operations.
     * Used to communicate results back to the web application.
     */
    public interface WebViewCallback {
        void onSuccess(String functionName, String result);
        void onError(String functionName, String error);
    }

    /**
     * Constructor initializes the bridge with the required components.
     * @param activity The Android Activity context
     * @param callback Callback interface for communicating with the web app
     */
    public SamsungHealthBridge(Activity activity, WebViewCallback callback) {
        this.activity = activity;
        this.callback = callback;
        this.healthDataStore = new HealthDataStore(activity, connectionListener);
    }

    /**
     * Connection listener for Samsung Health data store.
     * Handles connection events and initializes the data resolver.
     */
    private final HealthDataStore.ConnectionListener connectionListener = new HealthDataStore.ConnectionListener() {
        @Override
        public void onConnected() {
            healthDataResolver = new HealthDataResolver(healthDataStore, null);
            Log.d(TAG, "Health data store connected");
            callback.onSuccess("initialize", "true");
        }

        @Override
        public void onConnectionFailed(HealthConnectionErrorResult error) {
            Log.e(TAG, "Health data store connection failed", error.getException());
            callback.onError("initialize", error.toString());
        }

        @Override
        public void onDisconnected() {
            Log.d(TAG, "Health data store disconnected");
            healthDataResolver = null;
        }
    };

    /**
     * Initializes the connection to Samsung Health.
     * Called from the web application to establish the connection.
     */
    @JavascriptInterface
    public void initialize() {
        healthDataStore.connectService();
    }

    /**
     * Requests permissions for accessing specific health data types.
     * @param permissions Array of permission strings ("sleep", "heart_rate", "motion")
     */
    @JavascriptInterface
    public void requestPermissions(String[] permissions) {
        Set<HealthPermissionManager.PermissionKey> permissionKeys = new HashSet<>();
        
        // Map web app permission strings to Samsung Health permission keys
        for (String permission : permissions) {
            switch (permission) {
                case "sleep":
                    permissionKeys.add(new HealthPermissionManager.PermissionKey(
                            HealthConstants.Sleep.HEALTH_DATA_TYPE, HealthPermissionManager.PermissionType.READ));
                    break;
                case "heart_rate":
                    permissionKeys.add(new HealthPermissionManager.PermissionKey(
                            HealthConstants.HeartRate.HEALTH_DATA_TYPE, HealthPermissionManager.PermissionType.READ));
                    break;
                case "motion":
                    permissionKeys.add(new HealthPermissionManager.PermissionKey(
                            HealthConstants.PhysicalActivity.HEALTH_DATA_TYPE, HealthPermissionManager.PermissionType.READ));
                    break;
            }
        }

        // Request permissions from Samsung Health
        HealthPermissionManager pmsManager = new HealthPermissionManager(healthDataStore);
        try {
            pmsManager.requestPermissions(permissionKeys, activity).setResultListener(result -> {
                JSONObject response = new JSONObject();
                for (String permission : permissions) {
                    try {
                        response.put(permission, result.isGranted(getPermissionKey(permission)) ? "granted" : "denied");
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing permission result", e);
                    }
                }
                callback.onSuccess("requestPermissions", response.toString());
            });
        } catch (Exception e) {
            callback.onError("requestPermissions", e.getMessage());
        }
    }

    /**
     * Helper method to get the appropriate permission key for a given permission string.
     * @param permission The permission string from the web app
     * @return The corresponding Samsung Health permission key
     */
    private HealthPermissionManager.PermissionKey getPermissionKey(String permission) {
        switch (permission) {
            case "sleep":
                return new HealthPermissionManager.PermissionKey(
                        HealthConstants.Sleep.HEALTH_DATA_TYPE, HealthPermissionManager.PermissionType.READ);
            case "heart_rate":
                return new HealthPermissionManager.PermissionKey(
                        HealthConstants.HeartRate.HEALTH_DATA_TYPE, HealthPermissionManager.PermissionType.READ);
            case "motion":
                return new HealthPermissionManager.PermissionKey(
                        HealthConstants.PhysicalActivity.HEALTH_DATA_TYPE, HealthPermissionManager.PermissionType.READ);
            default:
                return null;
        }
    }

    /**
     * Retrieves sleep data from Samsung Health for the specified time range.
     * @param startTime Start time in milliseconds since epoch
     * @param endTime End time in milliseconds since epoch
     */
    @JavascriptInterface
    public void getSleepData(long startTime, long endTime) {
        HealthDataResolver.ReadRequest request = new HealthDataResolver.ReadRequest.Builder()
                .setDataType(HealthConstants.Sleep.HEALTH_DATA_TYPE)
                .setTimeRange(startTime, endTime, HealthDataResolver.TimeUnit.MILLISECOND)
                .build();

        try {
            healthDataResolver.read(request).setResultListener(result -> {
                try {
                    JSONArray sleepData = new JSONArray();
                    HealthData data;
                    while ((data = result.next()) != null) {
                        JSONObject sleepEntry = new JSONObject();
                        sleepEntry.put("startTime", data.getLong(HealthConstants.Sleep.START_TIME));
                        sleepEntry.put("endTime", data.getLong(HealthConstants.Sleep.END_TIME));
                        sleepEntry.put("stage", data.getInt(HealthConstants.Sleep.STAGE));
                        sleepData.put(sleepEntry);
                    }
                    callback.onSuccess("getSleepData", sleepData.toString());
                } catch (Exception e) {
                    callback.onError("getSleepData", e.getMessage());
                }
            });
        } catch (Exception e) {
            callback.onError("getSleepData", e.getMessage());
        }
    }

    /**
     * Retrieves heart rate data from Samsung Health for the specified time range.
     * @param startTime Start time in milliseconds since epoch
     * @param endTime End time in milliseconds since epoch
     */
    @JavascriptInterface
    public void getHeartRateData(long startTime, long endTime) {
        HealthDataResolver.ReadRequest request = new HealthDataResolver.ReadRequest.Builder()
                .setDataType(HealthConstants.HeartRate.HEALTH_DATA_TYPE)
                .setTimeRange(startTime, endTime, HealthDataResolver.TimeUnit.MILLISECOND)
                .build();

        try {
            healthDataResolver.read(request).setResultListener(result -> {
                try {
                    JSONArray heartRateData = new JSONArray();
                    HealthData data;
                    while ((data = result.next()) != null) {
                        JSONObject heartRateEntry = new JSONObject();
                        heartRateEntry.put("time", data.getLong(HealthConstants.HeartRate.START_TIME));
                        heartRateEntry.put("rate", data.getInt(HealthConstants.HeartRate.HEART_RATE));
                        heartRateData.put(heartRateEntry);
                    }
                    callback.onSuccess("getHeartRateData", heartRateData.toString());
                } catch (Exception e) {
                    callback.onError("getHeartRateData", e.getMessage());
                }
            });
        } catch (Exception e) {
            callback.onError("getHeartRateData", e.getMessage());
        }
    }

    /**
     * Retrieves motion/activity data from Samsung Health for the specified time range.
     * @param startTime Start time in milliseconds since epoch
     * @param endTime End time in milliseconds since epoch
     */
    @JavascriptInterface
    public void getMotionData(long startTime, long endTime) {
        HealthDataResolver.ReadRequest request = new HealthDataResolver.ReadRequest.Builder()
                .setDataType(HealthConstants.PhysicalActivity.HEALTH_DATA_TYPE)
                .setTimeRange(startTime, endTime, HealthDataResolver.TimeUnit.MILLISECOND)
                .build();

        try {
            healthDataResolver.read(request).setResultListener(result -> {
                try {
                    JSONArray motionData = new JSONArray();
                    HealthData data;
                    while ((data = result.next()) != null) {
                        JSONObject motionEntry = new JSONObject();
                        motionEntry.put("startTime", data.getLong(HealthConstants.PhysicalActivity.START_TIME));
                        motionEntry.put("endTime", data.getLong(HealthConstants.PhysicalActivity.END_TIME));
                        motionEntry.put("type", data.getInt(HealthConstants.PhysicalActivity.ACTIVITY_TYPE));
                        motionEntry.put("duration", data.getLong(HealthConstants.PhysicalActivity.DURATION));
                        motionData.put(motionEntry);
                    }
                    callback.onSuccess("getMotionData", motionData.toString());
                } catch (Exception e) {
                    callback.onError("getMotionData", e.getMessage());
                }
            });
        } catch (Exception e) {
            callback.onError("getMotionData", e.getMessage());
        }
    }

    /**
     * Disconnects from the Samsung Health service.
     * Should be called when the application is closing or the bridge is no longer needed.
     */
    public void disconnect() {
        if (healthDataStore != null) {
            healthDataStore.disconnectService();
        }
    }
} 