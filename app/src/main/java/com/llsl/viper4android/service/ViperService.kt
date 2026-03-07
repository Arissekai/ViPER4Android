package com.llsl.viper4android.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Binder
import android.os.IBinder
import android.util.SparseArray
import androidx.core.app.NotificationCompat
import androidx.core.util.size
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.llsl.viper4android.R
import com.llsl.viper4android.audio.AudioOutputDetector
import com.llsl.viper4android.audio.EffectDispatcher
import com.llsl.viper4android.audio.FileLogger
import com.llsl.viper4android.audio.ViperEffect
import com.llsl.viper4android.audio.ViperParams
import com.llsl.viper4android.data.repository.ViperRepository
import com.llsl.viper4android.ui.screens.main.MainUiState
import com.llsl.viper4android.ui.screens.main.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ViperService : LifecycleService() {

    @Inject
    lateinit var repository: ViperRepository

    companion object {
        private const val NOTIFICATION_ID = 1

        const val ACTION_START = "com.llsl.viper4android.service.START"
        const val ACTION_STOP = "com.llsl.viper4android.service.STOP"
        const val ACTION_SESSION_OPEN = "com.llsl.viper4android.service.SESSION_OPEN"
        const val ACTION_SESSION_CLOSE = "com.llsl.viper4android.service.SESSION_CLOSE"
        const val EXTRA_AUDIO_SESSION = "android.media.extra.AUDIO_SESSION"
        const val EXTRA_PACKAGE_NAME = "android.media.extra.PACKAGE_NAME"

        fun startService(context: Context) {
            val intent = Intent(context, ViperService::class.java).apply {
                action = ACTION_START
            }
            context.startForegroundService(intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, ViperService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    inner class LocalBinder : Binder() {
        val service: ViperService get() = this@ViperService
    }

    private val binder = LocalBinder()
    private val sessions = SparseArray<ViperEffect>()
    private var globalEffect: ViperEffect? = null
    private var useAidlTypeUuid: Boolean = true
    private var audioOutputDetector: AudioOutputDetector? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        FileLogger.i("Service", "Service created")
        lifecycleScope.launch {
            useAidlTypeUuid = repository.getBooleanPreference(MainViewModel.PREF_AIDL_MODE).first()
            initGlobalEffect()
            startAudioOutputMonitor()
        }
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    private fun initGlobalEffect() {
        val typeUuid =
            if (useAidlTypeUuid) ViperEffect.EFFECT_TYPE_UUID_AIDL else ViperEffect.EFFECT_TYPE_UUID
        val effect = ViperEffect(0, typeUuid)
        if (!effect.create()) {
            FileLogger.e("Service", "Failed to create global effect")
            return
        }
        globalEffect = effect
        FileLogger.i("Service", "Global effect created (aidlType=$useAidlTypeUuid)")
        lifecycleScope.launch {
            applyFullStateToEffect(effect)
            FileLogger.i("Service", "Global effect initialized with full state")
        }
    }

    private fun startAudioOutputMonitor() {
        val detector = AudioOutputDetector(this)
        audioOutputDetector = detector
        lifecycleScope.launch {
            var lastHeadphoneState = detector.isHeadphoneConnected.value
            detector.isHeadphoneConnected.collect { headphoneConnected ->
                if (headphoneConnected != lastHeadphoneState) {
                    lastHeadphoneState = headphoneConnected
                    FileLogger.i("Service", "Audio output changed: headphone=$headphoneConnected")
                    reapplyAllEffects()
                }
            }
        }
    }

    private fun reapplyAllEffects() {
        lifecycleScope.launch {
            globalEffect?.let { applyFullStateToEffect(it, skipShmWrite = true) }
            for (i in 0 until sessions.size) {
                applyFullStateToEffect(sessions.valueAt(i), skipShmWrite = true)
            }
        }
    }

    private suspend fun applyFullStateToEffect(effect: ViperEffect, skipShmWrite: Boolean = false) {
        val state = EffectDispatcher.loadFullStateFromPrefs(repository)
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val headphoneConnected = AudioOutputDetector.isHeadphoneConnected(audioManager)
        val fxType =
            if (headphoneConnected) ViperParams.FX_TYPE_HEADPHONE else ViperParams.FX_TYPE_SPEAKER
        val activeState = state.copy(fxType = fxType)
        val isMasterOn =
            if (fxType == ViperParams.FX_TYPE_SPEAKER) activeState.spkMasterEnabled else activeState.masterEnabled
        effect.enabled = isMasterOn
        if (useAidlTypeUuid && !skipShmWrite) {
            // TODO: AIDL Implementation.
        }
        EffectDispatcher.dispatchFullState(effect, activeState, isMasterOn)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            ACTION_START -> FileLogger.i("Service", "Service started")
            ACTION_STOP -> {
                releaseAllSessions()
                globalEffect?.let { it.enabled = false; it.release() }
                globalEffect = null
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }

            ACTION_SESSION_OPEN -> {
                val sessionId = intent.getIntExtra(EXTRA_AUDIO_SESSION, -1)
                val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: ""
                if (sessionId >= 0) {
                    openSession(sessionId, packageName)
                }
            }

            ACTION_SESSION_CLOSE -> {
                val sessionId = intent.getIntExtra(EXTRA_AUDIO_SESSION, -1)
                if (sessionId >= 0) {
                    closeSession(sessionId)
                }
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        audioOutputDetector?.stop()
        audioOutputDetector = null
        globalEffect?.let { it.enabled = false; it.release() }
        globalEffect = null
        releaseAllSessions()
        FileLogger.i("Service", "Service destroyed")
        super.onDestroy()
    }

    private fun openSession(sessionId: Int, packageName: String) {
        if (sessions.get(sessionId) != null) {
            FileLogger.w("Service", "Session $sessionId already open")
            return
        }

        val typeUuid =
            if (useAidlTypeUuid) ViperEffect.EFFECT_TYPE_UUID_AIDL else ViperEffect.EFFECT_TYPE_UUID
        val effect = ViperEffect(sessionId, typeUuid)
        if (!effect.create()) {
            FileLogger.e("Service", "Failed to create effect for session $sessionId ($packageName)")
            return
        }

        sessions.put(sessionId, effect)
        FileLogger.i("Service", "Opened session $sessionId for $packageName")

        lifecycleScope.launch {
            applyFullStateToEffect(effect)
            FileLogger.i("Service", "Applied full state to session $sessionId")
        }
    }

    private fun closeSession(sessionId: Int) {
        val effect = sessions.get(sessionId) ?: return
        effect.enabled = false
        effect.release()
        sessions.remove(sessionId)
        FileLogger.i("Service", "Closed session $sessionId")
    }

    private fun releaseAllSessions() {
        for (i in 0 until sessions.size) {
            val effect = sessions.valueAt(i)
            effect.enabled = false
            effect.release()
        }
        sessions.clear()
    }

    fun dispatchParam(param: Int, value: Int) {
        FileLogger.d("Service", "DSP param=$param value=$value")
        if (useAidlTypeUuid) {
            // TODO: AIDL Implementation.
        }
        globalEffect?.setParameter(param, value)
        for (i in 0 until sessions.size) {
            sessions.valueAt(i).setParameter(param, value)
        }
    }

    fun dispatchParam(param: Int, val1: Int, val2: Int) {
        FileLogger.d("Service", "DSP param=$param v1=$val1 v2=$val2")
        if (useAidlTypeUuid) {
            // TODO: AIDL Implementation.
        }
        globalEffect?.setParameter(param, val1, val2)
        for (i in 0 until sessions.size) {
            sessions.valueAt(i).setParameter(param, val1, val2)
        }
    }

    fun dispatchParam(param: Int, val1: Int, val2: Int, val3: Int) {
        FileLogger.d("Service", "DSP param=$param v1=$val1 v2=$val2 v3=$val3")
        if (useAidlTypeUuid) {
            // TODO: AIDL Implementation.
        }
        globalEffect?.setParameter(param, val1, val2, val3)
        for (i in 0 until sessions.size) {
            sessions.valueAt(i).setParameter(param, val1, val2, val3)
        }
    }

    fun dispatchParam(param: Int, value: ByteArray) {
        FileLogger.d("Service", "DSP param=$param bytes=${value.size}")
        if (useAidlTypeUuid) {
            // TODO: AIDL Implementation.
        }
        globalEffect?.setParameter(param, value)
        for (i in 0 until sessions.size) {
            sessions.valueAt(i).setParameter(param, value)
        }
    }

    fun dispatchEqBands(param: Int, bandsString: String) {
        FileLogger.d("Service", "DSP EQ param=$param bands=$bandsString")
        if (useAidlTypeUuid) {
            // TODO: AIDL Implementation.
        }
        globalEffect?.let { EffectDispatcher.dispatchEqBands(it, param, bandsString) }
        for (i in 0 until sessions.size) {
            EffectDispatcher.dispatchEqBands(sessions.valueAt(i), param, bandsString)
        }
    }

    fun dispatchFullState(
        state: MainUiState,
        masterEnabled: Boolean
    ) {
        if (useAidlTypeUuid) {
            // TODO: AIDL Implementation.
        }
        globalEffect?.let { effect ->
            effect.enabled = masterEnabled
            EffectDispatcher.dispatchFullState(effect, state, masterEnabled)
        }
        for (i in 0 until sessions.size) {
            val effect = sessions.valueAt(i)
            effect.enabled = masterEnabled
            EffectDispatcher.dispatchFullState(effect, state, masterEnabled)
        }
    }

    fun setEffectEnabled(enabled: Boolean) {
        globalEffect?.enabled = enabled
        for (i in 0 until sessions.size) {
            sessions.valueAt(i).enabled = enabled
        }
    }

    fun getGlobalEffect(): ViperEffect? = globalEffect

    fun recreateGlobalEffect(aidlType: Boolean) {
        globalEffect?.let { it.enabled = false; it.release() }
        globalEffect = null
        useAidlTypeUuid = aidlType
        initGlobalEffect()
    }

    val activeSessionCount: Int
        get() = sessions.size

    private fun createNotificationChannel() {
        val channelId = getString(R.string.notification_channel_id)
        val channelName = getString(R.string.notification_channel_name)
        val channelDescription = getString(R.string.notification_channel_description)
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = channelDescription
            setShowBadge(false)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val channelId = getString(R.string.notification_channel_id)
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = launchIntent?.let {
            PendingIntent.getActivity(
                this, 0, it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }
}
