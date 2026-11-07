# ====================================================================
# TO_OT Player - Production ProGuard & R8 Obfuscation Rules
# Target: Google Play & Cafe Bazaar Compliance Standards
# ====================================================================

# --------------------------------------------------------------------
# 1. General Optimization & Keep Attributes
# --------------------------------------------------------------------
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keepattributes SourceFile, LineNumberTable
-renamesourcefileattribute SourceFile
-repackageclasses 'com.tootplayer.audiohub.internal'
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# --------------------------------------------------------------------
# 2. Release Mode Logging Optimization (Strip Debug Logs)
# --------------------------------------------------------------------
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# --------------------------------------------------------------------
# 3. Android Core & Application Entry Points
# --------------------------------------------------------------------
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference

# --------------------------------------------------------------------
# 4. App Components: Activities, Services, Widgets, & Receivers
# --------------------------------------------------------------------
-keep class com.example.MainActivity { *; }
-keep class com.example.service.TootPlaybackService { *; }
-keep class com.example.widget.TootAppWidgetProvider { *; }
-keep class androidx.media.session.MediaButtonReceiver { *; }
-keep class androidx.core.content.FileProvider { *; }

# --------------------------------------------------------------------
# 5. Room Database & Persistence Layer
# --------------------------------------------------------------------
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep class com.example.data.db.** { *; }
-dontwarn androidx.room.paging.**
-dontwarn androidx.room.**

# --------------------------------------------------------------------
# 6. Moshi JSON Models & CodeGen
# --------------------------------------------------------------------
-keep @com.squareup.moshi.JsonClass class * { *; }
-keep class com.squareup.moshi.** { *; }
-keepclassmembers class * {
    @com.squareup.moshi.FromJson *;
    @com.squareup.moshi.ToJson *;
}
-dontwarn com.squareup.moshi.**

# --------------------------------------------------------------------
# 7. Kotlin Coroutines & Flow
# --------------------------------------------------------------------
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# --------------------------------------------------------------------
# 8. AndroidX Media & Audio Session Compat
# --------------------------------------------------------------------
-keep class android.support.v4.media.session.** { *; }
-keep class androidx.media.** { *; }
-keep class androidx.media.session.** { *; }
-keepclassmembers class * extends androidx.media.MediaSessionCompat { *; }

# --------------------------------------------------------------------
# 8.1 Capacitor Native Bridge Classes
# --------------------------------------------------------------------
-keep class com.getcapacitor.** { *; }
-keep interface com.getcapacitor.** { *; }
-dontwarn com.getcapacitor.**

# --------------------------------------------------------------------
# 9. Jetpack Compose & ViewModel State
# --------------------------------------------------------------------
-keep class androidx.compose.runtime.** { *; }
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class com.example.ui.TootPlayerViewModel { *; }
-keep class com.example.ui.TootPlayerUiState { *; }
-keep class com.example.audio.AudioLaneState { *; }
-keep class com.example.audio.AudioEngine { *; }
-keep class com.example.audio.MasterRecorder { *; }
-keep class com.example.data.model.** { *; }
-keep class com.example.util.AppInfo { *; }

# --------------------------------------------------------------------
# 10. Generic Serialization, Reflection & Safe Deserialization
# --------------------------------------------------------------------
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# --------------------------------------------------------------------
# 11. Firebase Crashlytics & Analytics
# --------------------------------------------------------------------
-keep class com.google.firebase.crashlytics.** { *; }
-keep interface com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**
-keep class com.example.util.CrashlyticsHelper { *; }

