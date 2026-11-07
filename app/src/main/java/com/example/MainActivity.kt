package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.ui.TootPlayerApp
import com.example.ui.theme.TootPlayerTheme
import com.example.util.CrashlyticsHelper

class MainActivity : ComponentActivity() {

    private val requestStartupPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { _ ->
            // Permissions handled; ViewModel will scan device audio files
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            CrashlyticsHelper.init(this)
            
            val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                android.util.Log.e("MainActivity", "Uncaught exception on thread ${thread.name}: $throwable", throwable)
                CrashlyticsHelper.recordException(throwable, "Uncaught exception on thread ${thread.name}")
                defaultHandler?.uncaughtException(thread, throwable)
            }

            enableEdgeToEdge()

            val permissionsToRequest = mutableListOf<String>()

            val audioPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_AUDIO
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }
            if (ContextCompat.checkSelfPermission(this, audioPermission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(audioPermission)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            if (permissionsToRequest.isNotEmpty()) {
                try {
                    requestStartupPermissionsLauncher.launch(permissionsToRequest.toTypedArray())
                } catch (e: Exception) {
                    // Non-blocking
                }
            }

            setContent {
                TootPlayerApp()
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Fatal error in onCreate: $e", e)
            CrashlyticsHelper.recordException(e, "Fatal error in MainActivity.onCreate")
            setContent {
                TootPlayerApp()
            }
        }
    }
}

