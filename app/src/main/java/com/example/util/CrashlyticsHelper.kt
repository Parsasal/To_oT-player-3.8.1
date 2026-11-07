package com.example.util

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Centralized utility for Firebase Crashlytics monitoring, custom metadata,
 * breadcrumb logging, and caught exception reporting.
 */
object CrashlyticsHelper {

    private const val TAG = "CrashlyticsHelper"
    @Volatile
    private var isAvailable: Boolean = false

    /**
     * Initializes Crashlytics configuration on app launch.
     * Sets standard metadata keys such as version name, version code, and release status.
     */
    fun init(context: Context) {
        try {
            val appContext = context.applicationContext
            if (FirebaseApp.getApps(appContext).isEmpty()) {
                val app = FirebaseApp.initializeApp(appContext)
                if (app == null) {
                    Log.w(TAG, "FirebaseApp could not be initialized (no google-services config). Crashlytics disabled.")
                    isAvailable = false
                    return
                }
            }

            val crashlytics = FirebaseCrashlytics.getInstance()
            
            // Enable Crashlytics collection
            crashlytics.setCrashlyticsCollectionEnabled(true)

            // Custom Keys for detailed crash telemetry
            crashlytics.setCustomKey("version_name", AppInfo.VERSION_NAME)
            crashlytics.setCustomKey("version_code", AppInfo.VERSION_CODE)
            crashlytics.setCustomKey("build_type", if (BuildConfig.DEBUG) "debug" else "release")
            crashlytics.setCustomKey("app_id", "com.tootplayer.audiohub")

            isAvailable = true
            log("Crashlytics initialized successfully for TO_OT Player v${AppInfo.VERSION_NAME}")
        } catch (e: Exception) {
            isAvailable = false
            Log.w(TAG, "Firebase Crashlytics is not available: ${e.message}")
        }
    }

    /**
     * Records a custom breadcrumb log message in Crashlytics.
     */
    fun log(message: String) {
        if (!isAvailable) {
            Log.d(TAG, message)
            return
        }
        try {
            FirebaseCrashlytics.getInstance().log(message)
            Log.d(TAG, "[Breadcrumb] $message")
        } catch (e: Exception) {
            Log.d(TAG, message)
        }
    }

    /**
     * Sets a custom key-value pair for enhanced debugging context in crash reports.
     */
    fun setCustomKey(key: String, value: String) {
        if (!isAvailable) return
        try {
            FirebaseCrashlytics.getInstance().setCustomKey(key, value)
        } catch (e: Exception) {
            Log.w(TAG, "Could not set custom key: $key=$value", e)
        }
    }

    fun setCustomKey(key: String, value: Int) {
        if (!isAvailable) return
        try {
            FirebaseCrashlytics.getInstance().setCustomKey(key, value)
        } catch (e: Exception) {
            Log.w(TAG, "Could not set custom key: $key=$value", e)
        }
    }

    fun setCustomKey(key: String, value: Boolean) {
        if (!isAvailable) return
        try {
            FirebaseCrashlytics.getInstance().setCustomKey(key, value)
        } catch (e: Exception) {
            Log.w(TAG, "Could not set custom key: $key=$value", e)
        }
    }

    /**
     * Records a non-fatal caught exception with optional contextual message.
     */
    fun recordException(throwable: Throwable, contextMessage: String? = null) {
        if (!isAvailable) {
            Log.e(TAG, "Exception [Crashlytics offline]: ${contextMessage ?: ""} ${throwable.message}", throwable)
            return
        }
        try {
            if (!contextMessage.isNullOrBlank()) {
                FirebaseCrashlytics.getInstance().log("Context: $contextMessage")
            }
            FirebaseCrashlytics.getInstance().recordException(throwable)
            Log.e(TAG, "Recorded non-fatal exception to Crashlytics: ${throwable.message}", throwable)
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${throwable.message}", throwable)
        }
    }

    /**
     * Sets user identifier (anonymous or unique session) if needed.
     */
    fun setUserId(userId: String) {
        if (!isAvailable) return
        try {
            FirebaseCrashlytics.getInstance().setUserId(userId)
        } catch (e: Exception) {
            Log.w(TAG, "Could not set user ID", e)
        }
    }
}
