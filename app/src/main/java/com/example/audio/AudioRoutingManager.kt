package com.example.audio

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AudioOutputType {
    SPEAKER,
    WIRED_HEADSET,
    BLUETOOTH_A2DP,
    BLUETOOTH_SCO,
    USB_AUDIO,
    OTHER
}

data class AudioOutputDevice(
    val id: String,
    val nameFa: String,
    val nameEn: String,
    val type: AudioOutputType,
    val audioDeviceInfo: AudioDeviceInfo? = null,
    val isDefault: Boolean = false,
    val isPhoneSco: Boolean = false
)

class AudioRoutingManager(private val context: Context) {
    companion object {
        private const val TAG = "AudioRoutingManager"
    }

    private val audioManager: AudioManager? = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _availableOutputs = MutableStateFlow<List<AudioOutputDevice>>(emptyList())
    val availableOutputs: StateFlow<List<AudioOutputDevice>> = _availableOutputs.asStateFlow()

    private val _selectedOutput = MutableStateFlow<AudioOutputDevice?>(null)
    val selectedOutput: StateFlow<AudioOutputDevice?> = _selectedOutput.asStateFlow()

    private val _isPhoneAudioScoMode = MutableStateFlow(false)
    val isPhoneAudioScoMode: StateFlow<Boolean> = _isPhoneAudioScoMode.asStateFlow()

    var onRoutingChanged: (() -> Unit)? = null

