package com.llsl.viper4android.ui.screens.main

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.llsl.viper4android.audio.AudioOutputDetector
import com.llsl.viper4android.audio.EffectDispatcher
import com.llsl.viper4android.audio.FileLogger
import com.llsl.viper4android.audio.ParamEntry
import com.llsl.viper4android.audio.ViperEffect
import com.llsl.viper4android.audio.ViperParams
import com.llsl.viper4android.data.model.EqPreset
import com.llsl.viper4android.data.model.Preset
import com.llsl.viper4android.data.repository.ViperRepository
import com.llsl.viper4android.service.ViperService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class DriverStatus(
    val installed: Boolean = false,
    val versionCode: Int = -1,
    val versionName: String = "",
    val architecture: String = "",
    val streaming: Boolean = false,
    val samplingRate: Int = 0
)

data class MainUiState(
    val masterEnabled: Boolean = false,
    val fxType: Int = ViperParams.FX_TYPE_HEADPHONE,

    val outputVolume: Int = 11,
    val channelPan: Int = 0,
    val limiter: Int = 5,

    val agcEnabled: Boolean = false,
    val agcStrength: Int = 0,
    val agcMaxGain: Int = 3,
    val agcOutputThreshold: Int = 3,

    val fetEnabled: Boolean = false,
    val fetThreshold: Int = 100,
    val fetRatio: Int = 100,
    val fetAutoKnee: Boolean = true,
    val fetKnee: Int = 0,
    val fetKneeMulti: Int = 0,
    val fetAutoGain: Boolean = true,
    val fetGain: Int = 0,
    val fetAutoAttack: Boolean = true,
    val fetAttack: Int = 20,
    val fetMaxAttack: Int = 80,
    val fetAutoRelease: Boolean = true,
    val fetRelease: Int = 50,
    val fetMaxRelease: Int = 100,
    val fetCrest: Int = 100,
    val fetAdapt: Int = 50,
    val fetNoClip: Boolean = true,

    val ddcEnabled: Boolean = false,
    val ddcDevice: String = "",

    val vseEnabled: Boolean = false,
    val vseStrength: Int = 10,
    val vseExciter: Int = 0,

    val eqEnabled: Boolean = false,
    val eqBandCount: Int = 10,
    val eqPresetId: Long? = null,
    val eqBands: String = "0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;",
    val eqBandsMap: Map<Int, String> = mapOf(10 to "0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;"),
    val eqPresets: List<EqPreset> = emptyList(),

    val convolverEnabled: Boolean = false,
    val convolverKernel: String = "",
    val convolverCrossChannel: Int = 0,

    val fieldSurroundEnabled: Boolean = false,
    val fieldSurroundWidening: Int = 0,
    val fieldSurroundMidImage: Int = 5,
    val fieldSurroundDepth: Int = 0,

    val diffSurroundEnabled: Boolean = false,
    val diffSurroundDelay: Int = 4,

    val vheEnabled: Boolean = false,
    val vheQuality: Int = 0,

    val reverbEnabled: Boolean = false,
    val reverbRoomSize: Int = 0,
    val reverbWidth: Int = 0,
    val reverbDampening: Int = 0,
    val reverbWet: Int = 0,
    val reverbDry: Int = 50,

    val dynamicSystemEnabled: Boolean = false,
    val dynamicSystemDevice: Int = 0,
    val dynamicSystemStrength: Int = 50,

    val tubeSimulatorEnabled: Boolean = false,

    val bassEnabled: Boolean = false,
    val bassMode: Int = 0,
    val bassFrequency: Int = 55,
    val bassGain: Int = 0,

    val clarityEnabled: Boolean = false,
    val clarityMode: Int = 0,
    val clarityGain: Int = 1,

    val cureEnabled: Boolean = false,
    val cureStrength: Int = 0,

    val analogxEnabled: Boolean = false,
    val analogxMode: Int = 0,

    val spkDdcEnabled: Boolean = false,
    val spkDdcDevice: String = "",

    val spkVseEnabled: Boolean = false,
    val spkVseStrength: Int = 10,
    val spkVseExciter: Int = 0,

    val spkFieldSurroundEnabled: Boolean = false,
    val spkFieldSurroundWidening: Int = 0,
    val spkFieldSurroundMidImage: Int = 5,
    val spkFieldSurroundDepth: Int = 0,

    val spkDiffSurroundEnabled: Boolean = false,
    val spkDiffSurroundDelay: Int = 4,

    val spkVheEnabled: Boolean = false,
    val spkVheQuality: Int = 0,

    val spkDynamicSystemEnabled: Boolean = false,
    val spkDynamicSystemDevice: Int = 0,
    val spkDynamicSystemStrength: Int = 50,

    val spkTubeSimulatorEnabled: Boolean = false,

    val spkBassEnabled: Boolean = false,
    val spkBassMode: Int = 0,
    val spkBassFrequency: Int = 55,
    val spkBassGain: Int = 0,

    val spkClarityEnabled: Boolean = false,
    val spkClarityMode: Int = 0,
    val spkClarityGain: Int = 1,

    val spkCureEnabled: Boolean = false,
    val spkCureStrength: Int = 0,

    val spkAnalogxEnabled: Boolean = false,
    val spkAnalogxMode: Int = 0,

    val spkChannelPan: Int = 0,

    val spkMasterEnabled: Boolean = false,
    val speakerOptEnabled: Boolean = false,

    val spkConvolverEnabled: Boolean = false,
    val spkConvolverKernel: String = "",
    val spkConvolverCrossChannel: Int = 0,

    val spkEqEnabled: Boolean = false,
    val spkEqBandCount: Int = 10,
    val spkEqPresetId: Long? = null,
    val spkEqBands: String = "0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;",
    val spkEqBandsMap: Map<Int, String> = mapOf(10 to "0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;"),
    val spkEqPresets: List<EqPreset> = emptyList(),

    val spkReverbEnabled: Boolean = false,
    val spkReverbRoomSize: Int = 0,
    val spkReverbWidth: Int = 0,
    val spkReverbDampening: Int = 0,
    val spkReverbWet: Int = 0,
    val spkReverbDry: Int = 50,

    val spkAgcEnabled: Boolean = false,
    val spkAgcStrength: Int = 0,
    val spkAgcMaxGain: Int = 3,
    val spkAgcOutputThreshold: Int = 3,

    val spkFetEnabled: Boolean = false,
    val spkFetThreshold: Int = 100,
    val spkFetRatio: Int = 100,
    val spkFetAutoKnee: Boolean = true,
    val spkFetKnee: Int = 0,
    val spkFetKneeMulti: Int = 0,
    val spkFetAutoGain: Boolean = true,
    val spkFetGain: Int = 0,
    val spkFetAutoAttack: Boolean = true,
    val spkFetAttack: Int = 20,
    val spkFetMaxAttack: Int = 80,
    val spkFetAutoRelease: Boolean = true,
    val spkFetRelease: Int = 50,
    val spkFetMaxRelease: Int = 100,
    val spkFetCrest: Int = 100,
    val spkFetAdapt: Int = 50,
    val spkFetNoClip: Boolean = true,

    val spkOutputVolume: Int = 11,
    val spkLimiter: Int = 5
)

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    private val repository: ViperRepository
) : AndroidViewModel(application) {

    companion object {
        val OUTPUT_VOLUME_VALUES get() = EffectDispatcher.OUTPUT_VOLUME_VALUES
        val OUTPUT_DB_VALUES get() = EffectDispatcher.OUTPUT_DB_VALUES
        val PLAYBACK_GAIN_RATIO_VALUES get() = EffectDispatcher.PLAYBACK_GAIN_RATIO_VALUES
        val MULTI_FACTOR_VALUES get() = EffectDispatcher.MULTI_FACTOR_VALUES
        val VSE_BARK_VALUES get() = EffectDispatcher.VSE_BARK_VALUES
        val DIFF_SURROUND_DELAY_VALUES get() = EffectDispatcher.DIFF_SURROUND_DELAY_VALUES
        val FIELD_SURROUND_WIDENING_VALUES get() = EffectDispatcher.FIELD_SURROUND_WIDENING_VALUES
        val DYNAMIC_SYSTEM_DEVICES get() = EffectDispatcher.DYNAMIC_SYSTEM_DEVICES
        val DYNAMIC_SYSTEM_DEVICE_NAMES get() = EffectDispatcher.DYNAMIC_SYSTEM_DEVICE_NAMES
        val BASS_GAIN_DB_LABELS get() = EffectDispatcher.BASS_GAIN_DB_LABELS
        val CLARITY_GAIN_DB_LABELS get() = EffectDispatcher.CLARITY_GAIN_DB_LABELS

        const val PREF_AUTO_START = "auto_start"
        const val PREF_AIDL_MODE = "aidl_mode"
    }

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    val presetList: StateFlow<List<Preset>> = repository.getAllPresets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _driverStatus = MutableStateFlow(DriverStatus())
    val driverStatus: StateFlow<DriverStatus> = _driverStatus.asStateFlow()

    private val _vdcFileList = MutableStateFlow<List<String>>(emptyList())
    val vdcFileList: StateFlow<List<String>> = _vdcFileList.asStateFlow()

    private val _kernelFileList = MutableStateFlow<List<String>>(emptyList())
    val kernelFileList: StateFlow<List<String>> = _kernelFileList.asStateFlow()

    private val _autoStartEnabled = MutableStateFlow(false)
    val autoStartEnabled: StateFlow<Boolean> = _autoStartEnabled.asStateFlow()

    private val _aidlModeEnabled = MutableStateFlow(false)
    val aidlModeEnabled: StateFlow<Boolean> = _aidlModeEnabled.asStateFlow()

    private val _debugModeEnabled = MutableStateFlow(false)
    val debugModeEnabled: StateFlow<Boolean> = _debugModeEnabled.asStateFlow()

    private var viperService: ViperService? = null
    private var serviceBound = false
    private val audioOutputDetector = AudioOutputDetector(application)
    private var activeDeviceType: Int = ViperParams.FX_TYPE_HEADPHONE
    private var eqPresetsJob: kotlinx.coroutines.Job? = null
    private var spkEqPresetsJob: kotlinx.coroutines.Job? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val localBinder = binder as? ViperService.LocalBinder ?: return
            viperService = localBinder.service
            serviceBound = true
            applyFullState()
            queryDriverStatus()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            viperService = null
            serviceBound = false
        }
    }

    init {
        loadSettingsPreferences()
        viewModelScope.launch {
            loadInitialState()
            loadEqPresetsForBandCount(_uiState.value.eqBandCount, isSpk = false)
            loadEqPresetsForBandCount(_uiState.value.spkEqBandCount, isSpk = true)
            bindToService()
            audioOutputDetector.isHeadphoneConnected.collect { headphoneConnected ->
                val detectedType =
                    if (headphoneConnected) ViperParams.FX_TYPE_HEADPHONE else ViperParams.FX_TYPE_SPEAKER
                if (activeDeviceType != detectedType) {
                    activeDeviceType = detectedType
                    _uiState.update { it.copy(fxType = detectedType) }
                    viewModelScope.launch {
                        repository.setIntPreference(
                            ViperRepository.PREF_FX_TYPE,
                            detectedType
                        )
                    }
                    applyFullState()
                }
            }
        }
    }

    private fun bindToService() {
        val intent = Intent(getApplication(), ViperService::class.java)
        getApplication<Application>().bindService(
            intent,
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    override fun onCleared() {
        super.onCleared()
        audioOutputDetector.stop()
        if (serviceBound) {
            getApplication<Application>().unbindService(serviceConnection)
            serviceBound = false
        }
        viperService = null
    }

    private suspend fun loadInitialState() {
        loadHeadphonePreferences()
        loadSpeakerPreferences()
    }

    private fun loadEqPresetsForBandCount(bandCount: Int, isSpk: Boolean) {
        if (isSpk) {
            spkEqPresetsJob?.cancel()
            spkEqPresetsJob = viewModelScope.launch {
                repository.getEqPresetsByBandCount(bandCount).collect { presets ->
                    _uiState.update { it.copy(spkEqPresets = presets) }
                }
            }
        } else {
            eqPresetsJob?.cancel()
            eqPresetsJob = viewModelScope.launch {
                repository.getEqPresetsByBandCount(bandCount).collect { presets ->
                    _uiState.update { it.copy(eqPresets = presets) }
                }
            }
        }
    }

    private suspend fun loadHeadphonePreferences() {
        val masterEnabled =
            repository.getBooleanPreference(ViperRepository.PREF_MASTER_ENABLE).first()
        val fxType =
            repository.getIntPreference(ViperRepository.PREF_FX_TYPE, ViperParams.FX_TYPE_HEADPHONE)
                .first()

        val outputVolume =
            repository.getIntPreference("${ViperParams.PARAM_HP_OUTPUT_VOLUME}", 11).first()
        val channelPan =
            repository.getIntPreference("${ViperParams.PARAM_HP_CHANNEL_PAN}", 0).first()
        val limiter = repository.getIntPreference("${ViperParams.PARAM_HP_LIMITER}", 5).first()

        val agcEnabled =
            repository.getBooleanPreference("${ViperParams.PARAM_HP_AGC_ENABLE}").first()
        val fetEnabled =
            repository.getBooleanPreference("${ViperParams.PARAM_HP_FET_COMPRESSOR_ENABLE}").first()
        val ddcEnabled =
            repository.getBooleanPreference("${ViperParams.PARAM_HP_DDC_ENABLE}").first()
        val ddcDevice = repository.getStringPreference("ddc_device", "").first()
        val vseEnabled =
            repository.getBooleanPreference("${ViperParams.PARAM_HP_SPECTRUM_EXTENSION_ENABLE}")
                .first()
        val eqEnabled = repository.getBooleanPreference("${ViperParams.PARAM_HP_EQ_ENABLE}").first()
        val convolverEnabled =
            repository.getBooleanPreference("${ViperParams.PARAM_HP_CONVOLVER_ENABLE}").first()
        val convolverKernel = repository.getStringPreference("convolver_kernel", "").first()
        val fieldSurroundEnabled =
            repository.getBooleanPreference("${ViperParams.PARAM_HP_FIELD_SURROUND_ENABLE}").first()
        val diffSurroundEnabled =
            repository.getBooleanPreference("${ViperParams.PARAM_HP_DIFF_SURROUND_ENABLE}").first()
        val vheEnabled =
            repository.getBooleanPreference("${ViperParams.PARAM_HP_HEADPHONE_SURROUND_ENABLE}")
                .first()
        val reverbEnabled =
            repository.getBooleanPreference("${ViperParams.PARAM_HP_REVERB_ENABLE}").first()
        val dynamicSystemEnabled =
            repository.getBooleanPreference("${ViperParams.PARAM_HP_DYNAMIC_SYSTEM_ENABLE}").first()
        val tubeSimulatorEnabled =
            repository.getBooleanPreference("${ViperParams.PARAM_HP_TUBE_SIMULATOR_ENABLE}").first()
        val bassEnabled =
            repository.getBooleanPreference("${ViperParams.PARAM_HP_BASS_ENABLE}").first()
        val clarityEnabled =
            repository.getBooleanPreference("${ViperParams.PARAM_HP_CLARITY_ENABLE}").first()
        val cureEnabled =
            repository.getBooleanPreference("${ViperParams.PARAM_HP_CURE_ENABLE}").first()
        val analogxEnabled =
            repository.getBooleanPreference("${ViperParams.PARAM_HP_ANALOGX_ENABLE}").first()

        val eqBandCount = repository.getIntPreference("eq_band_count", 10).first()
        val rawEqBands = repository.getStringPreference(
            "${ViperParams.PARAM_HP_EQ_BAND_LEVEL}",
            "0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;"
        ).first()
        val parsedBandCount = rawEqBands.split(";").count { it.isNotBlank() }
        val eqBands = if (parsedBandCount != eqBandCount) {
            List(eqBandCount) { 0f }.joinToString(";") { "%.1f".format(it) } + ";"
        } else {
            rawEqBands
        }
        val eqPresetId = repository.getIntPreference("eq_preset_id", -1).first()
            .let { if (it < 0) null else it.toLong() }
        val eqBandsMap = mutableMapOf<Int, String>()
        for (bc in listOf(10, 15, 25, 31)) {
            val defaultBands = List(bc) { 0f }.joinToString(";") { "%.1f".format(it) } + ";"
            eqBandsMap[bc] = repository.getStringPreference("eq_bands_$bc", defaultBands).first()
        }
        eqBandsMap[eqBandCount] = eqBands
        val vseStrength =
            repository.getIntPreference("${ViperParams.PARAM_HP_SPECTRUM_EXTENSION_BARK}", 10)
                .first()
        val vseExciter = repository.getIntPreference(
            "${ViperParams.PARAM_HP_SPECTRUM_EXTENSION_BARK_RECONSTRUCT}",
            0
        ).first()
        val fieldSurroundWidening =
            repository.getIntPreference("${ViperParams.PARAM_HP_FIELD_SURROUND_WIDENING}", 0)
                .first()
        val fieldSurroundMidImage =
            repository.getIntPreference("${ViperParams.PARAM_HP_FIELD_SURROUND_MID_IMAGE}", 5)
                .first()
        val fieldSurroundDepth =
            repository.getIntPreference("${ViperParams.PARAM_HP_FIELD_SURROUND_DEPTH}", 0).first()
        val diffSurroundDelay =
            repository.getIntPreference("${ViperParams.PARAM_HP_DIFF_SURROUND_DELAY}", 4).first()
        val vheQuality =
            repository.getIntPreference("${ViperParams.PARAM_HP_HEADPHONE_SURROUND_STRENGTH}", 0)
                .first()
        val dynamicSystemDevice = repository.getIntPreference("dynamic_system_device", 0).first()
        val dynamicSystemStrength =
            repository.getIntPreference("${ViperParams.PARAM_HP_DYNAMIC_SYSTEM_STRENGTH}", 50)
                .first()
        val bassMode = repository.getIntPreference("${ViperParams.PARAM_HP_BASS_MODE}", 0).first()
        val bassFrequency =
            repository.getIntPreference("${ViperParams.PARAM_HP_BASS_FREQUENCY}", 55).first()
        val bassGain = repository.getIntPreference("${ViperParams.PARAM_HP_BASS_GAIN}", 0).first()
        val clarityMode =
            repository.getIntPreference("${ViperParams.PARAM_HP_CLARITY_MODE}", 0).first()
        val clarityGain =
            repository.getIntPreference("${ViperParams.PARAM_HP_CLARITY_GAIN}", 1).first()
        val cureStrength =
            repository.getIntPreference("${ViperParams.PARAM_HP_CURE_STRENGTH}", 0).first()
        val analogxMode =
            repository.getIntPreference("${ViperParams.PARAM_HP_ANALOGX_MODE}", 0).first()

        val reverbRoomSize =
            repository.getIntPreference("${ViperParams.PARAM_HP_REVERB_ROOM_SIZE}", 0).first()
        val reverbWidth =
            repository.getIntPreference("${ViperParams.PARAM_HP_REVERB_ROOM_WIDTH}", 0).first()
        val reverbDampening =
            repository.getIntPreference("${ViperParams.PARAM_HP_REVERB_ROOM_DAMPENING}", 0).first()
        val reverbWet =
            repository.getIntPreference("${ViperParams.PARAM_HP_REVERB_ROOM_WET_SIGNAL}", 0).first()
        val reverbDry =
            repository.getIntPreference("${ViperParams.PARAM_HP_REVERB_ROOM_DRY_SIGNAL}", 50)
                .first()

        val agcStrength =
            repository.getIntPreference("${ViperParams.PARAM_HP_AGC_RATIO}", 0).first()
        val agcMaxGain =
            repository.getIntPreference("${ViperParams.PARAM_HP_AGC_MAX_SCALER}", 3).first()
        val agcOutputThreshold =
            repository.getIntPreference("${ViperParams.PARAM_HP_AGC_VOLUME}", 3).first()

        val fetThreshold =
            repository.getIntPreference("${ViperParams.PARAM_HP_FET_COMPRESSOR_THRESHOLD}", 100)
                .first()
        val fetRatio =
            repository.getIntPreference("${ViperParams.PARAM_HP_FET_COMPRESSOR_RATIO}", 100).first()
        val fetAutoKnee = repository.getBooleanPreference(
            "${ViperParams.PARAM_HP_FET_COMPRESSOR_AUTO_KNEE}",
            true
        ).first()
        val fetKnee =
            repository.getIntPreference("${ViperParams.PARAM_HP_FET_COMPRESSOR_KNEE}", 0).first()
        val fetKneeMulti =
            repository.getIntPreference("${ViperParams.PARAM_HP_FET_COMPRESSOR_KNEE_MULTI}", 0)
                .first()
        val fetAutoGain = repository.getBooleanPreference(
            "${ViperParams.PARAM_HP_FET_COMPRESSOR_AUTO_GAIN}",
            true
        ).first()
        val fetGain =
            repository.getIntPreference("${ViperParams.PARAM_HP_FET_COMPRESSOR_GAIN}", 0).first()
        val fetAutoAttack = repository.getBooleanPreference(
            "${ViperParams.PARAM_HP_FET_COMPRESSOR_AUTO_ATTACK}",
            true
        ).first()
        val fetAttack =
            repository.getIntPreference("${ViperParams.PARAM_HP_FET_COMPRESSOR_ATTACK}", 20).first()
        val fetMaxAttack =
            repository.getIntPreference("${ViperParams.PARAM_HP_FET_COMPRESSOR_MAX_ATTACK}", 80)
                .first()
        val fetAutoRelease = repository.getBooleanPreference(
            "${ViperParams.PARAM_HP_FET_COMPRESSOR_AUTO_RELEASE}",
            true
        ).first()
        val fetRelease =
            repository.getIntPreference("${ViperParams.PARAM_HP_FET_COMPRESSOR_RELEASE}", 50)
                .first()
        val fetMaxRelease =
            repository.getIntPreference("${ViperParams.PARAM_HP_FET_COMPRESSOR_MAX_RELEASE}", 100)
                .first()
        val fetCrest =
            repository.getIntPreference("${ViperParams.PARAM_HP_FET_COMPRESSOR_CREST}", 100).first()
        val fetAdapt =
            repository.getIntPreference("${ViperParams.PARAM_HP_FET_COMPRESSOR_ADAPT}", 50).first()
        val fetNoClip =
            repository.getBooleanPreference("${ViperParams.PARAM_HP_FET_COMPRESSOR_NO_CLIP}", true)
                .first()

        val convolverCrossChannel =
            repository.getIntPreference("${ViperParams.PARAM_HP_CONVOLVER_CROSS_CHANNEL}", 0)
                .first()

        _uiState.update {
            it.copy(
                masterEnabled = masterEnabled,
                fxType = fxType,
                outputVolume = outputVolume,
                channelPan = channelPan,
                limiter = limiter,
                agcEnabled = agcEnabled,
                agcStrength = agcStrength,
                agcMaxGain = agcMaxGain,
                agcOutputThreshold = agcOutputThreshold,
                fetEnabled = fetEnabled,
                fetThreshold = fetThreshold,
                fetRatio = fetRatio,
                fetAutoKnee = fetAutoKnee,
                fetKnee = fetKnee,
                fetKneeMulti = fetKneeMulti,
                fetAutoGain = fetAutoGain,
                fetGain = fetGain,
                fetAutoAttack = fetAutoAttack,
                fetAttack = fetAttack,
                fetMaxAttack = fetMaxAttack,
                fetAutoRelease = fetAutoRelease,
                fetRelease = fetRelease,
                fetMaxRelease = fetMaxRelease,
                fetCrest = fetCrest,
                fetAdapt = fetAdapt,
                fetNoClip = fetNoClip,
                ddcEnabled = ddcEnabled,
                ddcDevice = ddcDevice,
                vseEnabled = vseEnabled,
                vseStrength = vseStrength,
                vseExciter = vseExciter,
                eqEnabled = eqEnabled,
                eqBandCount = eqBandCount,
                eqPresetId = eqPresetId,
                eqBands = eqBands,
                eqBandsMap = eqBandsMap,
                convolverEnabled = convolverEnabled,
                convolverKernel = convolverKernel,
                convolverCrossChannel = convolverCrossChannel,
                fieldSurroundEnabled = fieldSurroundEnabled,
                fieldSurroundWidening = fieldSurroundWidening,
                fieldSurroundMidImage = fieldSurroundMidImage,
                fieldSurroundDepth = fieldSurroundDepth,
                diffSurroundEnabled = diffSurroundEnabled,
                diffSurroundDelay = diffSurroundDelay,
                vheEnabled = vheEnabled,
                vheQuality = vheQuality,
                reverbEnabled = reverbEnabled,
                reverbRoomSize = reverbRoomSize,
                reverbWidth = reverbWidth,
                reverbDampening = reverbDampening,
                reverbWet = reverbWet,
                reverbDry = reverbDry,
                dynamicSystemEnabled = dynamicSystemEnabled,
                dynamicSystemDevice = dynamicSystemDevice,
                dynamicSystemStrength = dynamicSystemStrength,
                tubeSimulatorEnabled = tubeSimulatorEnabled,
                bassEnabled = bassEnabled,
                bassMode = bassMode,
                bassFrequency = bassFrequency,
                bassGain = bassGain,
                clarityEnabled = clarityEnabled,
                clarityMode = clarityMode,
                clarityGain = clarityGain,
                cureEnabled = cureEnabled,
                cureStrength = cureStrength,
                analogxEnabled = analogxEnabled,
                analogxMode = analogxMode
            )
        }
    }

    private suspend fun loadSpeakerPreferences() {
        val spkMasterEnabled = repository.getBooleanPreference("spk_master_enable").first()
        val spkDdcEnabled =
            repository.getBooleanPreference("spk_${ViperParams.PARAM_SPK_DDC_ENABLE}").first()
        val spkDdcDevice = repository.getStringPreference("spk_ddc_device", "").first()
        val spkVseEnabled =
            repository.getBooleanPreference("spk_${ViperParams.PARAM_SPK_SPECTRUM_EXTENSION_ENABLE}")
                .first()
        val spkVseStrength =
            repository.getIntPreference("spk_${ViperParams.PARAM_SPK_SPECTRUM_EXTENSION_BARK}", 10)
                .first()
        val spkVseExciter = repository.getIntPreference(
            "spk_${ViperParams.PARAM_SPK_SPECTRUM_EXTENSION_BARK_RECONSTRUCT}",
            0
        ).first()
        val spkFieldSurroundEnabled =
            repository.getBooleanPreference("spk_${ViperParams.PARAM_SPK_FIELD_SURROUND_ENABLE}")
                .first()
        val spkFieldSurroundWidening =
            repository.getIntPreference("spk_${ViperParams.PARAM_SPK_FIELD_SURROUND_WIDENING}", 0)
                .first()
        val spkFieldSurroundMidImage =
            repository.getIntPreference("spk_${ViperParams.PARAM_SPK_FIELD_SURROUND_MID_IMAGE}", 5)
                .first()
        val spkFieldSurroundDepth =
            repository.getIntPreference("spk_${ViperParams.PARAM_SPK_FIELD_SURROUND_DEPTH}", 0)
                .first()
        val spkDiffSurroundEnabled =
            repository.getBooleanPreference("spk_${ViperParams.PARAM_SPK_DIFF_SURROUND_ENABLE}")
                .first()
        val spkDiffSurroundDelay =
            repository.getIntPreference("spk_${ViperParams.PARAM_SPK_DIFF_SURROUND_DELAY}", 4)
                .first()
        val spkVheEnabled =
            repository.getBooleanPreference("spk_${ViperParams.PARAM_SPK_HEADPHONE_SURROUND_ENABLE}")
                .first()
        val spkVheQuality = repository.getIntPreference(
            "spk_${ViperParams.PARAM_SPK_HEADPHONE_SURROUND_STRENGTH}",
            0
        ).first()
        val spkDynamicSystemEnabled =
            repository.getBooleanPreference("spk_${ViperParams.PARAM_SPK_DYNAMIC_SYSTEM_ENABLE}")
                .first()
        val spkDynamicSystemDevice =
            repository.getIntPreference("spk_dynamic_system_device", 0).first()
        val spkDynamicSystemStrength =
            repository.getIntPreference("spk_${ViperParams.PARAM_SPK_DYNAMIC_SYSTEM_STRENGTH}", 50)
                .first()
        val spkTubeSimulatorEnabled =
            repository.getBooleanPreference("spk_${ViperParams.PARAM_SPK_TUBE_SIMULATOR_ENABLE}")
                .first()
        val spkBassEnabled =
            repository.getBooleanPreference("spk_${ViperParams.PARAM_SPK_BASS_ENABLE}").first()
        val spkBassMode =
            repository.getIntPreference("spk_${ViperParams.PARAM_SPK_BASS_MODE}", 0).first()
        val spkBassFrequency =
            repository.getIntPreference("spk_${ViperParams.PARAM_SPK_BASS_FREQUENCY}", 55).first()
        val spkBassGain =
            repository.getIntPreference("spk_${ViperParams.PARAM_SPK_BASS_GAIN}", 0).first()
        val spkClarityEnabled =
            repository.getBooleanPreference("spk_${ViperParams.PARAM_SPK_CLARITY_ENABLE}").first()
        val spkClarityMode =
            repository.getIntPreference("spk_${ViperParams.PARAM_SPK_CLARITY_MODE}", 0).first()
        val spkClarityGain =
            repository.getIntPreference("spk_${ViperParams.PARAM_SPK_CLARITY_GAIN}", 1).first()
        val spkCureEnabled =
            repository.getBooleanPreference("spk_${ViperParams.PARAM_SPK_CURE_ENABLE}").first()
        val spkCureStrength =
            repository.getIntPreference("spk_${ViperParams.PARAM_SPK_CURE_STRENGTH}", 0).first()
        val spkAnalogxEnabled =
            repository.getBooleanPreference("spk_${ViperParams.PARAM_SPK_ANALOGX_ENABLE}").first()
        val spkAnalogxMode =
            repository.getIntPreference("spk_${ViperParams.PARAM_SPK_ANALOGX_MODE}", 0).first()
        val spkChannelPan =
            repository.getIntPreference("spk_${ViperParams.PARAM_SPK_CHANNEL_PAN}", 0).first()

        val speakerOptEnabled =
            repository.getBooleanPreference("speaker_optimization_enable").first()
        val spkConvolverEnabled =
            repository.getBooleanPreference("${ViperParams.PARAM_SPK_CONVOLVER_ENABLE}").first()
        val spkConvolverCrossChannel =
            repository.getIntPreference("${ViperParams.PARAM_SPK_CONVOLVER_CROSS_CHANNEL}", 0)
                .first()
        val spkConvolverKernel = repository.getStringPreference("spk_convolver_kernel", "").first()
        val spkEqBandCount = repository.getIntPreference("spk_eq_band_count", 10).first()
        val spkEqEnabled =
            repository.getBooleanPreference("${ViperParams.PARAM_SPK_EQ_ENABLE}").first()
        val spkEqPresetId = repository.getIntPreference("spk_eq_preset_id", -1).first()
            .let { if (it < 0) null else it.toLong() }
        val rawSpkEqBands = repository.getStringPreference(
            "${ViperParams.PARAM_SPK_EQ_BAND_LEVEL}",
            "0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;"
        ).first()
        val parsedSpkBandCount = rawSpkEqBands.split(";").count { it.isNotBlank() }
        val spkEqBands = if (parsedSpkBandCount != spkEqBandCount) {
            List(spkEqBandCount) { 0f }.joinToString(";") { "%.1f".format(it) } + ";"
        } else {
            rawSpkEqBands
        }
        val spkEqBandsMap = mutableMapOf<Int, String>()
        for (bc in listOf(10, 15, 25, 31)) {
            val defaultBands = List(bc) { 0f }.joinToString(";") { "%.1f".format(it) } + ";"
            spkEqBandsMap[bc] =
                repository.getStringPreference("spk_eq_bands_$bc", defaultBands).first()
        }
        spkEqBandsMap[spkEqBandCount] = spkEqBands
        val spkReverbEnabled =
            repository.getBooleanPreference("${ViperParams.PARAM_SPK_REVERB_ENABLE}").first()
        val spkReverbRoomSize =
            repository.getIntPreference("${ViperParams.PARAM_SPK_REVERB_ROOM_SIZE}", 0).first()
        val spkReverbWidth =
            repository.getIntPreference("${ViperParams.PARAM_SPK_REVERB_ROOM_WIDTH}", 0).first()
        val spkReverbDampening =
            repository.getIntPreference("${ViperParams.PARAM_SPK_REVERB_ROOM_DAMPENING}", 0).first()
        val spkReverbWet =
            repository.getIntPreference("${ViperParams.PARAM_SPK_REVERB_ROOM_WET_SIGNAL}", 0)
                .first()
        val spkReverbDry =
            repository.getIntPreference("${ViperParams.PARAM_SPK_REVERB_ROOM_DRY_SIGNAL}", 50)
                .first()
        val spkAgcEnabled =
            repository.getBooleanPreference("${ViperParams.PARAM_SPK_AGC_ENABLE}").first()
        val spkAgcStrength =
            repository.getIntPreference("${ViperParams.PARAM_SPK_AGC_RATIO}", 0).first()
        val spkAgcMaxGain =
            repository.getIntPreference("${ViperParams.PARAM_SPK_AGC_MAX_SCALER}", 3).first()
        val spkAgcOutputThreshold =
            repository.getIntPreference("${ViperParams.PARAM_SPK_AGC_VOLUME}", 3).first()
        val spkFetEnabled =
            repository.getBooleanPreference("${ViperParams.PARAM_SPK_FET_COMPRESSOR_ENABLE}")
                .first()
        val spkFetThreshold =
            repository.getIntPreference("${ViperParams.PARAM_SPK_FET_COMPRESSOR_THRESHOLD}", 100)
                .first()
        val spkFetRatio =
            repository.getIntPreference("${ViperParams.PARAM_SPK_FET_COMPRESSOR_RATIO}", 100)
                .first()
        val spkFetAutoKnee = repository.getBooleanPreference(
            "${ViperParams.PARAM_SPK_FET_COMPRESSOR_AUTO_KNEE}",
            true
        ).first()
        val spkFetKnee =
            repository.getIntPreference("${ViperParams.PARAM_SPK_FET_COMPRESSOR_KNEE}", 0).first()
        val spkFetKneeMulti =
            repository.getIntPreference("${ViperParams.PARAM_SPK_FET_COMPRESSOR_KNEE_MULTI}", 0)
                .first()
        val spkFetAutoGain = repository.getBooleanPreference(
            "${ViperParams.PARAM_SPK_FET_COMPRESSOR_AUTO_GAIN}",
            true
        ).first()
        val spkFetGain =
            repository.getIntPreference("${ViperParams.PARAM_SPK_FET_COMPRESSOR_GAIN}", 0).first()
        val spkFetAutoAttack = repository.getBooleanPreference(
            "${ViperParams.PARAM_SPK_FET_COMPRESSOR_AUTO_ATTACK}",
            true
        ).first()
        val spkFetAttack =
            repository.getIntPreference("${ViperParams.PARAM_SPK_FET_COMPRESSOR_ATTACK}", 20)
                .first()
        val spkFetMaxAttack =
            repository.getIntPreference("${ViperParams.PARAM_SPK_FET_COMPRESSOR_MAX_ATTACK}", 80)
                .first()
        val spkFetAutoRelease = repository.getBooleanPreference(
            "${ViperParams.PARAM_SPK_FET_COMPRESSOR_AUTO_RELEASE}",
            true
        ).first()
        val spkFetRelease =
            repository.getIntPreference("${ViperParams.PARAM_SPK_FET_COMPRESSOR_RELEASE}", 50)
                .first()
        val spkFetMaxRelease =
            repository.getIntPreference("${ViperParams.PARAM_SPK_FET_COMPRESSOR_MAX_RELEASE}", 100)
                .first()
        val spkFetCrest =
            repository.getIntPreference("${ViperParams.PARAM_SPK_FET_COMPRESSOR_CREST}", 100)
                .first()
        val spkFetAdapt =
            repository.getIntPreference("${ViperParams.PARAM_SPK_FET_COMPRESSOR_ADAPT}", 50).first()
        val spkFetNoClip =
            repository.getBooleanPreference("${ViperParams.PARAM_SPK_FET_COMPRESSOR_NO_CLIP}", true)
                .first()
        val spkOutputVolume =
            repository.getIntPreference("${ViperParams.PARAM_SPK_OUTPUT_VOLUME}", 11).first()
        val spkLimiter = repository.getIntPreference("${ViperParams.PARAM_SPK_LIMITER}", 5).first()

        _uiState.update {
            it.copy(
                spkMasterEnabled = spkMasterEnabled,
                spkDdcEnabled = spkDdcEnabled,
                spkDdcDevice = spkDdcDevice,
                spkVseEnabled = spkVseEnabled,
                spkVseStrength = spkVseStrength,
                spkVseExciter = spkVseExciter,
                spkFieldSurroundEnabled = spkFieldSurroundEnabled,
                spkFieldSurroundWidening = spkFieldSurroundWidening,
                spkFieldSurroundMidImage = spkFieldSurroundMidImage,
                spkFieldSurroundDepth = spkFieldSurroundDepth,
                spkDiffSurroundEnabled = spkDiffSurroundEnabled,
                spkDiffSurroundDelay = spkDiffSurroundDelay,
                spkVheEnabled = spkVheEnabled,
                spkVheQuality = spkVheQuality,
                spkDynamicSystemEnabled = spkDynamicSystemEnabled,
                spkDynamicSystemDevice = spkDynamicSystemDevice,
                spkDynamicSystemStrength = spkDynamicSystemStrength,
                spkTubeSimulatorEnabled = spkTubeSimulatorEnabled,
                spkBassEnabled = spkBassEnabled,
                spkBassMode = spkBassMode,
                spkBassFrequency = spkBassFrequency,
                spkBassGain = spkBassGain,
                spkClarityEnabled = spkClarityEnabled,
                spkClarityMode = spkClarityMode,
                spkClarityGain = spkClarityGain,
                spkCureEnabled = spkCureEnabled,
                spkCureStrength = spkCureStrength,
                spkAnalogxEnabled = spkAnalogxEnabled,
                spkAnalogxMode = spkAnalogxMode,
                spkChannelPan = spkChannelPan,
                speakerOptEnabled = speakerOptEnabled,
                spkConvolverEnabled = spkConvolverEnabled,
                spkConvolverKernel = spkConvolverKernel,
                spkConvolverCrossChannel = spkConvolverCrossChannel,
                spkEqBandCount = spkEqBandCount,
                spkEqEnabled = spkEqEnabled,
                spkEqPresetId = spkEqPresetId,
                spkEqBands = spkEqBands,
                spkEqBandsMap = spkEqBandsMap,
                spkReverbEnabled = spkReverbEnabled,
                spkReverbRoomSize = spkReverbRoomSize,
                spkReverbWidth = spkReverbWidth,
                spkReverbDampening = spkReverbDampening,
                spkReverbWet = spkReverbWet,
                spkReverbDry = spkReverbDry,
                spkAgcEnabled = spkAgcEnabled,
                spkAgcStrength = spkAgcStrength,
                spkAgcMaxGain = spkAgcMaxGain,
                spkAgcOutputThreshold = spkAgcOutputThreshold,
                spkFetEnabled = spkFetEnabled,
                spkFetThreshold = spkFetThreshold,
                spkFetRatio = spkFetRatio,
                spkFetAutoKnee = spkFetAutoKnee,
                spkFetKnee = spkFetKnee,
                spkFetKneeMulti = spkFetKneeMulti,
                spkFetAutoGain = spkFetAutoGain,
                spkFetGain = spkFetGain,
                spkFetAutoAttack = spkFetAutoAttack,
                spkFetAttack = spkFetAttack,
                spkFetMaxAttack = spkFetMaxAttack,
                spkFetAutoRelease = spkFetAutoRelease,
                spkFetRelease = spkFetRelease,
                spkFetMaxRelease = spkFetMaxRelease,
                spkFetCrest = spkFetCrest,
                spkFetAdapt = spkFetAdapt,
                spkFetNoClip = spkFetNoClip,
                spkOutputVolume = spkOutputVolume,
                spkLimiter = spkLimiter
            )
        }
    }

    private fun applyFullState() {
        val service = viperService ?: return
        val state = _uiState.value
        val isMasterOn =
            if (activeDeviceType == ViperParams.FX_TYPE_SPEAKER) state.spkMasterEnabled else state.masterEnabled
        val mode = if (activeDeviceType == ViperParams.FX_TYPE_HEADPHONE) "Headphone" else "Speaker"
        FileLogger.d(
            "ViewModel",
            "Dispatch: applyFullState mode=$mode master=${if (isMasterOn) "ON" else "OFF"}"
        )

        val ddcEnabled =
            if (activeDeviceType == ViperParams.FX_TYPE_SPEAKER) state.spkDdcEnabled else state.ddcEnabled
        val ddcDevice =
            if (activeDeviceType == ViperParams.FX_TYPE_SPEAKER) state.spkDdcDevice else state.ddcDevice
        FileLogger.i("ViewModel", "applyFullState: ddcEnabled=$ddcEnabled ddcDevice='$ddcDevice'")

        val convolverEnabled =
            if (activeDeviceType == ViperParams.FX_TYPE_SPEAKER) state.spkConvolverEnabled else state.convolverEnabled
        val kernel =
            if (activeDeviceType == ViperParams.FX_TYPE_SPEAKER) state.spkConvolverKernel else state.convolverKernel
        FileLogger.i(
            "ViewModel",
            "applyFullState: convolverEnabled=$convolverEnabled kernel='$kernel'"
        )

        service.dispatchFullState(
            state.copy(fxType = activeDeviceType),
            isMasterOn
        )
    }

    fun setMasterEnabled(enabled: Boolean) {
        FileLogger.i("ViewModel", "Master: ${if (enabled) "ON" else "OFF"} (headphone)")
        _uiState.update { it.copy(masterEnabled = enabled) }
        viewModelScope.launch {
            repository.setBooleanPreference(ViperRepository.PREF_MASTER_ENABLE, enabled)
        }
        if (activeDeviceType == ViperParams.FX_TYPE_HEADPHONE) {
            viperService?.setEffectEnabled(enabled)
            dispatchInt(ViperParams.PARAM_SET_UPDATE_STATUS, if (enabled) 1 else 0)
            if (enabled) applyFullState()
        }
    }

    fun setSpkMasterEnabled(enabled: Boolean) {
        FileLogger.i("ViewModel", "Master: ${if (enabled) "ON" else "OFF"} (speaker)")
        _uiState.update { it.copy(spkMasterEnabled = enabled) }
        viewModelScope.launch {
            repository.setBooleanPreference("spk_master_enable", enabled)
        }
        if (activeDeviceType == ViperParams.FX_TYPE_SPEAKER) {
            viperService?.setEffectEnabled(enabled)
            dispatchInt(ViperParams.PARAM_SET_UPDATE_STATUS, if (enabled) 1 else 0)
            if (enabled) applyFullState()
        }
    }

    fun setFxType(type: Int) {
        val mode = if (type == ViperParams.FX_TYPE_HEADPHONE) "Headphone" else "Speaker"
        FileLogger.i("ViewModel", "Dispatch: fxType=$mode")
        _uiState.update { it.copy(fxType = type) }
        viewModelScope.launch {
            repository.setIntPreference(ViperRepository.PREF_FX_TYPE, type)
        }
        applyFullState()
    }

    fun setOutputVolume(value: Int) {
        _uiState.update { it.copy(outputVolume = value) }
        viewModelScope.launch {
            repository.setIntPreference(
                "${ViperParams.PARAM_HP_OUTPUT_VOLUME}",
                value
            )
        }
        hpDispatchInt(
            ViperParams.PARAM_HP_OUTPUT_VOLUME,
            OUTPUT_VOLUME_VALUES.getOrElse(value) { 100 })
    }

    fun setChannelPan(value: Int) {
        _uiState.update { it.copy(channelPan = value) }
        saveAndDispatchInt(
            "${ViperParams.PARAM_HP_CHANNEL_PAN}",
            ViperParams.PARAM_HP_CHANNEL_PAN,
            value
        )
    }

    fun setLimiter(value: Int) {
        _uiState.update { it.copy(limiter = value) }
        viewModelScope.launch {
            repository.setIntPreference(
                "${ViperParams.PARAM_HP_LIMITER}",
                value
            )
        }
        hpDispatchInt(ViperParams.PARAM_HP_LIMITER, OUTPUT_DB_VALUES.getOrElse(value) { 100 })
    }

    fun setAgcEnabled(enabled: Boolean) {
        FileLogger.i("ViewModel", "AGC: ${if (enabled) "ON" else "OFF"}")
        _uiState.update { it.copy(agcEnabled = enabled) }
        saveAndDispatchBool(
            "${ViperParams.PARAM_HP_AGC_ENABLE}",
            ViperParams.PARAM_HP_AGC_ENABLE,
            enabled
        )
    }

    fun setAgcStrength(value: Int) {
        _uiState.update { it.copy(agcStrength = value) }
        viewModelScope.launch {
            repository.setIntPreference(
                "${ViperParams.PARAM_HP_AGC_RATIO}",
                value
            )
        }
        hpDispatchInt(
            ViperParams.PARAM_HP_AGC_RATIO,
            PLAYBACK_GAIN_RATIO_VALUES.getOrElse(value) { 50 })
    }

    fun setAgcMaxGain(value: Int) {
        _uiState.update { it.copy(agcMaxGain = value) }
        viewModelScope.launch {
            repository.setIntPreference(
                "${ViperParams.PARAM_HP_AGC_MAX_SCALER}",
                value
            )
        }
        hpDispatchInt(
            ViperParams.PARAM_HP_AGC_MAX_SCALER,
            MULTI_FACTOR_VALUES.getOrElse(value) { 100 })
    }

    fun setAgcOutputThreshold(value: Int) {
        _uiState.update { it.copy(agcOutputThreshold = value) }
        viewModelScope.launch {
            repository.setIntPreference(
                "${ViperParams.PARAM_HP_AGC_VOLUME}",
                value
            )
        }
        hpDispatchInt(ViperParams.PARAM_HP_AGC_VOLUME, OUTPUT_DB_VALUES.getOrElse(value) { 100 })
    }

    fun setFetEnabled(enabled: Boolean) {
        FileLogger.i("ViewModel", "FET Compressor: ${if (enabled) "ON" else "OFF"}")
        _uiState.update { it.copy(fetEnabled = enabled) }
        saveAndDispatchFetBool(
            "${ViperParams.PARAM_HP_FET_COMPRESSOR_ENABLE}",
            ViperParams.PARAM_HP_FET_COMPRESSOR_ENABLE,
            enabled
        )
    }

    fun setFetThreshold(v: Int) {
        _uiState.update { it.copy(fetThreshold = v) }; saveAndDispatchInt(
            "${ViperParams.PARAM_HP_FET_COMPRESSOR_THRESHOLD}",
            ViperParams.PARAM_HP_FET_COMPRESSOR_THRESHOLD,
            v
        )
    }

    fun setFetRatio(v: Int) {
        _uiState.update { it.copy(fetRatio = v) }; saveAndDispatchInt(
            "${ViperParams.PARAM_HP_FET_COMPRESSOR_RATIO}",
            ViperParams.PARAM_HP_FET_COMPRESSOR_RATIO,
            v
        )
    }

    fun setFetAutoKnee(v: Boolean) {
        _uiState.update { it.copy(fetAutoKnee = v) }; saveAndDispatchFetBool(
            "${ViperParams.PARAM_HP_FET_COMPRESSOR_AUTO_KNEE}",
            ViperParams.PARAM_HP_FET_COMPRESSOR_AUTO_KNEE,
            v
        )
    }

    fun setFetKnee(v: Int) {
        _uiState.update { it.copy(fetKnee = v) }; saveAndDispatchInt(
            "${ViperParams.PARAM_HP_FET_COMPRESSOR_KNEE}",
            ViperParams.PARAM_HP_FET_COMPRESSOR_KNEE,
            v
        )
    }

    fun setFetKneeMulti(v: Int) {
        _uiState.update { it.copy(fetKneeMulti = v) }; saveAndDispatchInt(
            "${ViperParams.PARAM_HP_FET_COMPRESSOR_KNEE_MULTI}",
            ViperParams.PARAM_HP_FET_COMPRESSOR_KNEE_MULTI,
            v
        )
    }

    fun setFetAutoGain(v: Boolean) {
        _uiState.update { it.copy(fetAutoGain = v) }; saveAndDispatchFetBool(
            "${ViperParams.PARAM_HP_FET_COMPRESSOR_AUTO_GAIN}",
            ViperParams.PARAM_HP_FET_COMPRESSOR_AUTO_GAIN,
            v
        )
    }

    fun setFetGain(v: Int) {
        _uiState.update { it.copy(fetGain = v) }; saveAndDispatchInt(
            "${ViperParams.PARAM_HP_FET_COMPRESSOR_GAIN}",
            ViperParams.PARAM_HP_FET_COMPRESSOR_GAIN,
            v
        )
    }

    fun setFetAutoAttack(v: Boolean) {
        _uiState.update { it.copy(fetAutoAttack = v) }; saveAndDispatchFetBool(
            "${ViperParams.PARAM_HP_FET_COMPRESSOR_AUTO_ATTACK}",
            ViperParams.PARAM_HP_FET_COMPRESSOR_AUTO_ATTACK,
            v
        )
    }

    fun setFetAttack(v: Int) {
        _uiState.update { it.copy(fetAttack = v) }; saveAndDispatchInt(
            "${ViperParams.PARAM_HP_FET_COMPRESSOR_ATTACK}",
            ViperParams.PARAM_HP_FET_COMPRESSOR_ATTACK,
            v
        )
    }

    fun setFetMaxAttack(v: Int) {
        _uiState.update { it.copy(fetMaxAttack = v) }; saveAndDispatchInt(
            "${ViperParams.PARAM_HP_FET_COMPRESSOR_MAX_ATTACK}",
            ViperParams.PARAM_HP_FET_COMPRESSOR_MAX_ATTACK,
            v
        )
    }

    fun setFetAutoRelease(v: Boolean) {
        _uiState.update { it.copy(fetAutoRelease = v) }; saveAndDispatchFetBool(
            "${ViperParams.PARAM_HP_FET_COMPRESSOR_AUTO_RELEASE}",
            ViperParams.PARAM_HP_FET_COMPRESSOR_AUTO_RELEASE,
            v
        )
    }

    fun setFetRelease(v: Int) {
        _uiState.update { it.copy(fetRelease = v) }; saveAndDispatchInt(
            "${ViperParams.PARAM_HP_FET_COMPRESSOR_RELEASE}",
            ViperParams.PARAM_HP_FET_COMPRESSOR_RELEASE,
            v
        )
    }

    fun setFetMaxRelease(v: Int) {
        _uiState.update { it.copy(fetMaxRelease = v) }; saveAndDispatchInt(
            "${ViperParams.PARAM_HP_FET_COMPRESSOR_MAX_RELEASE}",
            ViperParams.PARAM_HP_FET_COMPRESSOR_MAX_RELEASE,
            v
        )
    }

    fun setFetCrest(v: Int) {
        _uiState.update { it.copy(fetCrest = v) }; saveAndDispatchInt(
            "${ViperParams.PARAM_HP_FET_COMPRESSOR_CREST}",
            ViperParams.PARAM_HP_FET_COMPRESSOR_CREST,
            v
        )
    }

    fun setFetAdapt(v: Int) {
        _uiState.update { it.copy(fetAdapt = v) }; saveAndDispatchInt(
            "${ViperParams.PARAM_HP_FET_COMPRESSOR_ADAPT}",
            ViperParams.PARAM_HP_FET_COMPRESSOR_ADAPT,
            v
        )
    }

    fun setFetNoClip(v: Boolean) {
        _uiState.update { it.copy(fetNoClip = v) }; saveAndDispatchFetBool(
            "${ViperParams.PARAM_HP_FET_COMPRESSOR_NO_CLIP}",
            ViperParams.PARAM_HP_FET_COMPRESSOR_NO_CLIP,
            v
        )
    }

    fun setDdcEnabled(enabled: Boolean) {
        FileLogger.i("ViewModel", "DDC: ${if (enabled) "ON" else "OFF"}")
        _uiState.update { it.copy(ddcEnabled = enabled) }
        viewModelScope.launch {
            repository.setBooleanPreference(
                "${ViperParams.PARAM_HP_DDC_ENABLE}",
                enabled
            )
        }
        val device = _uiState.value.ddcDevice
        val effectiveEnabled = enabled && device.isNotEmpty()
        if (effectiveEnabled && activeDeviceType == ViperParams.FX_TYPE_HEADPHONE) {
            loadVdcByName(device, ViperParams.PARAM_HP_DDC_ENABLE)
        } else if (activeDeviceType == ViperParams.FX_TYPE_HEADPHONE) {
            dispatchInt(ViperParams.PARAM_HP_DDC_ENABLE, 0)
        }
    }

    fun setDdcDevice(device: String) {
        FileLogger.i("ViewModel", "DDC selected: $device")
        _uiState.update { it.copy(ddcDevice = device) }
        viewModelScope.launch { repository.setStringPreference("ddc_device", device) }
        if (activeDeviceType == ViperParams.FX_TYPE_HEADPHONE) {
            if (device.isEmpty()) {
                dispatchInt(ViperParams.PARAM_HP_DDC_ENABLE, 0)
            } else {
                val enableParam =
                    if (_uiState.value.ddcEnabled) ViperParams.PARAM_HP_DDC_ENABLE else null
                loadVdcByName(device, enableParam)
            }
        }
    }

    fun setVseEnabled(enabled: Boolean) {
        FileLogger.i("ViewModel", "VSE: ${if (enabled) "ON" else "OFF"}")
        _uiState.update { it.copy(vseEnabled = enabled) }
        saveAndDispatchBool(
            "${ViperParams.PARAM_HP_SPECTRUM_EXTENSION_ENABLE}",
            ViperParams.PARAM_HP_SPECTRUM_EXTENSION_ENABLE,
            enabled
        )
    }

    fun setVseStrength(value: Int) {
        _uiState.update { it.copy(vseStrength = value) }
        viewModelScope.launch {
            repository.setIntPreference(
                "${ViperParams.PARAM_HP_SPECTRUM_EXTENSION_BARK}",
                value
            )
        }
        hpDispatchInt(
            ViperParams.PARAM_HP_SPECTRUM_EXTENSION_BARK,
            VSE_BARK_VALUES.getOrElse(value) { 7600 })
    }

    fun setVseExciter(value: Int) {
        _uiState.update { it.copy(vseExciter = value) }
        viewModelScope.launch {
            repository.setIntPreference(
                "${ViperParams.PARAM_HP_SPECTRUM_EXTENSION_BARK_RECONSTRUCT}",
                value
            )
        }
        hpDispatchInt(
            ViperParams.PARAM_HP_SPECTRUM_EXTENSION_BARK_RECONSTRUCT,
            (value * 5.6).toInt()
        )
    }

    fun setEqEnabled(enabled: Boolean) {
        FileLogger.i("ViewModel", "EQ: ${if (enabled) "ON" else "OFF"}")
        _uiState.update { it.copy(eqEnabled = enabled) }
        saveAndDispatchBool(
            "${ViperParams.PARAM_HP_EQ_ENABLE}",
            ViperParams.PARAM_HP_EQ_ENABLE,
            enabled
        )
    }

    fun setEqPreset(presetId: Long) {
        val state = _uiState.value
        val preset = state.eqPresets.find { it.id == presetId } ?: return
        val bands = preset.bands
        val bandCount = state.eqBandCount
        _uiState.update { s ->
            val updatedMap = s.eqBandsMap.toMutableMap().apply { put(bandCount, bands) }
            s.copy(eqPresetId = presetId, eqBands = bands, eqBandsMap = updatedMap)
        }
        viewModelScope.launch {
            repository.setIntPreference("eq_preset_id", presetId.toInt())
            repository.setStringPreference("${ViperParams.PARAM_HP_EQ_BAND_LEVEL}", bands)
            repository.setStringPreference("eq_bands_$bandCount", bands)
        }
        hpDispatchEqBands(ViperParams.PARAM_HP_EQ_BAND_LEVEL, bands)
    }

    fun setEqBands(bands: String) {
        val bandCount = _uiState.value.eqBandCount
        _uiState.update { state ->
            val updatedMap = state.eqBandsMap.toMutableMap().apply { put(bandCount, bands) }
            state.copy(eqBands = bands, eqBandsMap = updatedMap)
        }
        viewModelScope.launch {
            repository.setStringPreference("${ViperParams.PARAM_HP_EQ_BAND_LEVEL}", bands)
            repository.setStringPreference("eq_bands_$bandCount", bands)
        }
        hpDispatchEqBands(ViperParams.PARAM_HP_EQ_BAND_LEVEL, bands)
    }

    fun setEqBandCount(count: Int) {
        val currentState = _uiState.value
        val oldCount = currentState.eqBandCount
        FileLogger.d("ViewModel", "EQ band count: $oldCount -> $count")
        val updatedMap = currentState.eqBandsMap.toMutableMap().apply {
            put(oldCount, currentState.eqBands)
        }
        val defaultBands = List(count) { 0f }.joinToString(";") { "%.1f".format(it) } + ";"
        val bands = updatedMap[count] ?: defaultBands
        _uiState.update {
            it.copy(
                eqBandCount = count,
                eqBands = bands,
                eqPresetId = null,
                eqBandsMap = updatedMap
            )
        }
        viewModelScope.launch {
            repository.setIntPreference("eq_band_count", count)
            repository.setStringPreference("${ViperParams.PARAM_HP_EQ_BAND_LEVEL}", bands)
            repository.setStringPreference("eq_bands_$oldCount", currentState.eqBands)
            repository.setStringPreference("eq_bands_$count", bands)
        }
        if (activeDeviceType == ViperParams.FX_TYPE_HEADPHONE) {
            dispatchInt(ViperParams.PARAM_HP_EQ_BAND_COUNT, count)
            dispatchEqBands(ViperParams.PARAM_HP_EQ_BAND_LEVEL, bands)
        }
        loadEqPresetsForBandCount(count, isSpk = false)
    }

    fun addEqPreset(name: String) {
        val state = _uiState.value
        val preset = EqPreset(name = name, bandCount = state.eqBandCount, bands = state.eqBands)
        viewModelScope.launch {
            val id = repository.saveEqPreset(preset)
            _uiState.update { it.copy(eqPresetId = id) }
            repository.setIntPreference("eq_preset_id", id.toInt())
        }
    }

    fun deleteEqPreset(presetId: Long) {
        viewModelScope.launch {
            repository.deleteEqPresetById(presetId)
            if (_uiState.value.eqPresetId == presetId) {
                _uiState.update { it.copy(eqPresetId = null) }
                repository.setIntPreference("eq_preset_id", -1)
            }
        }
    }

    fun resetEqBands() {
        val bandCount = _uiState.value.eqBandCount
        val flatBands = List(bandCount) { 0f }.joinToString(";") { "%.1f".format(it) } + ";"
        setEqBands(flatBands)
        _uiState.update { it.copy(eqPresetId = null) }
        viewModelScope.launch { repository.setIntPreference("eq_preset_id", -1) }
    }

    fun setConvolverEnabled(enabled: Boolean) {
        FileLogger.i("ViewModel", "Convolver: ${if (enabled) "ON" else "OFF"}")
        _uiState.update { it.copy(convolverEnabled = enabled) }
        viewModelScope.launch {
            repository.setBooleanPreference(
                "${ViperParams.PARAM_HP_CONVOLVER_ENABLE}",
                enabled
            )
        }
        val kernel = _uiState.value.convolverKernel
        val effectiveEnabled = enabled && kernel.isNotEmpty()
        if (effectiveEnabled && activeDeviceType == ViperParams.FX_TYPE_HEADPHONE) {
            loadKernelByName(kernel, ViperParams.PARAM_HP_CONVOLVER_ENABLE)
        } else if (activeDeviceType == ViperParams.FX_TYPE_HEADPHONE) {
            dispatchInt(ViperParams.PARAM_HP_CONVOLVER_ENABLE, 0)
        }
    }

    fun setConvolverKernel(kernel: String) {
        FileLogger.i("ViewModel", "Convolver kernel selected: $kernel")
        _uiState.update { it.copy(convolverKernel = kernel) }
        viewModelScope.launch { repository.setStringPreference("convolver_kernel", kernel) }
        if (activeDeviceType == ViperParams.FX_TYPE_HEADPHONE) {
            if (kernel.isEmpty()) {
                dispatchInt(ViperParams.PARAM_HP_CONVOLVER_ENABLE, 0)
            } else {
                val enableParam =
                    if (_uiState.value.convolverEnabled) ViperParams.PARAM_HP_CONVOLVER_ENABLE else null
                loadKernelByName(kernel, enableParam)
            }
        }
    }

    fun setConvolverCrossChannel(value: Int) {
        _uiState.update { it.copy(convolverCrossChannel = value) }
        saveAndDispatchInt(
            "${ViperParams.PARAM_HP_CONVOLVER_CROSS_CHANNEL}",
            ViperParams.PARAM_HP_CONVOLVER_CROSS_CHANNEL,
            value
        )
    }

    fun setFieldSurroundEnabled(enabled: Boolean) {
        FileLogger.i("ViewModel", "Field Surround: ${if (enabled) "ON" else "OFF"}")
        _uiState.update { it.copy(fieldSurroundEnabled = enabled) }
        saveAndDispatchBool(
            "${ViperParams.PARAM_HP_FIELD_SURROUND_ENABLE}",
            ViperParams.PARAM_HP_FIELD_SURROUND_ENABLE,
            enabled
        )
    }

    fun setFieldSurroundWidening(value: Int) {
        _uiState.update { it.copy(fieldSurroundWidening = value) }
        viewModelScope.launch {
            repository.setIntPreference(
                "${ViperParams.PARAM_HP_FIELD_SURROUND_WIDENING}",
                value
            )
        }
        hpDispatchInt(
            ViperParams.PARAM_HP_FIELD_SURROUND_WIDENING,
            FIELD_SURROUND_WIDENING_VALUES.getOrElse(value) { 0 })
    }

    fun setFieldSurroundMidImage(value: Int) {
        _uiState.update { it.copy(fieldSurroundMidImage = value) }
        viewModelScope.launch {
            repository.setIntPreference(
                "${ViperParams.PARAM_HP_FIELD_SURROUND_MID_IMAGE}",
                value
            )
        }
        hpDispatchInt(ViperParams.PARAM_HP_FIELD_SURROUND_MID_IMAGE, value * 10 + 100)
    }

    fun setFieldSurroundDepth(value: Int) {
        _uiState.update { it.copy(fieldSurroundDepth = value) }
        viewModelScope.launch {
            repository.setIntPreference(
                "${ViperParams.PARAM_HP_FIELD_SURROUND_DEPTH}",
                value
            )
        }
        hpDispatchInt(ViperParams.PARAM_HP_FIELD_SURROUND_DEPTH, value * 75 + 200)
    }

    fun setDiffSurroundEnabled(enabled: Boolean) {
        FileLogger.i("ViewModel", "Diff Surround: ${if (enabled) "ON" else "OFF"}")
        _uiState.update { it.copy(diffSurroundEnabled = enabled) }
        saveAndDispatchBool(
            "${ViperParams.PARAM_HP_DIFF_SURROUND_ENABLE}",
            ViperParams.PARAM_HP_DIFF_SURROUND_ENABLE,
            enabled
        )
    }

    fun setDiffSurroundDelay(value: Int) {
        _uiState.update { it.copy(diffSurroundDelay = value) }
        viewModelScope.launch {
            repository.setIntPreference(
                "${ViperParams.PARAM_HP_DIFF_SURROUND_DELAY}",
                value
            )
        }
        hpDispatchInt(
            ViperParams.PARAM_HP_DIFF_SURROUND_DELAY,
            DIFF_SURROUND_DELAY_VALUES.getOrElse(value) { 500 })
    }

    fun setVheEnabled(enabled: Boolean) {
        FileLogger.i("ViewModel", "VHE: ${if (enabled) "ON" else "OFF"}")
        _uiState.update { it.copy(vheEnabled = enabled) }
        saveAndDispatchBool(
            "${ViperParams.PARAM_HP_HEADPHONE_SURROUND_ENABLE}",
            ViperParams.PARAM_HP_HEADPHONE_SURROUND_ENABLE,
            enabled
        )
    }

    fun setVheQuality(value: Int) {
        _uiState.update { it.copy(vheQuality = value) }
        saveAndDispatchInt(
            "${ViperParams.PARAM_HP_HEADPHONE_SURROUND_STRENGTH}",
            ViperParams.PARAM_HP_HEADPHONE_SURROUND_STRENGTH,
            value
        )
    }

    fun setReverbEnabled(enabled: Boolean) {
        FileLogger.i("ViewModel", "Reverb: ${if (enabled) "ON" else "OFF"}")
        _uiState.update { it.copy(reverbEnabled = enabled) }
        saveAndDispatchBool(
            "${ViperParams.PARAM_HP_REVERB_ENABLE}",
            ViperParams.PARAM_HP_REVERB_ENABLE,
            enabled
        )
    }

    fun setReverbRoomSize(v: Int) {
        _uiState.update { it.copy(reverbRoomSize = v) }; viewModelScope.launch {
            repository.setIntPreference(
                "${ViperParams.PARAM_HP_REVERB_ROOM_SIZE}",
                v
            )
        }; hpDispatchInt(ViperParams.PARAM_HP_REVERB_ROOM_SIZE, v * 10)
    }

    fun setReverbWidth(v: Int) {
        _uiState.update { it.copy(reverbWidth = v) }; viewModelScope.launch {
            repository.setIntPreference(
                "${ViperParams.PARAM_HP_REVERB_ROOM_WIDTH}",
                v
            )
        }; hpDispatchInt(ViperParams.PARAM_HP_REVERB_ROOM_WIDTH, v * 10)
    }

    fun setReverbDampening(v: Int) {
        _uiState.update { it.copy(reverbDampening = v) }; viewModelScope.launch {
            repository.setIntPreference(
                "${ViperParams.PARAM_HP_REVERB_ROOM_DAMPENING}",
                v
            )
        }; hpDispatchInt(ViperParams.PARAM_HP_REVERB_ROOM_DAMPENING, v)
    }

    fun setReverbWet(v: Int) {
        _uiState.update { it.copy(reverbWet = v) }; viewModelScope.launch {
            repository.setIntPreference(
                "${ViperParams.PARAM_HP_REVERB_ROOM_WET_SIGNAL}",
                v
            )
        }; hpDispatchInt(ViperParams.PARAM_HP_REVERB_ROOM_WET_SIGNAL, v)
    }

    fun setReverbDry(v: Int) {
        _uiState.update { it.copy(reverbDry = v) }; saveAndDispatchInt(
            "${ViperParams.PARAM_HP_REVERB_ROOM_DRY_SIGNAL}",
            ViperParams.PARAM_HP_REVERB_ROOM_DRY_SIGNAL,
            v
        )
    }

    fun setDynamicSystemEnabled(enabled: Boolean) {
        FileLogger.i("ViewModel", "Dynamic System: ${if (enabled) "ON" else "OFF"}")
        _uiState.update { it.copy(dynamicSystemEnabled = enabled) }
        saveAndDispatchBool(
            "${ViperParams.PARAM_HP_DYNAMIC_SYSTEM_ENABLE}",
            ViperParams.PARAM_HP_DYNAMIC_SYSTEM_ENABLE,
            enabled
        )
    }

    fun setDynamicSystemDevice(index: Int) {
        _uiState.update { it.copy(dynamicSystemDevice = index) }
        viewModelScope.launch { repository.setIntPreference("dynamic_system_device", index) }
        if (activeDeviceType == ViperParams.FX_TYPE_HEADPHONE) {
            val coeffs = DYNAMIC_SYSTEM_DEVICES.getOrElse(index) { "100;5600;40;80;50;50" }
            val parts = coeffs.split(";").map { it.toIntOrNull() ?: 0 }
            if (parts.size >= 6) {
                viperService?.dispatchParamsBatch(
                    listOf(
                        ParamEntry(
                            ViperParams.PARAM_HP_DYNAMIC_SYSTEM_X_COEFFICIENTS,
                            intArrayOf(parts[0], parts[1])
                        ),
                        ParamEntry(
                            ViperParams.PARAM_HP_DYNAMIC_SYSTEM_Y_COEFFICIENTS,
                            intArrayOf(parts[2], parts[3])
                        ),
                        ParamEntry(
                            ViperParams.PARAM_HP_DYNAMIC_SYSTEM_SIDE_GAIN,
                            intArrayOf(parts[4], parts[5])
                        )
                    )
                )
            }
        }
    }

    fun setDynamicSystemStrength(value: Int) {
        _uiState.update { it.copy(dynamicSystemStrength = value) }
        viewModelScope.launch {
            repository.setIntPreference(
                "${ViperParams.PARAM_HP_DYNAMIC_SYSTEM_STRENGTH}",
                value
            )
        }
        hpDispatchInt(ViperParams.PARAM_HP_DYNAMIC_SYSTEM_STRENGTH, value * 20 + 100)
    }

    fun setTubeSimulatorEnabled(enabled: Boolean) {
        FileLogger.i("ViewModel", "Tube Simulator: ${if (enabled) "ON" else "OFF"}")
        _uiState.update { it.copy(tubeSimulatorEnabled = enabled) }
        saveAndDispatchBool(
            "${ViperParams.PARAM_HP_TUBE_SIMULATOR_ENABLE}",
            ViperParams.PARAM_HP_TUBE_SIMULATOR_ENABLE,
            enabled
        )
    }

    fun setBassEnabled(enabled: Boolean) {
        FileLogger.i("ViewModel", "Bass: ${if (enabled) "ON" else "OFF"}")
        _uiState.update { it.copy(bassEnabled = enabled) }
        saveAndDispatchBool(
            "${ViperParams.PARAM_HP_BASS_ENABLE}",
            ViperParams.PARAM_HP_BASS_ENABLE,
            enabled
        )
    }

    fun setBassMode(mode: Int) {
        _uiState.update { it.copy(bassMode = mode) }; saveAndDispatchInt(
            "${ViperParams.PARAM_HP_BASS_MODE}",
            ViperParams.PARAM_HP_BASS_MODE,
            mode
        )
    }

    fun setBassFrequency(v: Int) {
        _uiState.update { it.copy(bassFrequency = v) }; viewModelScope.launch {
            repository.setIntPreference(
                "${ViperParams.PARAM_HP_BASS_FREQUENCY}",
                v
            )
        }; hpDispatchInt(ViperParams.PARAM_HP_BASS_FREQUENCY, v + 15)
    }

    fun setBassGain(v: Int) {
        _uiState.update { it.copy(bassGain = v) }; viewModelScope.launch {
            repository.setIntPreference(
                "${ViperParams.PARAM_HP_BASS_GAIN}",
                v
            )
        }; hpDispatchInt(ViperParams.PARAM_HP_BASS_GAIN, v * 50 + 50)
    }

    fun setClarityEnabled(enabled: Boolean) {
        FileLogger.i("ViewModel", "Clarity: ${if (enabled) "ON" else "OFF"}")
        _uiState.update { it.copy(clarityEnabled = enabled) }
        saveAndDispatchBool(
            "${ViperParams.PARAM_HP_CLARITY_ENABLE}",
            ViperParams.PARAM_HP_CLARITY_ENABLE,
            enabled
        )
    }

    fun setClarityMode(mode: Int) {
        _uiState.update { it.copy(clarityMode = mode) }; saveAndDispatchInt(
            "${ViperParams.PARAM_HP_CLARITY_MODE}",
            ViperParams.PARAM_HP_CLARITY_MODE,
            mode
        )
    }

    fun setClarityGain(v: Int) {
        _uiState.update { it.copy(clarityGain = v) }; viewModelScope.launch {
            repository.setIntPreference(
                "${ViperParams.PARAM_HP_CLARITY_GAIN}",
                v
            )
        }; hpDispatchInt(ViperParams.PARAM_HP_CLARITY_GAIN, v * 50)
    }

    fun setCureEnabled(enabled: Boolean) {
        FileLogger.i("ViewModel", "Cure: ${if (enabled) "ON" else "OFF"}")
        _uiState.update { it.copy(cureEnabled = enabled) }
        saveAndDispatchBool(
            "${ViperParams.PARAM_HP_CURE_ENABLE}",
            ViperParams.PARAM_HP_CURE_ENABLE,
            enabled
        )
    }

    fun setCureStrength(v: Int) {
        _uiState.update { it.copy(cureStrength = v) }; saveAndDispatchInt(
            "${ViperParams.PARAM_HP_CURE_STRENGTH}",
            ViperParams.PARAM_HP_CURE_STRENGTH,
            v
        )
    }

    fun setAnalogxEnabled(enabled: Boolean) {
        FileLogger.i("ViewModel", "AnalogX: ${if (enabled) "ON" else "OFF"}")
        _uiState.update { it.copy(analogxEnabled = enabled) }
        saveAndDispatchBool(
            "${ViperParams.PARAM_HP_ANALOGX_ENABLE}",
            ViperParams.PARAM_HP_ANALOGX_ENABLE,
            enabled
        )
    }

    fun setAnalogxMode(mode: Int) {
        _uiState.update { it.copy(analogxMode = mode) }; saveAndDispatchInt(
            "${ViperParams.PARAM_HP_ANALOGX_MODE}",
            ViperParams.PARAM_HP_ANALOGX_MODE,
            mode
        )
    }

    fun setSpeakerOptEnabled(enabled: Boolean) {
        FileLogger.i("ViewModel", "Speaker Optimization: ${if (enabled) "ON" else "OFF"}")
        _uiState.update { it.copy(speakerOptEnabled = enabled) }
        viewModelScope.launch {
            repository.setBooleanPreference(
                "speaker_optimization_enable",
                enabled
            )
        }
        spkDispatchInt(ViperParams.PARAM_SPK_SPEAKER_CORRECTION_ENABLE, if (enabled) 1 else 0)
    }

    fun setSpkConvolverEnabled(enabled: Boolean) {
        FileLogger.i("ViewModel", "[Spk] Convolver: ${if (enabled) "ON" else "OFF"}")
        _uiState.update { it.copy(spkConvolverEnabled = enabled) }
        viewModelScope.launch {
            repository.setBooleanPreference(
                "${ViperParams.PARAM_SPK_CONVOLVER_ENABLE}",
                enabled
            )
        }
        val kernel = _uiState.value.spkConvolverKernel
        val effectiveEnabled = enabled && kernel.isNotEmpty()
        if (effectiveEnabled && activeDeviceType == ViperParams.FX_TYPE_SPEAKER) {
            loadKernelByName(kernel, ViperParams.PARAM_SPK_CONVOLVER_ENABLE)
        } else if (activeDeviceType == ViperParams.FX_TYPE_SPEAKER) {
            dispatchInt(ViperParams.PARAM_SPK_CONVOLVER_ENABLE, 0)
        }
    }

    fun setSpkConvolverKernel(kernel: String) {
        FileLogger.i("ViewModel", "[Spk] Convolver kernel selected: $kernel")
        _uiState.update { it.copy(spkConvolverKernel = kernel) }
        viewModelScope.launch { repository.setStringPreference("spk_convolver_kernel", kernel) }
        if (activeDeviceType == ViperParams.FX_TYPE_SPEAKER) {
            if (kernel.isEmpty()) {
                dispatchInt(ViperParams.PARAM_SPK_CONVOLVER_ENABLE, 0)
            } else {
                val enableParam =
                    if (_uiState.value.spkConvolverEnabled) ViperParams.PARAM_SPK_CONVOLVER_ENABLE else null
                loadKernelByName(kernel, enableParam)
            }
        }
    }

    fun setSpkConvolverCrossChannel(value: Int) {
        _uiState.update { it.copy(spkConvolverCrossChannel = value) }
        spkSaveAndDispatchInt(
            "${ViperParams.PARAM_SPK_CONVOLVER_CROSS_CHANNEL}",
            ViperParams.PARAM_SPK_CONVOLVER_CROSS_CHANNEL,
            value
        )
    }

    fun setSpkEqEnabled(enabled: Boolean) {
        FileLogger.i("ViewModel", "[Spk] EQ: ${if (enabled) "ON" else "OFF"}")
        _uiState.update { it.copy(spkEqEnabled = enabled) }
        spkSaveAndDispatchBool(
            "${ViperParams.PARAM_SPK_EQ_ENABLE}",
            ViperParams.PARAM_SPK_EQ_ENABLE,
            enabled
        )
    }

    fun setSpkEqPreset(presetId: Long) {
        val state = _uiState.value
        val preset = state.spkEqPresets.find { it.id == presetId } ?: return
        val bands = preset.bands
        val bandCount = state.spkEqBandCount
        _uiState.update { s ->
            val updatedMap = s.spkEqBandsMap.toMutableMap().apply { put(bandCount, bands) }
            s.copy(spkEqPresetId = presetId, spkEqBands = bands, spkEqBandsMap = updatedMap)
        }
        viewModelScope.launch {
            repository.setIntPreference("spk_eq_preset_id", presetId.toInt())
            repository.setStringPreference("${ViperParams.PARAM_SPK_EQ_BAND_LEVEL}", bands)
            repository.setStringPreference("spk_eq_bands_$bandCount", bands)
        }
        spkDispatchEqBands(ViperParams.PARAM_SPK_EQ_BAND_LEVEL, bands)
    }

    fun setSpkEqBands(bands: String) {
        val bandCount = _uiState.value.spkEqBandCount
        _uiState.update { state ->
            val updatedMap = state.spkEqBandsMap.toMutableMap().apply { put(bandCount, bands) }
            state.copy(spkEqBands = bands, spkEqBandsMap = updatedMap)
        }
        viewModelScope.launch {
            repository.setStringPreference("${ViperParams.PARAM_SPK_EQ_BAND_LEVEL}", bands)
            repository.setStringPreference("spk_eq_bands_$bandCount", bands)
        }
        spkDispatchEqBands(ViperParams.PARAM_SPK_EQ_BAND_LEVEL, bands)
    }

    fun setSpkEqBandCount(count: Int) {
        val currentState = _uiState.value
        val oldCount = currentState.spkEqBandCount
        FileLogger.d("ViewModel", "[Spk] EQ band count: $oldCount -> $count")
        val updatedMap = currentState.spkEqBandsMap.toMutableMap().apply {
            put(oldCount, currentState.spkEqBands)
        }
        val defaultBands = List(count) { 0f }.joinToString(";") { "%.1f".format(it) } + ";"
        val bands = updatedMap[count] ?: defaultBands
        _uiState.update {
            it.copy(
                spkEqBandCount = count,
                spkEqBands = bands,
                spkEqPresetId = null,
                spkEqBandsMap = updatedMap
            )
        }
        viewModelScope.launch {
            repository.setIntPreference("spk_eq_band_count", count)
            repository.setStringPreference("${ViperParams.PARAM_SPK_EQ_BAND_LEVEL}", bands)
            repository.setStringPreference("spk_eq_bands_$oldCount", currentState.spkEqBands)
            repository.setStringPreference("spk_eq_bands_$count", bands)
        }
        if (activeDeviceType == ViperParams.FX_TYPE_SPEAKER) {
            dispatchInt(ViperParams.PARAM_SPK_EQ_BAND_COUNT, count)
            dispatchEqBands(ViperParams.PARAM_SPK_EQ_BAND_LEVEL, bands)
        }
        loadEqPresetsForBandCount(count, isSpk = true)
    }

    fun addSpkEqPreset(name: String) {
        val state = _uiState.value
        val preset =
            EqPreset(name = name, bandCount = state.spkEqBandCount, bands = state.spkEqBands)
        viewModelScope.launch {
            val id = repository.saveEqPreset(preset)
            _uiState.update { it.copy(spkEqPresetId = id) }
            repository.setIntPreference("spk_eq_preset_id", id.toInt())
        }
    }

    fun deleteSpkEqPreset(presetId: Long) {
        viewModelScope.launch {
            repository.deleteEqPresetById(presetId)
            if (_uiState.value.spkEqPresetId == presetId) {
                _uiState.update { it.copy(spkEqPresetId = null) }
                repository.setIntPreference("spk_eq_preset_id", -1)
            }
        }
    }

    fun resetSpkEqBands() {
        val bandCount = _uiState.value.spkEqBandCount
        val flatBands = List(bandCount) { 0f }.joinToString(";") { "%.1f".format(it) } + ";"
        setSpkEqBands(flatBands)
        _uiState.update { it.copy(spkEqPresetId = null) }
        viewModelScope.launch { repository.setIntPreference("spk_eq_preset_id", -1) }
    }

    fun setSpkReverbEnabled(enabled: Boolean) {
        FileLogger.i("ViewModel", "[Spk] Reverb: ${if (enabled) "ON" else "OFF"}")
        _uiState.update { it.copy(spkReverbEnabled = enabled) }
        spkSaveAndDispatchBool(
            "${ViperParams.PARAM_SPK_REVERB_ENABLE}",
            ViperParams.PARAM_SPK_REVERB_ENABLE,
            enabled
        )
    }

    fun setSpkReverbRoomSize(v: Int) {
        _uiState.update { it.copy(spkReverbRoomSize = v) }; viewModelScope.launch {
            repository.setIntPreference(
                "${ViperParams.PARAM_SPK_REVERB_ROOM_SIZE}",
                v
            )
        }; spkDispatchInt(ViperParams.PARAM_SPK_REVERB_ROOM_SIZE, v * 10)
    }

    fun setSpkReverbWidth(v: Int) {
        _uiState.update { it.copy(spkReverbWidth = v) }; viewModelScope.launch {
            repository.setIntPreference(
                "${ViperParams.PARAM_SPK_REVERB_ROOM_WIDTH}",
                v
            )
        }; spkDispatchInt(ViperParams.PARAM_SPK_REVERB_ROOM_WIDTH, v * 10)
    }

    fun setSpkReverbDampening(v: Int) {
        _uiState.update { it.copy(spkReverbDampening = v) }; viewModelScope.launch {
            repository.setIntPreference(
                "${ViperParams.PARAM_SPK_REVERB_ROOM_DAMPENING}",
                v
            )
        }; spkDispatchInt(ViperParams.PARAM_SPK_REVERB_ROOM_DAMPENING, v)
    }

    fun setSpkReverbWet(v: Int) {
        _uiState.update { it.copy(spkReverbWet = v) }; viewModelScope.launch {
            repository.setIntPreference(
                "${ViperParams.PARAM_SPK_REVERB_ROOM_WET_SIGNAL}",
                v
            )
        }; spkDispatchInt(ViperParams.PARAM_SPK_REVERB_ROOM_WET_SIGNAL, v)
    }

    fun setSpkReverbDry(v: Int) {
        _uiState.update { it.copy(spkReverbDry = v) }; spkSaveAndDispatchInt(
            "${ViperParams.PARAM_SPK_REVERB_ROOM_DRY_SIGNAL}",
            ViperParams.PARAM_SPK_REVERB_ROOM_DRY_SIGNAL,
            v
        )
    }

    fun setSpkAgcEnabled(enabled: Boolean) {
        FileLogger.i("ViewModel", "[Spk] AGC: ${if (enabled) "ON" else "OFF"}")
        _uiState.update { it.copy(spkAgcEnabled = enabled) }
        spkSaveAndDispatchBool(
            "${ViperParams.PARAM_SPK_AGC_ENABLE}",
            ViperParams.PARAM_SPK_AGC_ENABLE,
            enabled
        )
    }

    fun setSpkAgcStrength(v: Int) {
        _uiState.update { it.copy(spkAgcStrength = v) }; viewModelScope.launch {
            repository.setIntPreference(
                "${ViperParams.PARAM_SPK_AGC_RATIO}",
                v
            )
        }; spkDispatchInt(
            ViperParams.PARAM_SPK_AGC_RATIO,
            PLAYBACK_GAIN_RATIO_VALUES.getOrElse(v) { 50 })
    }

    fun setSpkAgcMaxGain(v: Int) {
        _uiState.update { it.copy(spkAgcMaxGain = v) }; viewModelScope.launch {
            repository.setIntPreference(
                "${ViperParams.PARAM_SPK_AGC_MAX_SCALER}",
                v
            )
        }; spkDispatchInt(
            ViperParams.PARAM_SPK_AGC_MAX_SCALER,
            MULTI_FACTOR_VALUES.getOrElse(v) { 100 })
    }

    fun setSpkAgcOutputThreshold(v: Int) {
        _uiState.update { it.copy(spkAgcOutputThreshold = v) }; viewModelScope.launch {
            repository.setIntPreference(
                "${ViperParams.PARAM_SPK_AGC_VOLUME}",
                v
            )
        }; spkDispatchInt(ViperParams.PARAM_SPK_AGC_VOLUME, OUTPUT_DB_VALUES.getOrElse(v) { 100 })
    }

    fun setSpkFetEnabled(enabled: Boolean) {
        FileLogger.i("ViewModel", "[Spk] FET Compressor: ${if (enabled) "ON" else "OFF"}")
        _uiState.update { it.copy(spkFetEnabled = enabled) }
        spkSaveAndDispatchFetBool(
            "${ViperParams.PARAM_SPK_FET_COMPRESSOR_ENABLE}",
            ViperParams.PARAM_SPK_FET_COMPRESSOR_ENABLE,
            enabled
        )
    }

    fun setSpkFetThreshold(v: Int) {
        _uiState.update { it.copy(spkFetThreshold = v) }; spkSaveAndDispatchInt(
            "${ViperParams.PARAM_SPK_FET_COMPRESSOR_THRESHOLD}",
            ViperParams.PARAM_SPK_FET_COMPRESSOR_THRESHOLD,
            v
        )
    }

    fun setSpkFetRatio(v: Int) {
        _uiState.update { it.copy(spkFetRatio = v) }; spkSaveAndDispatchInt(
            "${ViperParams.PARAM_SPK_FET_COMPRESSOR_RATIO}",
            ViperParams.PARAM_SPK_FET_COMPRESSOR_RATIO,
            v
        )
    }

    fun setSpkFetAutoKnee(v: Boolean) {
        _uiState.update { it.copy(spkFetAutoKnee = v) }; spkSaveAndDispatchFetBool(
            "${ViperParams.PARAM_SPK_FET_COMPRESSOR_AUTO_KNEE}",
            ViperParams.PARAM_SPK_FET_COMPRESSOR_AUTO_KNEE,
            v
        )
    }

    fun setSpkFetKnee(v: Int) {
        _uiState.update { it.copy(spkFetKnee = v) }; spkSaveAndDispatchInt(
            "${ViperParams.PARAM_SPK_FET_COMPRESSOR_KNEE}",
            ViperParams.PARAM_SPK_FET_COMPRESSOR_KNEE,
            v
        )
    }

    fun setSpkFetKneeMulti(v: Int) {
        _uiState.update { it.copy(spkFetKneeMulti = v) }; spkSaveAndDispatchInt(
            "${ViperParams.PARAM_SPK_FET_COMPRESSOR_KNEE_MULTI}",
            ViperParams.PARAM_SPK_FET_COMPRESSOR_KNEE_MULTI,
            v
        )
    }

    fun setSpkFetAutoGain(v: Boolean) {
        _uiState.update { it.copy(spkFetAutoGain = v) }; spkSaveAndDispatchFetBool(
            "${ViperParams.PARAM_SPK_FET_COMPRESSOR_AUTO_GAIN}",
            ViperParams.PARAM_SPK_FET_COMPRESSOR_AUTO_GAIN,
            v
        )
    }

    fun setSpkFetGain(v: Int) {
        _uiState.update { it.copy(spkFetGain = v) }; spkSaveAndDispatchInt(
            "${ViperParams.PARAM_SPK_FET_COMPRESSOR_GAIN}",
            ViperParams.PARAM_SPK_FET_COMPRESSOR_GAIN,
            v
        )
    }

    fun setSpkFetAutoAttack(v: Boolean) {
        _uiState.update { it.copy(spkFetAutoAttack = v) }; spkSaveAndDispatchFetBool(
            "${ViperParams.PARAM_SPK_FET_COMPRESSOR_AUTO_ATTACK}",
            ViperParams.PARAM_SPK_FET_COMPRESSOR_AUTO_ATTACK,
            v
        )
    }

    fun setSpkFetAttack(v: Int) {
        _uiState.update { it.copy(spkFetAttack = v) }; spkSaveAndDispatchInt(
            "${ViperParams.PARAM_SPK_FET_COMPRESSOR_ATTACK}",
            ViperParams.PARAM_SPK_FET_COMPRESSOR_ATTACK,
            v
        )
    }

    fun setSpkFetMaxAttack(v: Int) {
        _uiState.update { it.copy(spkFetMaxAttack = v) }; spkSaveAndDispatchInt(
            "${ViperParams.PARAM_SPK_FET_COMPRESSOR_MAX_ATTACK}",
            ViperParams.PARAM_SPK_FET_COMPRESSOR_MAX_ATTACK,
            v
        )
    }

    fun setSpkFetAutoRelease(v: Boolean) {
        _uiState.update { it.copy(spkFetAutoRelease = v) }; spkSaveAndDispatchFetBool(
            "${ViperParams.PARAM_SPK_FET_COMPRESSOR_AUTO_RELEASE}",
            ViperParams.PARAM_SPK_FET_COMPRESSOR_AUTO_RELEASE,
            v
        )
    }

    fun setSpkFetRelease(v: Int) {
        _uiState.update { it.copy(spkFetRelease = v) }; spkSaveAndDispatchInt(
            "${ViperParams.PARAM_SPK_FET_COMPRESSOR_RELEASE}",
            ViperParams.PARAM_SPK_FET_COMPRESSOR_RELEASE,
            v
        )
    }

    fun setSpkFetMaxRelease(v: Int) {
        _uiState.update { it.copy(spkFetMaxRelease = v) }; spkSaveAndDispatchInt(
            "${ViperParams.PARAM_SPK_FET_COMPRESSOR_MAX_RELEASE}",
            ViperParams.PARAM_SPK_FET_COMPRESSOR_MAX_RELEASE,
            v
        )
    }

    fun setSpkFetCrest(v: Int) {
        _uiState.update { it.copy(spkFetCrest = v) }; spkSaveAndDispatchInt(
            "${ViperParams.PARAM_SPK_FET_COMPRESSOR_CREST}",
            ViperParams.PARAM_SPK_FET_COMPRESSOR_CREST,
            v
        )
    }

    fun setSpkFetAdapt(v: Int) {
        _uiState.update { it.copy(spkFetAdapt = v) }; spkSaveAndDispatchInt(
            "${ViperParams.PARAM_SPK_FET_COMPRESSOR_ADAPT}",
            ViperParams.PARAM_SPK_FET_COMPRESSOR_ADAPT,
            v
        )
    }

    fun setSpkFetNoClip(v: Boolean) {
        _uiState.update { it.copy(spkFetNoClip = v) }; spkSaveAndDispatchFetBool(
            "${ViperParams.PARAM_SPK_FET_COMPRESSOR_NO_CLIP}",
            ViperParams.PARAM_SPK_FET_COMPRESSOR_NO_CLIP,
            v
        )
    }

    fun setSpkOutputVolume(v: Int) {
        _uiState.update { it.copy(spkOutputVolume = v) }
        viewModelScope.launch {
            repository.setIntPreference(
                "${ViperParams.PARAM_SPK_OUTPUT_VOLUME}",
                v
            )
        }
        spkDispatchInt(
            ViperParams.PARAM_SPK_OUTPUT_VOLUME,
            OUTPUT_VOLUME_VALUES.getOrElse(v) { 100 })
    }

    fun setSpkLimiter(v: Int) {
        _uiState.update { it.copy(spkLimiter = v) }
        viewModelScope.launch { repository.setIntPreference("${ViperParams.PARAM_SPK_LIMITER}", v) }
        spkDispatchInt(ViperParams.PARAM_SPK_LIMITER, OUTPUT_DB_VALUES.getOrElse(v) { 100 })
    }

    fun setSpkDdcEnabled(enabled: Boolean) {
        FileLogger.i("ViewModel", "[Spk] DDC: ${if (enabled) "ON" else "OFF"}")
        _uiState.update { it.copy(spkDdcEnabled = enabled) }
        viewModelScope.launch {
            repository.setBooleanPreference(
                "spk_${ViperParams.PARAM_SPK_DDC_ENABLE}",
                enabled
            )
        }
        val device = _uiState.value.spkDdcDevice
        val effectiveEnabled = enabled && device.isNotEmpty()
        if (effectiveEnabled && activeDeviceType == ViperParams.FX_TYPE_SPEAKER) {
            loadVdcByName(device, ViperParams.PARAM_SPK_DDC_ENABLE)
        } else if (activeDeviceType == ViperParams.FX_TYPE_SPEAKER) {
            dispatchInt(ViperParams.PARAM_SPK_DDC_ENABLE, 0)
        }
    }

    fun setSpkDdcDevice(device: String) {
        FileLogger.i("ViewModel", "[Spk] DDC selected: $device")
        _uiState.update { it.copy(spkDdcDevice = device) }
        viewModelScope.launch { repository.setStringPreference("spk_ddc_device", device) }
        if (activeDeviceType == ViperParams.FX_TYPE_SPEAKER) {
            if (device.isEmpty()) {
                dispatchInt(ViperParams.PARAM_SPK_DDC_ENABLE, 0)
            } else {
                val enableParam =
                    if (_uiState.value.spkDdcEnabled) ViperParams.PARAM_SPK_DDC_ENABLE else null
                loadVdcByName(device, enableParam)
            }
        }
    }

    fun setSpkVseEnabled(enabled: Boolean) {
        FileLogger.i("ViewModel", "[Spk] VSE: ${if (enabled) "ON" else "OFF"}")
        _uiState.update { it.copy(spkVseEnabled = enabled) }
        spkSaveAndDispatchBool(
            "spk_${ViperParams.PARAM_SPK_SPECTRUM_EXTENSION_ENABLE}",
            ViperParams.PARAM_SPK_SPECTRUM_EXTENSION_ENABLE,
            enabled
        )
    }

    fun setSpkVseStrength(value: Int) {
        _uiState.update { it.copy(spkVseStrength = value) }
        viewModelScope.launch {
            repository.setIntPreference(
                "spk_${ViperParams.PARAM_SPK_SPECTRUM_EXTENSION_BARK}",
                value
            )
        }
        spkDispatchInt(
            ViperParams.PARAM_SPK_SPECTRUM_EXTENSION_BARK,
            VSE_BARK_VALUES.getOrElse(value) { 7600 })
    }

    fun setSpkVseExciter(value: Int) {
        _uiState.update { it.copy(spkVseExciter = value) }
        viewModelScope.launch {
            repository.setIntPreference(
                "spk_${ViperParams.PARAM_SPK_SPECTRUM_EXTENSION_BARK_RECONSTRUCT}",
                value
            )
        }
        spkDispatchInt(
            ViperParams.PARAM_SPK_SPECTRUM_EXTENSION_BARK_RECONSTRUCT,
            (value * 5.6).toInt()
        )
    }

    fun setSpkFieldSurroundEnabled(enabled: Boolean) {
        FileLogger.i("ViewModel", "[Spk] Field Surround: ${if (enabled) "ON" else "OFF"}")
        _uiState.update { it.copy(spkFieldSurroundEnabled = enabled) }
        spkSaveAndDispatchBool(
            "spk_${ViperParams.PARAM_SPK_FIELD_SURROUND_ENABLE}",
            ViperParams.PARAM_SPK_FIELD_SURROUND_ENABLE,
            enabled
        )
    }

    fun setSpkFieldSurroundWidening(value: Int) {
        _uiState.update { it.copy(spkFieldSurroundWidening = value) }
        viewModelScope.launch {
            repository.setIntPreference(
                "spk_${ViperParams.PARAM_SPK_FIELD_SURROUND_WIDENING}",
                value
            )
        }
        spkDispatchInt(
            ViperParams.PARAM_SPK_FIELD_SURROUND_WIDENING,
            FIELD_SURROUND_WIDENING_VALUES.getOrElse(value) { 0 })
    }

    fun setSpkFieldSurroundMidImage(value: Int) {
        _uiState.update { it.copy(spkFieldSurroundMidImage = value) }
        viewModelScope.launch {
            repository.setIntPreference(
                "spk_${ViperParams.PARAM_SPK_FIELD_SURROUND_MID_IMAGE}",
                value
            )
        }
        spkDispatchInt(ViperParams.PARAM_SPK_FIELD_SURROUND_MID_IMAGE, value * 10 + 100)
    }

    fun setSpkFieldSurroundDepth(value: Int) {
        _uiState.update { it.copy(spkFieldSurroundDepth = value) }
        viewModelScope.launch {
            repository.setIntPreference(
                "spk_${ViperParams.PARAM_SPK_FIELD_SURROUND_DEPTH}",
                value
            )
        }
        spkDispatchInt(ViperParams.PARAM_SPK_FIELD_SURROUND_DEPTH, value * 75 + 200)
    }

    fun setSpkDiffSurroundEnabled(enabled: Boolean) {
        FileLogger.i("ViewModel", "[Spk] Diff Surround: ${if (enabled) "ON" else "OFF"}")
        _uiState.update { it.copy(spkDiffSurroundEnabled = enabled) }
        spkSaveAndDispatchBool(
            "spk_${ViperParams.PARAM_SPK_DIFF_SURROUND_ENABLE}",
            ViperParams.PARAM_SPK_DIFF_SURROUND_ENABLE,
            enabled
        )
    }

    fun setSpkDiffSurroundDelay(value: Int) {
        _uiState.update { it.copy(spkDiffSurroundDelay = value) }
        viewModelScope.launch {
            repository.setIntPreference(
                "spk_${ViperParams.PARAM_SPK_DIFF_SURROUND_DELAY}",
                value
            )
        }
        spkDispatchInt(
            ViperParams.PARAM_SPK_DIFF_SURROUND_DELAY,
            DIFF_SURROUND_DELAY_VALUES.getOrElse(value) { 500 })
    }

    fun setSpkVheEnabled(enabled: Boolean) {
        FileLogger.i("ViewModel", "[Spk] VHE: ${if (enabled) "ON" else "OFF"}")
        _uiState.update { it.copy(spkVheEnabled = enabled) }
        spkSaveAndDispatchBool(
            "spk_${ViperParams.PARAM_SPK_HEADPHONE_SURROUND_ENABLE}",
            ViperParams.PARAM_SPK_HEADPHONE_SURROUND_ENABLE,
            enabled
        )
    }

    fun setSpkVheQuality(value: Int) {
        _uiState.update { it.copy(spkVheQuality = value) }
        spkSaveAndDispatchInt(
            "spk_${ViperParams.PARAM_SPK_HEADPHONE_SURROUND_STRENGTH}",
            ViperParams.PARAM_SPK_HEADPHONE_SURROUND_STRENGTH,
            value
        )
    }

    fun setSpkDynamicSystemEnabled(enabled: Boolean) {
        FileLogger.i("ViewModel", "[Spk] Dynamic System: ${if (enabled) "ON" else "OFF"}")
        _uiState.update { it.copy(spkDynamicSystemEnabled = enabled) }
        spkSaveAndDispatchBool(
            "spk_${ViperParams.PARAM_SPK_DYNAMIC_SYSTEM_ENABLE}",
            ViperParams.PARAM_SPK_DYNAMIC_SYSTEM_ENABLE,
            enabled
        )
    }

    fun setSpkDynamicSystemDevice(index: Int) {
        _uiState.update { it.copy(spkDynamicSystemDevice = index) }
        viewModelScope.launch { repository.setIntPreference("spk_dynamic_system_device", index) }
        if (activeDeviceType == ViperParams.FX_TYPE_SPEAKER) {
            val coeffs = DYNAMIC_SYSTEM_DEVICES.getOrElse(index) { "100;5600;40;80;50;50" }
            val parts = coeffs.split(";").map { it.toIntOrNull() ?: 0 }
            if (parts.size >= 6) {
                viperService?.dispatchParamsBatch(
                    listOf(
                        ParamEntry(
                            ViperParams.PARAM_SPK_DYNAMIC_SYSTEM_X_COEFFICIENTS,
                            intArrayOf(parts[0], parts[1])
                        ),
                        ParamEntry(
                            ViperParams.PARAM_SPK_DYNAMIC_SYSTEM_Y_COEFFICIENTS,
                            intArrayOf(parts[2], parts[3])
                        ),
                        ParamEntry(
                            ViperParams.PARAM_SPK_DYNAMIC_SYSTEM_SIDE_GAIN,
                            intArrayOf(parts[4], parts[5])
                        )
                    )
                )
            }
        }
    }

    fun setSpkDynamicSystemStrength(value: Int) {
        _uiState.update { it.copy(spkDynamicSystemStrength = value) }
        viewModelScope.launch {
            repository.setIntPreference(
                "spk_${ViperParams.PARAM_SPK_DYNAMIC_SYSTEM_STRENGTH}",
                value
            )
        }
        spkDispatchInt(ViperParams.PARAM_SPK_DYNAMIC_SYSTEM_STRENGTH, value * 20 + 100)
    }

    fun setSpkTubeSimulatorEnabled(enabled: Boolean) {
        FileLogger.i("ViewModel", "[Spk] Tube Simulator: ${if (enabled) "ON" else "OFF"}")
        _uiState.update { it.copy(spkTubeSimulatorEnabled = enabled) }
        spkSaveAndDispatchBool(
            "spk_${ViperParams.PARAM_SPK_TUBE_SIMULATOR_ENABLE}",
            ViperParams.PARAM_SPK_TUBE_SIMULATOR_ENABLE,
            enabled
        )
    }

    fun setSpkBassEnabled(enabled: Boolean) {
        FileLogger.i("ViewModel", "[Spk] Bass: ${if (enabled) "ON" else "OFF"}")
        _uiState.update { it.copy(spkBassEnabled = enabled) }
        spkSaveAndDispatchBool(
            "spk_${ViperParams.PARAM_SPK_BASS_ENABLE}",
            ViperParams.PARAM_SPK_BASS_ENABLE,
            enabled
        )
    }

    fun setSpkBassMode(mode: Int) {
        _uiState.update { it.copy(spkBassMode = mode) }; spkSaveAndDispatchInt(
            "spk_${ViperParams.PARAM_SPK_BASS_MODE}",
            ViperParams.PARAM_SPK_BASS_MODE,
            mode
        )
    }

    fun setSpkBassFrequency(v: Int) {
        _uiState.update { it.copy(spkBassFrequency = v) }; viewModelScope.launch {
            repository.setIntPreference(
                "spk_${ViperParams.PARAM_SPK_BASS_FREQUENCY}",
                v
            )
        }; spkDispatchInt(ViperParams.PARAM_SPK_BASS_FREQUENCY, v + 15)
    }

    fun setSpkBassGain(v: Int) {
        _uiState.update { it.copy(spkBassGain = v) }; viewModelScope.launch {
            repository.setIntPreference(
                "spk_${ViperParams.PARAM_SPK_BASS_GAIN}",
                v
            )
        }; spkDispatchInt(ViperParams.PARAM_SPK_BASS_GAIN, v * 50 + 50)
    }

    fun setSpkClarityEnabled(enabled: Boolean) {
        FileLogger.i("ViewModel", "[Spk] Clarity: ${if (enabled) "ON" else "OFF"}")
        _uiState.update { it.copy(spkClarityEnabled = enabled) }
        spkSaveAndDispatchBool(
            "spk_${ViperParams.PARAM_SPK_CLARITY_ENABLE}",
            ViperParams.PARAM_SPK_CLARITY_ENABLE,
            enabled
        )
    }

    fun setSpkClarityMode(mode: Int) {
        _uiState.update { it.copy(spkClarityMode = mode) }; spkSaveAndDispatchInt(
            "spk_${ViperParams.PARAM_SPK_CLARITY_MODE}",
            ViperParams.PARAM_SPK_CLARITY_MODE,
            mode
        )
    }

    fun setSpkClarityGain(v: Int) {
        _uiState.update { it.copy(spkClarityGain = v) }; viewModelScope.launch {
            repository.setIntPreference(
                "spk_${ViperParams.PARAM_SPK_CLARITY_GAIN}",
                v
            )
        }; spkDispatchInt(ViperParams.PARAM_SPK_CLARITY_GAIN, v * 50)
    }

    fun setSpkCureEnabled(enabled: Boolean) {
        FileLogger.i("ViewModel", "[Spk] Cure: ${if (enabled) "ON" else "OFF"}")
        _uiState.update { it.copy(spkCureEnabled = enabled) }
        spkSaveAndDispatchBool(
            "spk_${ViperParams.PARAM_SPK_CURE_ENABLE}",
            ViperParams.PARAM_SPK_CURE_ENABLE,
            enabled
        )
    }

    fun setSpkCureStrength(v: Int) {
        _uiState.update { it.copy(spkCureStrength = v) }; spkSaveAndDispatchInt(
            "spk_${ViperParams.PARAM_SPK_CURE_STRENGTH}",
            ViperParams.PARAM_SPK_CURE_STRENGTH,
            v
        )
    }

    fun setSpkAnalogxEnabled(enabled: Boolean) {
        FileLogger.i("ViewModel", "[Spk] AnalogX: ${if (enabled) "ON" else "OFF"}")
        _uiState.update { it.copy(spkAnalogxEnabled = enabled) }
        spkSaveAndDispatchBool(
            "spk_${ViperParams.PARAM_SPK_ANALOGX_ENABLE}",
            ViperParams.PARAM_SPK_ANALOGX_ENABLE,
            enabled
        )
    }

    fun setSpkAnalogxMode(mode: Int) {
        _uiState.update { it.copy(spkAnalogxMode = mode) }; spkSaveAndDispatchInt(
            "spk_${ViperParams.PARAM_SPK_ANALOGX_MODE}",
            ViperParams.PARAM_SPK_ANALOGX_MODE,
            mode
        )
    }

    fun setSpkChannelPan(value: Int) {
        _uiState.update { it.copy(spkChannelPan = value) }
        spkSaveAndDispatchInt(
            "spk_${ViperParams.PARAM_SPK_CHANNEL_PAN}",
            ViperParams.PARAM_SPK_CHANNEL_PAN,
            value
        )
    }

    private fun getFilesDir(subDir: String): File {
        val dir = File(getApplication<Application>().getExternalFilesDir(null), subDir)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun refreshFileLists() {
        val ddcDir = getFilesDir("DDC")
        _vdcFileList.value = ddcDir.listFiles()
            ?.filter { it.extension == "vdc" }
            ?.map { it.nameWithoutExtension }
            ?.sorted() ?: emptyList()

        val kernelDir = getFilesDir("Kernel")
        _kernelFileList.value = kernelDir.listFiles()
            ?.map { it.name }
            ?.sorted() ?: emptyList()
    }

    fun deleteVdcFile(name: String): Boolean {
        return try {
            val file = File(getFilesDir("DDC"), "$name.vdc")
            if (!file.exists()) return false
            file.delete()
            val state = _uiState.value
            if (state.ddcDevice == name) {
                _uiState.update { it.copy(ddcDevice = "") }
                viewModelScope.launch { repository.setStringPreference("ddc_device", "") }
            }
            if (state.spkDdcDevice == name) {
                _uiState.update { it.copy(spkDdcDevice = "") }
                viewModelScope.launch { repository.setStringPreference("spk_ddc_device", "") }
            }
            refreshFileLists()
            true
        } catch (e: Exception) {
            FileLogger.e("ViewModel", "Failed to delete VDC: $name", e)
            false
        }
    }

    fun deleteKernelFile(fileName: String): Boolean {
        return try {
            val file = File(getFilesDir("Kernel"), fileName)
            if (!file.exists()) return false
            file.delete()
            val state = _uiState.value
            if (state.convolverKernel == fileName) {
                _uiState.update { it.copy(convolverKernel = "") }
                viewModelScope.launch { repository.setStringPreference("convolver_kernel", "") }
            }
            if (state.spkConvolverKernel == fileName) {
                _uiState.update { it.copy(spkConvolverKernel = "") }
                viewModelScope.launch { repository.setStringPreference("spk_convolver_kernel", "") }
            }
            refreshFileLists()
            true
        } catch (e: Exception) {
            FileLogger.e("ViewModel", "Failed to delete kernel: $fileName", e)
            false
        }
    }

    fun loadVdcByName(name: String, enableParam: Int? = null): Boolean {
        // TODO: Implement this.
        return false
    }

    fun loadKernelByName(fileName: String, enableParam: Int? = null): Boolean {
        // TODO: Implement this.
        return false
    }

    private fun loadSettingsPreferences() {
        viewModelScope.launch {
            repository.getBooleanPreference(PREF_AUTO_START).collect { v ->
                _autoStartEnabled.value = v
            }
        }
        viewModelScope.launch {
            repository.getBooleanPreference(PREF_AIDL_MODE).collect { v ->
                _aidlModeEnabled.value = v
            }
        }
        viewModelScope.launch {
            repository.getBooleanPreference("debug_mode").collect { v ->
                _debugModeEnabled.value = v
            }
        }
    }

    fun queryDriverStatus() {
        if (_aidlModeEnabled.value) {
            // TODO: AIDL driver status query.
            return
        }
        val effect = viperService?.getGlobalEffect()
        if (effect != null && effect.isCreated) {
            queryDriverStatusFrom(effect)
            return
        }
        val typeUuid = ViperEffect.EFFECT_TYPE_UUID
        val probe = ViperEffect(0, typeUuid)
        if (!probe.create()) {
            _driverStatus.value = DriverStatus(installed = false)
            probe.release()
            return
        }
        queryDriverStatusFrom(probe)
        probe.release()
    }

    private fun queryDriverStatusFrom(effect: ViperEffect) {
        val versionCode = effect.getDriverVersionCode()
        val archName = effect.getArchitectureString()
        val streaming = effect.isStreaming()
        val samplingRate = effect.getParameter(ViperParams.PARAM_GET_SAMPLING_RATE)

        val versionBytes = effect.getParameter(ViperParams.PARAM_GET_DRIVER_VERSION_NAME, 256)
        val versionName = if (versionBytes.isNotEmpty()) {
            val nullIdx = versionBytes.indexOf(0.toByte())
            if (nullIdx >= 0) String(versionBytes, 0, nullIdx) else String(versionBytes)
        } else {
            versionCode.toString()
        }

        _driverStatus.value = DriverStatus(
            installed = true,
            versionCode = versionCode,
            versionName = versionName,
            architecture = archName,
            streaming = streaming,
            samplingRate = samplingRate
        )
    }

    private fun saveAndDispatchInt(prefKey: String, param: Int, value: Int) {
        viewModelScope.launch { repository.setIntPreference(prefKey, value) }
        if (activeDeviceType == ViperParams.FX_TYPE_HEADPHONE) dispatchInt(param, value)
    }

    private fun saveAndDispatchBool(prefKey: String, param: Int, value: Boolean) {
        viewModelScope.launch { repository.setBooleanPreference(prefKey, value) }
        if (activeDeviceType == ViperParams.FX_TYPE_HEADPHONE) dispatchInt(
            param,
            if (value) 1 else 0
        )
    }

    private fun saveAndDispatchFetBool(prefKey: String, param: Int, value: Boolean) {
        viewModelScope.launch { repository.setBooleanPreference(prefKey, value) }
        if (activeDeviceType == ViperParams.FX_TYPE_HEADPHONE) dispatchInt(
            param,
            if (value) 100 else 0
        )
    }

    private fun spkSaveAndDispatchInt(prefKey: String, param: Int, value: Int) {
        viewModelScope.launch { repository.setIntPreference(prefKey, value) }
        if (activeDeviceType == ViperParams.FX_TYPE_SPEAKER) dispatchInt(param, value)
    }

    private fun spkSaveAndDispatchBool(prefKey: String, param: Int, value: Boolean) {
        viewModelScope.launch { repository.setBooleanPreference(prefKey, value) }
        if (activeDeviceType == ViperParams.FX_TYPE_SPEAKER) dispatchInt(param, if (value) 1 else 0)
    }

    private fun spkSaveAndDispatchFetBool(prefKey: String, param: Int, value: Boolean) {
        viewModelScope.launch { repository.setBooleanPreference(prefKey, value) }
        if (activeDeviceType == ViperParams.FX_TYPE_SPEAKER) dispatchInt(
            param,
            if (value) 100 else 0
        )
    }

    private fun dispatchInt(param: Int, value: Int) {
        viperService?.dispatchParam(param, value)
    }

    private fun hpDispatchInt(param: Int, value: Int) {
        if (activeDeviceType == ViperParams.FX_TYPE_HEADPHONE) dispatchInt(param, value)
    }

    private fun spkDispatchInt(param: Int, value: Int) {
        if (activeDeviceType == ViperParams.FX_TYPE_SPEAKER) dispatchInt(param, value)
    }

    private fun dispatchEqBands(param: Int, bandsString: String) {
        viperService?.dispatchEqBands(param, bandsString)
    }

    private fun hpDispatchEqBands(param: Int, bandsString: String) {
        if (activeDeviceType == ViperParams.FX_TYPE_HEADPHONE) dispatchEqBands(param, bandsString)
    }

    private fun spkDispatchEqBands(param: Int, bandsString: String) {
        if (activeDeviceType == ViperParams.FX_TYPE_SPEAKER) dispatchEqBands(param, bandsString)
    }
}
