package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.service.AudioServiceBridge

class TootAppWidgetProvider : AppWidgetProvider() {

    companion object {
        private const val TAG = "TootAppWidgetProvider"
        const val ACTION_WIDGET_PLAY_PAUSE = "com.example.widget.ACTION_WIDGET_PLAY_PAUSE"
        const val ACTION_WIDGET_NEXT = "com.example.widget.ACTION_WIDGET_NEXT"
        const val ACTION_WIDGET_PREV = "com.example.widget.ACTION_WIDGET_PREV"

        private var lastIsPlaying = false
        private var lastTrackTitle = "No Track Playing"
        private var lastSessionName = "TO_OT Player"

        fun updateAllWidgets(
            context: Context,
            isPlaying: Boolean,
            trackTitle: String,
            sessionName: String
        ) {
            try {
                lastIsPlaying = isPlaying
                lastTrackTitle = trackTitle
                lastSessionName = sessionName

                val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
                val componentName = ComponentName(context, TootAppWidgetProvider::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
                    for (appWidgetId in appWidgetIds) {
                        updateAppWidget(context, appWidgetManager, appWidgetId, isPlaying, trackTitle, sessionName)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error in updateAllWidgets: $e")
            }
        }

        private fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            isPlaying: Boolean,
            trackTitle: String,
            sessionName: String
        ) {
            try {
                val views = RemoteViews(context.packageName, R.layout.widget_toot_player)

                // Set Text
                views.setTextViewText(R.id.widget_track_title, trackTitle)
                views.setTextViewText(R.id.widget_session_title, "TO_OT • $sessionName")

                // Play / Pause Icon
                val playPauseIcon = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow
                views.setImageViewResource(R.id.widget_btn_play_pause, playPauseIcon)

                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }

                // Click entire widget background to open MainActivity
                val openAppIntent = Intent(context, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                val openAppPendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    openAppIntent,
                    flags
                )
                views.setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent)

                // Click Previous
                val prevIntent = Intent(context, TootAppWidgetProvider::class.java).apply {
                    action = ACTION_WIDGET_PREV
                }
                val prevPendingIntent = PendingIntent.getBroadcast(
                    context,
                    1,
                    prevIntent,
                    flags
                )
                views.setOnClickPendingIntent(R.id.widget_btn_prev, prevPendingIntent)

                // Click Play/Pause
                val playPauseIntent = Intent(context, TootAppWidgetProvider::class.java).apply {
                    action = ACTION_WIDGET_PLAY_PAUSE
                }
                val playPausePendingIntent = PendingIntent.getBroadcast(
                    context,
                    2,
                    playPauseIntent,
                    flags
                )
                views.setOnClickPendingIntent(R.id.widget_btn_play_pause, playPausePendingIntent)

                // Click Next
                val nextIntent = Intent(context, TootAppWidgetProvider::class.java).apply {
                    action = ACTION_WIDGET_NEXT
                }
                val nextPendingIntent = PendingIntent.getBroadcast(
                    context,
                    3,
                    nextIntent,
                    flags
                )
                views.setOnClickPendingIntent(R.id.widget_btn_next, nextPendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                Log.w(TAG, "Error updating appWidget $appWidgetId: $e")
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(
                context,
                appWidgetManager,
                appWidgetId,
                lastIsPlaying,
                lastTrackTitle,
                lastSessionName
            )
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        try {
            when (intent.action) {
                ACTION_WIDGET_PLAY_PAUSE -> {
                    AudioServiceBridge.triggerPlayPause()
                }
                ACTION_WIDGET_NEXT -> {
                    AudioServiceBridge.triggerNext()
                }
                ACTION_WIDGET_PREV -> {
                    AudioServiceBridge.triggerPrevious()
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error in onReceive: $e")
        }
    }
}