    private val audioDeviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
            Log.d(TAG, "Audio devices added: ${addedDevices?.size}")
            refreshDevices()
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
            Log.d(TAG, "Audio devices removed: ${removedDevices?.size}")
            refreshDevices()
        }
    }

    private val scoReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED) {
                val state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, AudioManager.SCO_AUDIO_STATE_DISCONNECTED)
                Log.d(TAG, "Bluetooth SCO state changed: $state")
                refreshDevices()
            }
        }
    }

    init {
        try {
            audioManager?.registerAudioDeviceCallback(audioDeviceCallback, mainHandler)
        } catch (e: Exception) {
            Log.w(TAG, "Error registering AudioDeviceCallback: $e")
        }

        try {
            val filter = IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)
            context.registerReceiver(scoReceiver, filter)
        } catch (e: Exception) {
            Log.w(TAG, "Error registering SCO receiver: $e")
        }

        refreshDevices()
    }

    fun refreshDevices() {
        try {
            val devices = mutableListOf<AudioOutputDevice>()
            val rawDevices = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                audioManager?.getDevices(AudioManager.GET_DEVICES_OUTPUTS)?.toList() ?: emptyList()
            } else {
                emptyList()
            }

            var hasSpeaker = false
            var hasWired = false
            var hasBtA2dp = false
            var hasBtSco = false
            var hasUsb = false

            for (dev in rawDevices) {
                when (dev.type) {
                    AudioDeviceInfo.TYPE_BUILTIN_SPEAKER,
                    AudioDeviceInfo.TYPE_BUILTIN_SPEAKER_SAFE -> {
                        hasSpeaker = true
                        devices.add(
                            AudioOutputDevice(
                                id = "builtin_speaker",
                                nameFa = "بلندگوی داخلی گوشی",
                                nameEn = "Phone Speaker",
                                type = AudioOutputType.SPEAKER,
                                audioDeviceInfo = dev,
                                isDefault = true
                            )
                        )
                    }
                    AudioDeviceInfo.TYPE_WIRED_HEADSET,
                    AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                    AudioDeviceInfo.TYPE_AUX_LINE -> {
                        hasWired = true
                        devices.add(
                            AudioOutputDevice(
                                id = "wired_headset_${dev.id}",
                                nameFa = "هدفون سیمی / کابل AUX",
                                nameEn = "Wired Headset / AUX",
                                type = AudioOutputType.WIRED_HEADSET,
                                audioDeviceInfo = dev
                            )
                        )
                    }
                    AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> {
                        hasBtA2dp = true
                        val productName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) dev.productName.toString() else "Bluetooth Audio"
                        val displayName = if (productName.isNotBlank() && productName != "Bluetooth Audio") productName else "بلوتوث استریو (A2DP)"
                        devices.add(
                            AudioOutputDevice(
                                id = "bt_a2dp_${dev.id}",
                                nameFa = displayName,
                                nameEn = if (productName.isNotBlank()) productName else "Bluetooth Audio (A2DP)",
                                type = AudioOutputType.BLUETOOTH_A2DP,
                                audioDeviceInfo = dev
                            )
                        )
                    }
                    AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
                    AudioDeviceInfo.TYPE_TELEPHONY -> {
                        hasBtSco = true
                        val productName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) dev.productName.toString() else "Phone Audio"
                        devices.add(
                            AudioOutputDevice(
                                id = "bt_sco_${dev.id}",
                                nameFa = if (productName.isNotBlank() && productName != "Phone Audio") "$productName (تماس/خودرو)" else "سیستم تلفنی و خودرو (SCO)",
                                nameEn = if (productName.isNotBlank()) "$productName (Phone/Car)" else "Phone/Car Audio (SCO)",
                                type = AudioOutputType.BLUETOOTH_SCO,
                                audioDeviceInfo = dev,
                                isPhoneSco = true
                            )
                        )
                    }
                    AudioDeviceInfo.TYPE_USB_DEVICE,
                    AudioDeviceInfo.TYPE_USB_HEADSET -> {
                        hasUsb = true
                        val productName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) dev.productName.toString() else "USB Audio"
                        devices.add(
                            AudioOutputDevice(
                                id = "usb_audio_${dev.id}",
                                nameFa = if (productName.isNotBlank()) productName else "خروجی صوتی USB",
                                nameEn = if (productName.isNotBlank()) productName else "USB Audio",
                                type = AudioOutputType.USB_AUDIO,
                                audioDeviceInfo = dev
                            )
                        )
                    }
                    else -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (dev.type == AudioDeviceInfo.TYPE_BLE_HEADSET ||
                                dev.type == AudioDeviceInfo.TYPE_BLE_SPEAKER ||
                                dev.type == AudioDeviceInfo.TYPE_BLE_BROADCAST
                            ) {
                                hasBtA2dp = true
                                val productName = dev.productName.toString()
                                devices.add(
                                    AudioOutputDevice(
                                        id = "ble_audio_${dev.id}",
                                        nameFa = if (productName.isNotBlank()) productName else "بلوتوث BLE",
                                        nameEn = if (productName.isNotBlank()) productName else "BLE Audio",
                                        type = AudioOutputType.BLUETOOTH_A2DP,
                                        audioDeviceInfo = dev
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // If no speaker was found explicitly, always provide the default internal speaker
            if (!hasSpeaker) {
                devices.add(
                    0,
                    AudioOutputDevice(
                        id = "builtin_speaker_fallback",
                        nameFa = "بلندگوی داخلی گوشی",
                        nameEn = "Phone Speaker",
                        type = AudioOutputType.SPEAKER,
                        audioDeviceInfo = null,
                        isDefault = true
                    )
                )
            }

            _availableOutputs.value = devices

            // Maintain selected output validity
            val currentSelected = _selectedOutput.value
            if (currentSelected != null && devices.none { it.id == currentSelected.id }) {
                _selectedOutput.value = null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error refreshing audio devices: $e")
        }
    }

    fun selectOutput(device: AudioOutputDevice?) {
        _selectedOutput.value = device
        try {
            if (device?.isPhoneSco == true) {
                setPhoneAudioScoMode(true)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error selecting output: $e")
        }
        onRoutingChanged?.invoke()
    }

    fun setPhoneAudioScoMode(enabled: Boolean) {
        if (_isPhoneAudioScoMode.value == enabled) return
        _isPhoneAudioScoMode.value = enabled
        try {
            if (enabled) {
                Log.d(TAG, "Activating Phone Audio SCO Mode for Car / Handsfree playback...")
                audioManager?.mode = AudioManager.MODE_IN_COMMUNICATION

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val scoDevice = audioManager?.availableCommunicationDevices?.firstOrNull {
                        it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
                        it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                        it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET
                    }
                    if (scoDevice != null) {
                        val result = audioManager.setCommunicationDevice(scoDevice)
                        Log.d(TAG, "setCommunicationDevice result: $result, device: ${scoDevice.productName}")
                    }
                } else {
                    @Suppress("DEPRECATION")
                    audioManager?.startBluetoothSco()
                    @Suppress("DEPRECATION")
                    audioManager?.isBluetoothScoOn = true
                }
            } else {
                Log.d(TAG, "Deactivating Phone Audio SCO Mode, returning to standard media playback...")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    audioManager?.clearCommunicationDevice()
                } else {
                    @Suppress("DEPRECATION")
                    audioManager?.stopBluetoothSco()
                    @Suppress("DEPRECATION")
                    audioManager?.isBluetoothScoOn = false
                }
                audioManager?.mode = AudioManager.MODE_NORMAL
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error configuring Phone Audio SCO: $e")
        }
        onRoutingChanged?.invoke()
    }

    fun buildAudioAttributes(): AudioAttributes {
        return if (_isPhoneAudioScoMode.value) {
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .build()
        } else {
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        }
    }

    fun release() {
        try {
            audioManager?.unregisterAudioDeviceCallback(audioDeviceCallback)
        } catch (e: Exception) {
            // ignore
        }
        try {
            context.unregisterReceiver(scoReceiver)
        } catch (e: Exception) {
            // ignore
        }
        if (_isPhoneAudioScoMode.value) {
            setPhoneAudioScoMode(false)
        }
    }
}
