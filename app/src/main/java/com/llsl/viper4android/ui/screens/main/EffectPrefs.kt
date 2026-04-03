package com.llsl.viper4android.ui.screens.main

import com.llsl.viper4android.audio.ViperParams
import com.llsl.viper4android.data.repository.ViperRepository
import kotlinx.coroutines.flow.first
import org.json.JSONObject

sealed class EffectPref<T>(
    val hpPrefKey: String,
    val spkPrefKey: String,
    val jsonKey: String,
    val spkJsonKey: String,
    val defaultValue: T,
    val getHp: (MainUiState) -> T,
    val setHp: MainUiState.(T) -> MainUiState,
    val getSpk: (MainUiState) -> T,
    val setSpk: MainUiState.(T) -> MainUiState
)

class IntPref(
    hpPrefKey: String,
    spkPrefKey: String,
    jsonKey: String,
    spkJsonKey: String,
    defaultValue: Int,
    getHp: (MainUiState) -> Int,
    setHp: MainUiState.(Int) -> MainUiState,
    getSp: (MainUiState) -> Int,
    setSp: MainUiState.(Int) -> MainUiState
) : EffectPref<Int>(
    hpPrefKey,
    spkPrefKey,
    jsonKey,
    spkJsonKey,
    defaultValue,
    getHp,
    setHp,
    getSp,
    setSp
)

class BoolPref(
    hpPrefKey: String,
    spkPrefKey: String,
    jsonKey: String,
    spkJsonKey: String,
    defaultValue: Boolean,
    getHp: (MainUiState) -> Boolean,
    setHp: MainUiState.(Boolean) -> MainUiState,
    getSp: (MainUiState) -> Boolean,
    setSp: MainUiState.(Boolean) -> MainUiState
) : EffectPref<Boolean>(
    hpPrefKey,
    spkPrefKey,
    jsonKey,
    spkJsonKey,
    defaultValue,
    getHp,
    setHp,
    getSp,
    setSp
)

class StringPref(
    hpPrefKey: String,
    spkPrefKey: String,
    jsonKey: String,
    spkJsonKey: String,
    defaultValue: String,
    getHp: (MainUiState) -> String,
    setHp: MainUiState.(String) -> MainUiState,
    getSp: (MainUiState) -> String,
    setSp: MainUiState.(String) -> MainUiState
) : EffectPref<String>(
    hpPrefKey,
    spkPrefKey,
    jsonKey,
    spkJsonKey,
    defaultValue,
    getHp,
    setHp,
    getSp,
    setSp
)

class NullableLongPref(
    hpPrefKey: String,
    spkPrefKey: String,
    jsonKey: String,
    spkJsonKey: String,
    getHp: (MainUiState) -> Long?,
    setHp: MainUiState.(Long?) -> MainUiState,
    getSp: (MainUiState) -> Long?,
    setSp: MainUiState.(Long?) -> MainUiState
) : EffectPref<Long?>(hpPrefKey, spkPrefKey, jsonKey, spkJsonKey, null, getHp, setHp, getSp, setSp)

val EFFECT_PREFS: List<EffectPref<*>> = listOf(
    BoolPref(
        hpPrefKey = ViperRepository.PREF_MASTER_ENABLE,
        spkPrefKey = "spk_${ViperRepository.PREF_MASTER_ENABLE}",
        jsonKey = "masterEnabled", spkJsonKey = "spkMasterEnabled",
        defaultValue = false,
        getHp = { it.masterEnabled }, setHp = { copy(masterEnabled = it) },
        getSp = { it.spkMasterEnabled }, setSp = { copy(spkMasterEnabled = it) }
    ),
    IntPref(
        hpPrefKey = "${ViperParams.PARAM_HP_OUTPUT_VOLUME}",
        spkPrefKey = "${ViperParams.PARAM_SPK_OUTPUT_VOLUME}",
        jsonKey = "outputVolume", spkJsonKey = "spkOutputVolume",
        defaultValue = 11,
        getHp = { it.outputVolume }, setHp = { copy(outputVolume = it) },
        getSp = { it.spkOutputVolume }, setSp = { copy(spkOutputVolume = it) }
    ),
    IntPref(
        hpPrefKey = "${ViperParams.PARAM_HP_CHANNEL_PAN}",
        spkPrefKey = "spk_${ViperParams.PARAM_SPK_CHANNEL_PAN}",
        jsonKey = "channelPan", spkJsonKey = "spkChannelPan",
        defaultValue = 0,
        getHp = { it.channelPan }, setHp = { copy(channelPan = it) },
        getSp = { it.spkChannelPan }, setSp = { copy(spkChannelPan = it) }
    ),
    IntPref(
        hpPrefKey = "${ViperParams.PARAM_HP_LIMITER}",
        spkPrefKey = "${ViperParams.PARAM_SPK_LIMITER}",
        jsonKey = "limiter", spkJsonKey = "spkLimiter",
        defaultValue = 5,
        getHp = { it.limiter }, setHp = { copy(limiter = it) },
        getSp = { it.spkLimiter }, setSp = { copy(spkLimiter = it) }
    ),

    // AGC
    BoolPref(
        hpPrefKey = "${ViperParams.PARAM_HP_AGC_ENABLE}",
        spkPrefKey = "${ViperParams.PARAM_SPK_AGC_ENABLE}",
        jsonKey = "agcEnabled", spkJsonKey = "spkAgcEnabled",
        defaultValue = false,
        getHp = { it.agcEnabled }, setHp = { copy(agcEnabled = it) },
        getSp = { it.spkAgcEnabled }, setSp = { copy(spkAgcEnabled = it) }
    ),
    IntPref(
        hpPrefKey = "${ViperParams.PARAM_HP_AGC_RATIO}",
        spkPrefKey = "${ViperParams.PARAM_SPK_AGC_RATIO}",
        jsonKey = "agcStrength", spkJsonKey = "spkAgcStrength",
        defaultValue = 0,
        getHp = { it.agcStrength }, setHp = { copy(agcStrength = it) },
        getSp = { it.spkAgcStrength }, setSp = { copy(spkAgcStrength = it) }
    ),
    IntPref(
        hpPrefKey = "${ViperParams.PARAM_HP_AGC_MAX_SCALER}",
        spkPrefKey = "${ViperParams.PARAM_SPK_AGC_MAX_SCALER}",
        jsonKey = "agcMaxGain", spkJsonKey = "spkAgcMaxGain",
        defaultValue = 3,
        getHp = { it.agcMaxGain }, setHp = { copy(agcMaxGain = it) },
        getSp = { it.spkAgcMaxGain }, setSp = { copy(spkAgcMaxGain = it) }
    ),
    IntPref(
        hpPrefKey = "${ViperParams.PARAM_HP_AGC_VOLUME}",
        spkPrefKey = "${ViperParams.PARAM_SPK_AGC_VOLUME}",
        jsonKey = "agcOutputThreshold", spkJsonKey = "spkAgcOutputThreshold",
        defaultValue = 3,
        getHp = { it.agcOutputThreshold }, setHp = { copy(agcOutputThreshold = it) },
        getSp = { it.spkAgcOutputThreshold }, setSp = { copy(spkAgcOutputThreshold = it) }
    ),

    // FET Compressor
    BoolPref(
        hpPrefKey = "${ViperParams.PARAM_HP_FET_COMPRESSOR_ENABLE}",
        spkPrefKey = "${ViperParams.PARAM_SPK_FET_COMPRESSOR_ENABLE}",
        jsonKey = "fetEnabled", spkJsonKey = "spkFetEnabled",
        defaultValue = false,
        getHp = { it.fetEnabled }, setHp = { copy(fetEnabled = it) },
        getSp = { it.spkFetEnabled }, setSp = { copy(spkFetEnabled = it) }
    ),
    IntPref(
        hpPrefKey = "${ViperParams.PARAM_HP_FET_COMPRESSOR_THRESHOLD}",
        spkPrefKey = "${ViperParams.PARAM_SPK_FET_COMPRESSOR_THRESHOLD}",
        jsonKey = "fetThreshold", spkJsonKey = "spkFetThreshold",
        defaultValue = 100,
        getHp = { it.fetThreshold }, setHp = { copy(fetThreshold = it) },
        getSp = { it.spkFetThreshold }, setSp = { copy(spkFetThreshold = it) }
    ),
    IntPref(
        hpPrefKey = "${ViperParams.PARAM_HP_FET_COMPRESSOR_RATIO}",
        spkPrefKey = "${ViperParams.PARAM_SPK_FET_COMPRESSOR_RATIO}",
        jsonKey = "fetRatio", spkJsonKey = "spkFetRatio",
        defaultValue = 100,
        getHp = { it.fetRatio }, setHp = { copy(fetRatio = it) },
        getSp = { it.spkFetRatio }, setSp = { copy(spkFetRatio = it) }
    ),
    BoolPref(
        hpPrefKey = "${ViperParams.PARAM_HP_FET_COMPRESSOR_AUTO_KNEE}",
        spkPrefKey = "${ViperParams.PARAM_SPK_FET_COMPRESSOR_AUTO_KNEE}",
        jsonKey = "fetAutoKnee", spkJsonKey = "spkFetAutoKnee",
        defaultValue = true,
        getHp = { it.fetAutoKnee }, setHp = { copy(fetAutoKnee = it) },
        getSp = { it.spkFetAutoKnee }, setSp = { copy(spkFetAutoKnee = it) }
    ),
    IntPref(
        hpPrefKey = "${ViperParams.PARAM_HP_FET_COMPRESSOR_KNEE}",
        spkPrefKey = "${ViperParams.PARAM_SPK_FET_COMPRESSOR_KNEE}",
        jsonKey = "fetKnee", spkJsonKey = "spkFetKnee",
        defaultValue = 0,
        getHp = { it.fetKnee }, setHp = { copy(fetKnee = it) },
        getSp = { it.spkFetKnee }, setSp = { copy(spkFetKnee = it) }
    ),
    IntPref(
        hpPrefKey = "${ViperParams.PARAM_HP_FET_COMPRESSOR_KNEE_MULTI}",
        spkPrefKey = "${ViperParams.PARAM_SPK_FET_COMPRESSOR_KNEE_MULTI}",
        jsonKey = "fetKneeMulti", spkJsonKey = "spkFetKneeMulti",
        defaultValue = 0,
        getHp = { it.fetKneeMulti }, setHp = { copy(fetKneeMulti = it) },
        getSp = { it.spkFetKneeMulti }, setSp = { copy(spkFetKneeMulti = it) }
    ),
    BoolPref(
        hpPrefKey = "${ViperParams.PARAM_HP_FET_COMPRESSOR_AUTO_GAIN}",
        spkPrefKey = "${ViperParams.PARAM_SPK_FET_COMPRESSOR_AUTO_GAIN}",
        jsonKey = "fetAutoGain", spkJsonKey = "spkFetAutoGain",
        defaultValue = true,
        getHp = { it.fetAutoGain }, setHp = { copy(fetAutoGain = it) },
        getSp = { it.spkFetAutoGain }, setSp = { copy(spkFetAutoGain = it) }
    ),
    IntPref(
        hpPrefKey = "${ViperParams.PARAM_HP_FET_COMPRESSOR_GAIN}",
        spkPrefKey = "${ViperParams.PARAM_SPK_FET_COMPRESSOR_GAIN}",
        jsonKey = "fetGain", spkJsonKey = "spkFetGain",
        defaultValue = 0,
        getHp = { it.fetGain }, setHp = { copy(fetGain = it) },
        getSp = { it.spkFetGain }, setSp = { copy(spkFetGain = it) }
    ),
    BoolPref(
        hpPrefKey = "${ViperParams.PARAM_HP_FET_COMPRESSOR_AUTO_ATTACK}",
        spkPrefKey = "${ViperParams.PARAM_SPK_FET_COMPRESSOR_AUTO_ATTACK}",
        jsonKey = "fetAutoAttack", spkJsonKey = "spkFetAutoAttack",
        defaultValue = true,
        getHp = { it.fetAutoAttack }, setHp = { copy(fetAutoAttack = it) },
        getSp = { it.spkFetAutoAttack }, setSp = { copy(spkFetAutoAttack = it) }
    ),
    IntPref(
        hpPrefKey = "${ViperParams.PARAM_HP_FET_COMPRESSOR_ATTACK}",
        spkPrefKey = "${ViperParams.PARAM_SPK_FET_COMPRESSOR_ATTACK}",
        jsonKey = "fetAttack", spkJsonKey = "spkFetAttack",
        defaultValue = 20,
        getHp = { it.fetAttack }, setHp = { copy(fetAttack = it) },
        getSp = { it.spkFetAttack }, setSp = { copy(spkFetAttack = it) }
    ),
    IntPref(
        hpPrefKey = "${ViperParams.PARAM_HP_FET_COMPRESSOR_MAX_ATTACK}",
        spkPrefKey = "${ViperParams.PARAM_SPK_FET_COMPRESSOR_MAX_ATTACK}",
        jsonKey = "fetMaxAttack", spkJsonKey = "spkFetMaxAttack",
        defaultValue = 80,
        getHp = { it.fetMaxAttack }, setHp = { copy(fetMaxAttack = it) },
        getSp = { it.spkFetMaxAttack }, setSp = { copy(spkFetMaxAttack = it) }
    ),
    BoolPref(
        hpPrefKey = "${ViperParams.PARAM_HP_FET_COMPRESSOR_AUTO_RELEASE}",
        spkPrefKey = "${ViperParams.PARAM_SPK_FET_COMPRESSOR_AUTO_RELEASE}",
        jsonKey = "fetAutoRelease", spkJsonKey = "spkFetAutoRelease",
        defaultValue = true,
        getHp = { it.fetAutoRelease }, setHp = { copy(fetAutoRelease = it) },
        getSp = { it.spkFetAutoRelease }, setSp = { copy(spkFetAutoRelease = it) }
    ),
    IntPref(
        hpPrefKey = "${ViperParams.PARAM_HP_FET_COMPRESSOR_RELEASE}",
        spkPrefKey = "${ViperParams.PARAM_SPK_FET_COMPRESSOR_RELEASE}",
        jsonKey = "fetRelease", spkJsonKey = "spkFetRelease",
        defaultValue = 50,
        getHp = { it.fetRelease }, setHp = { copy(fetRelease = it) },
        getSp = { it.spkFetRelease }, setSp = { copy(spkFetRelease = it) }
    ),
    IntPref(
        hpPrefKey = "${ViperParams.PARAM_HP_FET_COMPRESSOR_MAX_RELEASE}",
        spkPrefKey = "${ViperParams.PARAM_SPK_FET_COMPRESSOR_MAX_RELEASE}",
        jsonKey = "fetMaxRelease", spkJsonKey = "spkFetMaxRelease",
        defaultValue = 100,
        getHp = { it.fetMaxRelease }, setHp = { copy(fetMaxRelease = it) },
        getSp = { it.spkFetMaxRelease }, setSp = { copy(spkFetMaxRelease = it) }
    ),
    IntPref(
        hpPrefKey = "${ViperParams.PARAM_HP_FET_COMPRESSOR_CREST}",
        spkPrefKey = "${ViperParams.PARAM_SPK_FET_COMPRESSOR_CREST}",
        jsonKey = "fetCrest", spkJsonKey = "spkFetCrest",
        defaultValue = 100,
        getHp = { it.fetCrest }, setHp = { copy(fetCrest = it) },
        getSp = { it.spkFetCrest }, setSp = { copy(spkFetCrest = it) }
    ),
    IntPref(
        hpPrefKey = "${ViperParams.PARAM_HP_FET_COMPRESSOR_ADAPT}",
        spkPrefKey = "${ViperParams.PARAM_SPK_FET_COMPRESSOR_ADAPT}",
        jsonKey = "fetAdapt", spkJsonKey = "spkFetAdapt",
        defaultValue = 50,
        getHp = { it.fetAdapt }, setHp = { copy(fetAdapt = it) },
        getSp = { it.spkFetAdapt }, setSp = { copy(spkFetAdapt = it) }
    ),
    BoolPref(
        hpPrefKey = "${ViperParams.PARAM_HP_FET_COMPRESSOR_NO_CLIP}",
        spkPrefKey = "${ViperParams.PARAM_SPK_FET_COMPRESSOR_NO_CLIP}",
        jsonKey = "fetNoClip", spkJsonKey = "spkFetNoClip",
        defaultValue = true,
        getHp = { it.fetNoClip }, setHp = { copy(fetNoClip = it) },
        getSp = { it.spkFetNoClip }, setSp = { copy(spkFetNoClip = it) }
    ),

    // DDC
    BoolPref(
        hpPrefKey = "${ViperParams.PARAM_HP_DDC_ENABLE}",
        spkPrefKey = "spk_${ViperParams.PARAM_SPK_DDC_ENABLE}",
        jsonKey = "ddcEnabled", spkJsonKey = "spkDdcEnabled",
        defaultValue = false,
        getHp = { it.ddcEnabled }, setHp = { copy(ddcEnabled = it) },
        getSp = { it.spkDdcEnabled }, setSp = { copy(spkDdcEnabled = it) }
    ),
    StringPref(
        hpPrefKey = ViperRepository.PREF_DDC_DEVICE,
        spkPrefKey = "spk_${ViperRepository.PREF_DDC_DEVICE}",
        jsonKey = "ddcDevice", spkJsonKey = "spkDdcDevice",
        defaultValue = "",
        getHp = { it.ddcDevice }, setHp = { copy(ddcDevice = it) },
        getSp = { it.spkDdcDevice }, setSp = { copy(spkDdcDevice = it) }
    ),

    // Spectrum Extension
    BoolPref(
        hpPrefKey = "${ViperParams.PARAM_HP_SPECTRUM_EXTENSION_ENABLE}",
        spkPrefKey = "spk_${ViperParams.PARAM_SPK_SPECTRUM_EXTENSION_ENABLE}",
        jsonKey = "vseEnabled", spkJsonKey = "spkVseEnabled",
        defaultValue = false,
        getHp = { it.vseEnabled }, setHp = { copy(vseEnabled = it) },
        getSp = { it.spkVseEnabled }, setSp = { copy(spkVseEnabled = it) }
    ),
    IntPref(
        hpPrefKey = "${ViperParams.PARAM_HP_SPECTRUM_EXTENSION_BARK}",
        spkPrefKey = "spk_${ViperParams.PARAM_SPK_SPECTRUM_EXTENSION_BARK}",
        jsonKey = "vseStrength", spkJsonKey = "spkVseStrength",
        defaultValue = 10,
        getHp = { it.vseStrength }, setHp = { copy(vseStrength = it) },
        getSp = { it.spkVseStrength }, setSp = { copy(spkVseStrength = it) }
    ),
    IntPref(
        hpPrefKey = "${ViperParams.PARAM_HP_SPECTRUM_EXTENSION_BARK_RECONSTRUCT}",
        spkPrefKey = "spk_${ViperParams.PARAM_SPK_SPECTRUM_EXTENSION_BARK_RECONSTRUCT}",
        jsonKey = "vseExciter", spkJsonKey = "spkVseExciter",
        defaultValue = 0,
        getHp = { it.vseExciter }, setHp = { copy(vseExciter = it) },
        getSp = { it.spkVseExciter }, setSp = { copy(spkVseExciter = it) }
    ),

    // EQ
    BoolPref(
        hpPrefKey = "${ViperParams.PARAM_HP_EQ_ENABLE}",
        spkPrefKey = "${ViperParams.PARAM_SPK_EQ_ENABLE}",
        jsonKey = "eqEnabled", spkJsonKey = "spkEqEnabled",
        defaultValue = false,
        getHp = { it.eqEnabled }, setHp = { copy(eqEnabled = it) },
        getSp = { it.spkEqEnabled }, setSp = { copy(spkEqEnabled = it) }
    ),
    IntPref(
        hpPrefKey = "${ViperParams.PARAM_HP_EQ_BAND_COUNT}",
        spkPrefKey = "spk_${ViperParams.PARAM_SPK_EQ_BAND_COUNT}",
        jsonKey = "eqBandCount", spkJsonKey = "spkEqBandCount",
        defaultValue = 10,
        getHp = { it.eqBandCount }, setHp = { copy(eqBandCount = it) },
        getSp = { it.spkEqBandCount }, setSp = { copy(spkEqBandCount = it) }
    ),
    StringPref(
        hpPrefKey = "${ViperParams.PARAM_HP_EQ_BAND_LEVEL}",
        spkPrefKey = "${ViperParams.PARAM_SPK_EQ_BAND_LEVEL}",
        jsonKey = "eqBands", spkJsonKey = "spkEqBands",
        defaultValue = "0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;",
        getHp = { it.eqBands }, setHp = { copy(eqBands = it) },
        getSp = { it.spkEqBands }, setSp = { copy(spkEqBands = it) }
    ),
    NullableLongPref(
        hpPrefKey = ViperRepository.PREF_EQ_PRESET_ID,
        spkPrefKey = "spk_${ViperRepository.PREF_EQ_PRESET_ID}",
        jsonKey = "eqPresetId", spkJsonKey = "spkEqPresetId",
        getHp = { it.eqPresetId }, setHp = { copy(eqPresetId = it) },
        getSp = { it.spkEqPresetId }, setSp = { copy(spkEqPresetId = it) }
    ),

    // Convolver
    BoolPref(
        hpPrefKey = "${ViperParams.PARAM_HP_CONVOLVER_ENABLE}",
        spkPrefKey = "${ViperParams.PARAM_SPK_CONVOLVER_ENABLE}",
        jsonKey = "convolverEnabled", spkJsonKey = "spkConvolverEnabled",
        defaultValue = false,
        getHp = { it.convolverEnabled }, setHp = { copy(convolverEnabled = it) },
        getSp = { it.spkConvolverEnabled }, setSp = { copy(spkConvolverEnabled = it) }
    ),
    StringPref(
        hpPrefKey = "${ViperParams.PARAM_HP_CONVOLVER_SET_KERNEL}",
        spkPrefKey = "spk_${ViperParams.PARAM_SPK_CONVOLVER_SET_KERNEL}",
        jsonKey = "convolverKernel", spkJsonKey = "spkConvolverKernel",
        defaultValue = "",
        getHp = { it.convolverKernel }, setHp = { copy(convolverKernel = it) },
        getSp = { it.spkConvolverKernel }, setSp = { copy(spkConvolverKernel = it) }
    ),
    IntPref(
        hpPrefKey = "${ViperParams.PARAM_HP_CONVOLVER_CROSS_CHANNEL}",
        spkPrefKey = "${ViperParams.PARAM_SPK_CONVOLVER_CROSS_CHANNEL}",
        jsonKey = "convolverCrossChannel", spkJsonKey = "spkConvolverCrossChannel",
        defaultValue = 0,
        getHp = { it.convolverCrossChannel }, setHp = { copy(convolverCrossChannel = it) },
        getSp = { it.spkConvolverCrossChannel }, setSp = { copy(spkConvolverCrossChannel = it) }
    ),

    // Field Surround
    BoolPref(
        hpPrefKey = "${ViperParams.PARAM_HP_FIELD_SURROUND_ENABLE}",
        spkPrefKey = "spk_${ViperParams.PARAM_SPK_FIELD_SURROUND_ENABLE}",
        jsonKey = "fieldSurroundEnabled", spkJsonKey = "spkFieldSurroundEnabled",
        defaultValue = false,
        getHp = { it.fieldSurroundEnabled }, setHp = { copy(fieldSurroundEnabled = it) },
        getSp = { it.spkFieldSurroundEnabled }, setSp = { copy(spkFieldSurroundEnabled = it) }
    ),
    IntPref(
        hpPrefKey = "${ViperParams.PARAM_HP_FIELD_SURROUND_WIDENING}",
        spkPrefKey = "spk_${ViperParams.PARAM_SPK_FIELD_SURROUND_WIDENING}",
        jsonKey = "fieldSurroundWidening", spkJsonKey = "spkFieldSurroundWidening",
        defaultValue = 0,
        getHp = { it.fieldSurroundWidening }, setHp = { copy(fieldSurroundWidening = it) },
        getSp = { it.spkFieldSurroundWidening }, setSp = { copy(spkFieldSurroundWidening = it) }
    ),
    IntPref(
        hpPrefKey = "${ViperParams.PARAM_HP_FIELD_SURROUND_MID_IMAGE}",
        spkPrefKey = "spk_${ViperParams.PARAM_SPK_FIELD_SURROUND_MID_IMAGE}",
        jsonKey = "fieldSurroundMidImage", spkJsonKey = "spkFieldSurroundMidImage",
        defaultValue = 5,
        getHp = { it.fieldSurroundMidImage }, setHp = { copy(fieldSurroundMidImage = it) },
        getSp = { it.spkFieldSurroundMidImage }, setSp = { copy(spkFieldSurroundMidImage = it) }
    ),
    IntPref(
        hpPrefKey = "${ViperParams.PARAM_HP_FIELD_SURROUND_DEPTH}",
        spkPrefKey = "spk_${ViperParams.PARAM_SPK_FIELD_SURROUND_DEPTH}",
        jsonKey = "fieldSurroundDepth", spkJsonKey = "spkFieldSurroundDepth",
        defaultValue = 0,
        getHp = { it.fieldSurroundDepth }, setHp = { copy(fieldSurroundDepth = it) },
        getSp = { it.spkFieldSurroundDepth }, setSp = { copy(spkFieldSurroundDepth = it) }
    ),

    // Diff Surround
    BoolPref(
        hpPrefKey = "${ViperParams.PARAM_HP_DIFF_SURROUND_ENABLE}",
        spkPrefKey = "spk_${ViperParams.PARAM_SPK_DIFF_SURROUND_ENABLE}",
        jsonKey = "diffSurroundEnabled", spkJsonKey = "spkDiffSurroundEnabled",
        defaultValue = false,
        getHp = { it.diffSurroundEnabled }, setHp = { copy(diffSurroundEnabled = it) },
        getSp = { it.spkDiffSurroundEnabled }, setSp = { copy(spkDiffSurroundEnabled = it) }
    ),
    IntPref(
        hpPrefKey = "${ViperParams.PARAM_HP_DIFF_SURROUND_DELAY}",
        spkPrefKey = "spk_${ViperParams.PARAM_SPK_DIFF_SURROUND_DELAY}",
        jsonKey = "diffSurroundDelay", spkJsonKey = "spkDiffSurroundDelay",
        defaultValue = 4,
        getHp = { it.diffSurroundDelay }, setHp = { copy(diffSurroundDelay = it) },
        getSp = { it.spkDiffSurroundDelay }, setSp = { copy(spkDiffSurroundDelay = it) }
    ),

    // VHE (Headphone Surround)
    BoolPref(
        hpPrefKey = "${ViperParams.PARAM_HP_HEADPHONE_SURROUND_ENABLE}",
        spkPrefKey = "spk_${ViperParams.PARAM_SPK_HEADPHONE_SURROUND_ENABLE}",
        jsonKey = "vheEnabled", spkJsonKey = "spkVheEnabled",
        defaultValue = false,
        getHp = { it.vheEnabled }, setHp = { copy(vheEnabled = it) },
        getSp = { it.spkVheEnabled }, setSp = { copy(spkVheEnabled = it) }
    ),
    IntPref(
        hpPrefKey = "${ViperParams.PARAM_HP_HEADPHONE_SURROUND_STRENGTH}",
        spkPrefKey = "spk_${ViperParams.PARAM_SPK_HEADPHONE_SURROUND_STRENGTH}",
        jsonKey = "vheQuality", spkJsonKey = "spkVheQuality",
        defaultValue = 0,
        getHp = { it.vheQuality }, setHp = { copy(vheQuality = it) },
        getSp = { it.spkVheQuality }, setSp = { copy(spkVheQuality = it) }
    ),

    // Reverb
    BoolPref(
        hpPrefKey = "${ViperParams.PARAM_HP_REVERB_ENABLE}",
        spkPrefKey = "${ViperParams.PARAM_SPK_REVERB_ENABLE}",
        jsonKey = "reverbEnabled", spkJsonKey = "spkReverbEnabled",
        defaultValue = false,
        getHp = { it.reverbEnabled }, setHp = { copy(reverbEnabled = it) },
        getSp = { it.spkReverbEnabled }, setSp = { copy(spkReverbEnabled = it) }
    ),
    IntPref(
        hpPrefKey = "${ViperParams.PARAM_HP_REVERB_ROOM_SIZE}",
        spkPrefKey = "${ViperParams.PARAM_SPK_REVERB_ROOM_SIZE}",
        jsonKey = "reverbRoomSize", spkJsonKey = "spkReverbRoomSize",
        defaultValue = 0,
        getHp = { it.reverbRoomSize }, setHp = { copy(reverbRoomSize = it) },
        getSp = { it.spkReverbRoomSize }, setSp = { copy(spkReverbRoomSize = it) }
    ),
    IntPref(
        hpPrefKey = "${ViperParams.PARAM_HP_REVERB_ROOM_WIDTH}",
        spkPrefKey = "${ViperParams.PARAM_SPK_REVERB_ROOM_WIDTH}",
        jsonKey = "reverbWidth", spkJsonKey = "spkReverbWidth",
        defaultValue = 0,
        getHp = { it.reverbWidth }, setHp = { copy(reverbWidth = it) },
        getSp = { it.spkReverbWidth }, setSp = { copy(spkReverbWidth = it) }
    ),
    IntPref(
        hpPrefKey = "${ViperParams.PARAM_HP_REVERB_ROOM_DAMPENING}",
        spkPrefKey = "${ViperParams.PARAM_SPK_REVERB_ROOM_DAMPENING}",
        jsonKey = "reverbDampening", spkJsonKey = "spkReverbDampening",
        defaultValue = 0,
        getHp = { it.reverbDampening }, setHp = { copy(reverbDampening = it) },
        getSp = { it.spkReverbDampening }, setSp = { copy(spkReverbDampening = it) }
    ),
    IntPref(
        hpPrefKey = "${ViperParams.PARAM_HP_REVERB_ROOM_WET_SIGNAL}",
        spkPrefKey = "${ViperParams.PARAM_SPK_REVERB_ROOM_WET_SIGNAL}",
        jsonKey = "reverbWet", spkJsonKey = "spkReverbWet",
        defaultValue = 0,
        getHp = { it.reverbWet }, setHp = { copy(reverbWet = it) },
        getSp = { it.spkReverbWet }, setSp = { copy(spkReverbWet = it) }
    ),
    IntPref(
        hpPrefKey = "${ViperParams.PARAM_HP_REVERB_ROOM_DRY_SIGNAL}",
        spkPrefKey = "${ViperParams.PARAM_SPK_REVERB_ROOM_DRY_SIGNAL}",
        jsonKey = "reverbDry", spkJsonKey = "spkReverbDry",
        defaultValue = 50,
        getHp = { it.reverbDry }, setHp = { copy(reverbDry = it) },
        getSp = { it.spkReverbDry }, setSp = { copy(spkReverbDry = it) }
    ),

    // Dynamic System
    BoolPref(
        hpPrefKey = "${ViperParams.PARAM_HP_DYNAMIC_SYSTEM_ENABLE}",
        spkPrefKey = "spk_${ViperParams.PARAM_SPK_DYNAMIC_SYSTEM_ENABLE}",
        jsonKey = "dynamicSystemEnabled", spkJsonKey = "spkDynamicSystemEnabled",
        defaultValue = false,
        getHp = { it.dynamicSystemEnabled }, setHp = { copy(dynamicSystemEnabled = it) },
        getSp = { it.spkDynamicSystemEnabled }, setSp = { copy(spkDynamicSystemEnabled = it) }
    ),
    NullableLongPref(
        hpPrefKey = ViperRepository.PERF_DYNAMIC_SYS_PRESET_ID,
        spkPrefKey = "spk_${ViperRepository.PERF_DYNAMIC_SYS_PRESET_ID}",
        jsonKey = "dsPresetId", spkJsonKey = "spkDsPresetId",
        getHp = { it.dsPresetId }, setHp = { copy(dsPresetId = it) },
        getSp = { it.spkDsPresetId }, setSp = { copy(spkDsPresetId = it) }
    ),
    IntPref(
        hpPrefKey = ViperRepository.PERF_DYNAMIC_SYS_DEVICE,
        spkPrefKey = "spk_${ViperRepository.PERF_DYNAMIC_SYS_DEVICE}",
        jsonKey = "dynamicSystemDevice", spkJsonKey = "spkDynamicSystemDevice",
        defaultValue = 0,
        getHp = { it.dynamicSystemDevice }, setHp = { copy(dynamicSystemDevice = it) },
        getSp = { it.spkDynamicSystemDevice }, setSp = { copy(spkDynamicSystemDevice = it) }
    ),
    IntPref(
        hpPrefKey = "${ViperParams.PARAM_HP_DYNAMIC_SYSTEM_STRENGTH}",
        spkPrefKey = "spk_${ViperParams.PARAM_SPK_DYNAMIC_SYSTEM_STRENGTH}",
        jsonKey = "dynamicSystemStrength", spkJsonKey = "spkDynamicSystemStrength",
        defaultValue = 50,
        getHp = { it.dynamicSystemStrength }, setHp = { copy(dynamicSystemStrength = it) },
        getSp = { it.spkDynamicSystemStrength }, setSp = { copy(spkDynamicSystemStrength = it) }
    ),
    IntPref(
        hpPrefKey = "${ViperParams.PARAM_HP_DYNAMIC_SYSTEM_X_COEFFICIENTS}_low",
        spkPrefKey = "spk_${ViperParams.PARAM_SPK_DYNAMIC_SYSTEM_X_COEFFICIENTS}_low",
        jsonKey = "dsXLow", spkJsonKey = "spkDsXLow",
        defaultValue = 100,
        getHp = { it.dsXLow }, setHp = { copy(dsXLow = it) },
        getSp = { it.spkDsXLow }, setSp = { copy(spkDsXLow = it) }
    ),
    IntPref(
        hpPrefKey = "${ViperParams.PARAM_HP_DYNAMIC_SYSTEM_X_COEFFICIENTS}_high",
        spkPrefKey = "spk_${ViperParams.PARAM_SPK_DYNAMIC_SYSTEM_X_COEFFICIENTS}_high",
        jsonKey = "dsXHigh", spkJsonKey = "spkDsXHigh",
        defaultValue = 5600,
        getHp = { it.dsXHigh }, setHp = { copy(dsXHigh = it) },
        getSp = { it.spkDsXHigh }, setSp = { copy(spkDsXHigh = it) }
    ),
    IntPref(
        hpPrefKey = "${ViperParams.PARAM_HP_DYNAMIC_SYSTEM_Y_COEFFICIENTS}_low",
        spkPrefKey = "spk_${ViperParams.PARAM_SPK_DYNAMIC_SYSTEM_Y_COEFFICIENTS}_low",
        jsonKey = "dsYLow", spkJsonKey = "spkDsYLow",
        defaultValue = 40,
        getHp = { it.dsYLow }, setHp = { copy(dsYLow = it) },
        getSp = { it.spkDsYLow }, setSp = { copy(spkDsYLow = it) }
    ),
    IntPref(
        hpPrefKey = "${ViperParams.PARAM_HP_DYNAMIC_SYSTEM_Y_COEFFICIENTS}_high",
        spkPrefKey = "spk_${ViperParams.PARAM_SPK_DYNAMIC_SYSTEM_Y_COEFFICIENTS}_high",
        jsonKey = "dsYHigh", spkJsonKey = "spkDsYHigh",
        defaultValue = 80,
        getHp = { it.dsYHigh }, setHp = { copy(dsYHigh = it) },
        getSp = { it.spkDsYHigh }, setSp = { copy(spkDsYHigh = it) }
    ),
    IntPref(
        hpPrefKey = "${ViperParams.PARAM_HP_DYNAMIC_SYSTEM_SIDE_GAIN}_low",
        spkPrefKey = "spk_${ViperParams.PARAM_SPK_DYNAMIC_SYSTEM_SIDE_GAIN}_low",
        jsonKey = "dsSideGainLow", spkJsonKey = "spkDsSideGainLow",
        defaultValue = 50,
        getHp = { it.dsSideGainLow }, setHp = { copy(dsSideGainLow = it) },
        getSp = { it.spkDsSideGainLow }, setSp = { copy(spkDsSideGainLow = it) }
    ),
    IntPref(
        hpPrefKey = "${ViperParams.PARAM_HP_DYNAMIC_SYSTEM_SIDE_GAIN}_high",
        spkPrefKey = "spk_${ViperParams.PARAM_SPK_DYNAMIC_SYSTEM_SIDE_GAIN}_high",
        jsonKey = "dsSideGainHigh", spkJsonKey = "spkDsSideGainHigh",
        defaultValue = 50,
        getHp = { it.dsSideGainHigh }, setHp = { copy(dsSideGainHigh = it) },
        getSp = { it.spkDsSideGainHigh }, setSp = { copy(spkDsSideGainHigh = it) }
    ),

    // Tube Simulator
    BoolPref(
        hpPrefKey = "${ViperParams.PARAM_HP_TUBE_SIMULATOR_ENABLE}",
        spkPrefKey = "spk_${ViperParams.PARAM_SPK_TUBE_SIMULATOR_ENABLE}",
        jsonKey = "tubeSimulatorEnabled", spkJsonKey = "spkTubeSimulatorEnabled",
        defaultValue = false,
        getHp = { it.tubeSimulatorEnabled }, setHp = { copy(tubeSimulatorEnabled = it) },
        getSp = { it.spkTubeSimulatorEnabled }, setSp = { copy(spkTubeSimulatorEnabled = it) }
    ),

    // Bass
    BoolPref(
        hpPrefKey = "${ViperParams.PARAM_HP_BASS_ENABLE}",
        spkPrefKey = "spk_${ViperParams.PARAM_SPK_BASS_ENABLE}",
        jsonKey = "bassEnabled", spkJsonKey = "spkBassEnabled",
        defaultValue = false,
        getHp = { it.bassEnabled }, setHp = { copy(bassEnabled = it) },
        getSp = { it.spkBassEnabled }, setSp = { copy(spkBassEnabled = it) }
    ),
    IntPref(
        hpPrefKey = "${ViperParams.PARAM_HP_BASS_MODE}",
        spkPrefKey = "spk_${ViperParams.PARAM_SPK_BASS_MODE}",
        jsonKey = "bassMode", spkJsonKey = "spkBassMode",
        defaultValue = 0,
        getHp = { it.bassMode }, setHp = { copy(bassMode = it) },
        getSp = { it.spkBassMode }, setSp = { copy(spkBassMode = it) }
    ),
    IntPref(
        hpPrefKey = "${ViperParams.PARAM_HP_BASS_FREQUENCY}",
        spkPrefKey = "spk_${ViperParams.PARAM_SPK_BASS_FREQUENCY}",
        jsonKey = "bassFrequency", spkJsonKey = "spkBassFrequency",
        defaultValue = 55,
        getHp = { it.bassFrequency }, setHp = { copy(bassFrequency = it) },
        getSp = { it.spkBassFrequency }, setSp = { copy(spkBassFrequency = it) }
    ),
    IntPref(
        hpPrefKey = "${ViperParams.PARAM_HP_BASS_GAIN}",
        spkPrefKey = "spk_${ViperParams.PARAM_SPK_BASS_GAIN}",
        jsonKey = "bassGain", spkJsonKey = "spkBassGain",
        defaultValue = 0,
        getHp = { it.bassGain }, setHp = { copy(bassGain = it) },
        getSp = { it.spkBassGain }, setSp = { copy(spkBassGain = it) }
    ),
    BoolPref(
        hpPrefKey = "${ViperParams.PARAM_HP_BASS_ANTI_POP}",
        spkPrefKey = "spk_${ViperParams.PARAM_SPK_BASS_ANTI_POP}",
        jsonKey = "bassAntiPop", spkJsonKey = "spkBassAntiPop",
        defaultValue = true,
        getHp = { it.bassAntiPop }, setHp = { copy(bassAntiPop = it) },
        getSp = { it.spkBassAntiPop }, setSp = { copy(spkBassAntiPop = it) }
    ),

    // Bass Mono
    BoolPref(
        hpPrefKey = "${ViperParams.PARAM_HP_BASS_MONO_ENABLE}",
        spkPrefKey = "spk_${ViperParams.PARAM_SPK_BASS_MONO_ENABLE}",
        jsonKey = "bassMonoEnabled", spkJsonKey = "spkBassMonoEnabled",
        defaultValue = false,
        getHp = { it.bassMonoEnabled }, setHp = { copy(bassMonoEnabled = it) },
        getSp = { it.spkBassMonoEnabled }, setSp = { copy(spkBassMonoEnabled = it) }
    ),
    IntPref(
        hpPrefKey = "${ViperParams.PARAM_HP_BASS_MONO_MODE}",
        spkPrefKey = "spk_${ViperParams.PARAM_SPK_BASS_MONO_MODE}",
        jsonKey = "bassMonoMode", spkJsonKey = "spkBassMonoMode",
        defaultValue = 0,
        getHp = { it.bassMonoMode }, setHp = { copy(bassMonoMode = it) },
        getSp = { it.spkBassMonoMode }, setSp = { copy(spkBassMonoMode = it) }
    ),
    IntPref(
        hpPrefKey = "${ViperParams.PARAM_HP_BASS_MONO_FREQUENCY}",
        spkPrefKey = "spk_${ViperParams.PARAM_SPK_BASS_MONO_FREQUENCY}",
        jsonKey = "bassMonoFrequency", spkJsonKey = "spkBassMonoFrequency",
        defaultValue = 55,
        getHp = { it.bassMonoFrequency }, setHp = { copy(bassMonoFrequency = it) },
        getSp = { it.spkBassMonoFrequency }, setSp = { copy(spkBassMonoFrequency = it) }
    ),
    IntPref(
        hpPrefKey = "${ViperParams.PARAM_HP_BASS_MONO_GAIN}",
        spkPrefKey = "spk_${ViperParams.PARAM_SPK_BASS_MONO_GAIN}",
        jsonKey = "bassMonoGain", spkJsonKey = "spkBassMonoGain",
        defaultValue = 0,
        getHp = { it.bassMonoGain }, setHp = { copy(bassMonoGain = it) },
        getSp = { it.spkBassMonoGain }, setSp = { copy(spkBassMonoGain = it) }
    ),
    BoolPref(
        hpPrefKey = "${ViperParams.PARAM_HP_BASS_MONO_ANTI_POP}",
        spkPrefKey = "spk_${ViperParams.PARAM_SPK_BASS_MONO_ANTI_POP}",
        jsonKey = "bassMonoAntiPop", spkJsonKey = "spkBassMonoAntiPop",
        defaultValue = true,
        getHp = { it.bassMonoAntiPop }, setHp = { copy(bassMonoAntiPop = it) },
        getSp = { it.spkBassMonoAntiPop }, setSp = { copy(spkBassMonoAntiPop = it) }
    ),

    // Clarity
    BoolPref(
        hpPrefKey = "${ViperParams.PARAM_HP_CLARITY_ENABLE}",
        spkPrefKey = "spk_${ViperParams.PARAM_SPK_CLARITY_ENABLE}",
        jsonKey = "clarityEnabled", spkJsonKey = "spkClarityEnabled",
        defaultValue = false,
        getHp = { it.clarityEnabled }, setHp = { copy(clarityEnabled = it) },
        getSp = { it.spkClarityEnabled }, setSp = { copy(spkClarityEnabled = it) }
    ),
    IntPref(
        hpPrefKey = "${ViperParams.PARAM_HP_CLARITY_MODE}",
        spkPrefKey = "spk_${ViperParams.PARAM_SPK_CLARITY_MODE}",
        jsonKey = "clarityMode", spkJsonKey = "spkClarityMode",
        defaultValue = 0,
        getHp = { it.clarityMode }, setHp = { copy(clarityMode = it) },
        getSp = { it.spkClarityMode }, setSp = { copy(spkClarityMode = it) }
    ),
    IntPref(
        hpPrefKey = "${ViperParams.PARAM_HP_CLARITY_GAIN}",
        spkPrefKey = "spk_${ViperParams.PARAM_SPK_CLARITY_GAIN}",
        jsonKey = "clarityGain", spkJsonKey = "spkClarityGain",
        defaultValue = 1,
        getHp = { it.clarityGain }, setHp = { copy(clarityGain = it) },
        getSp = { it.spkClarityGain }, setSp = { copy(spkClarityGain = it) }
    ),

    // Cure
    BoolPref(
        hpPrefKey = "${ViperParams.PARAM_HP_CURE_ENABLE}",
        spkPrefKey = "spk_${ViperParams.PARAM_SPK_CURE_ENABLE}",
        jsonKey = "cureEnabled", spkJsonKey = "spkCureEnabled",
        defaultValue = false,
        getHp = { it.cureEnabled }, setHp = { copy(cureEnabled = it) },
        getSp = { it.spkCureEnabled }, setSp = { copy(spkCureEnabled = it) }
    ),
    IntPref(
        hpPrefKey = "${ViperParams.PARAM_HP_CURE_STRENGTH}",
        spkPrefKey = "spk_${ViperParams.PARAM_SPK_CURE_STRENGTH}",
        jsonKey = "cureStrength", spkJsonKey = "spkCureStrength",
        defaultValue = 0,
        getHp = { it.cureStrength }, setHp = { copy(cureStrength = it) },
        getSp = { it.spkCureStrength }, setSp = { copy(spkCureStrength = it) }
    ),

    // AnalogX
    BoolPref(
        hpPrefKey = "${ViperParams.PARAM_HP_ANALOGX_ENABLE}",
        spkPrefKey = "spk_${ViperParams.PARAM_SPK_ANALOGX_ENABLE}",
        jsonKey = "analogxEnabled", spkJsonKey = "spkAnalogxEnabled",
        defaultValue = false,
        getHp = { it.analogxEnabled }, setHp = { copy(analogxEnabled = it) },
        getSp = { it.spkAnalogxEnabled }, setSp = { copy(spkAnalogxEnabled = it) }
    ),
    IntPref(
        hpPrefKey = "${ViperParams.PARAM_HP_ANALOGX_MODE}",
        spkPrefKey = "spk_${ViperParams.PARAM_SPK_ANALOGX_MODE}",
        jsonKey = "analogxMode", spkJsonKey = "spkAnalogxMode",
        defaultValue = 0,
        getHp = { it.analogxMode }, setHp = { copy(analogxMode = it) },
        getSp = { it.spkAnalogxMode }, setSp = { copy(spkAnalogxMode = it) }
    ),

    // Speaker-only
    BoolPref(
        hpPrefKey = "spk_${ViperParams.PARAM_SPK_SPEAKER_CORRECTION_ENABLE}",
        spkPrefKey = "spk_${ViperParams.PARAM_SPK_SPEAKER_CORRECTION_ENABLE}",
        jsonKey = "speakerOptEnabled", spkJsonKey = "speakerOptEnabled",
        defaultValue = false,
        getHp = { it.speakerOptEnabled }, setHp = { copy(speakerOptEnabled = it) },
        getSp = { it.speakerOptEnabled }, setSp = { copy(speakerOptEnabled = it) }
    )
)

suspend fun loadEffectPrefs(
    repository: ViperRepository,
    isSpk: Boolean,
    state: MainUiState = MainUiState()
): MainUiState {
    var s = state
    for (pref in EFFECT_PREFS) {
        s = when (pref) {
            is IntPref -> {
                val key = if (isSpk) pref.spkPrefKey else pref.hpPrefKey
                val value = repository.getIntPreference(key, pref.defaultValue).first()
                if (isSpk) pref.setSpk(s, value) else pref.setHp(s, value)
            }

            is BoolPref -> {
                val key = if (isSpk) pref.spkPrefKey else pref.hpPrefKey
                val value = repository.getBooleanPreference(key, pref.defaultValue).first()
                if (isSpk) pref.setSpk(s, value) else pref.setHp(s, value)
            }

            is StringPref -> {
                val key = if (isSpk) pref.spkPrefKey else pref.hpPrefKey
                val value = repository.getStringPreference(key, pref.defaultValue).first()
                if (isSpk) pref.setSpk(s, value) else pref.setHp(s, value)
            }

            is NullableLongPref -> {
                val key = if (isSpk) pref.spkPrefKey else pref.hpPrefKey
                val raw = repository.getIntPreference(key, -1).first()
                val value = if (raw < 0) null else raw.toLong()
                if (isSpk) pref.setSpk(s, value) else pref.setHp(s, value)
            }
        }
    }
    return s
}

suspend fun saveEffectPrefs(
    repository: ViperRepository,
    state: MainUiState,
    isSpk: Boolean
) {
    for (pref in EFFECT_PREFS) {
        when (pref) {
            is IntPref -> {
                val key = if (isSpk) pref.spkPrefKey else pref.hpPrefKey
                val value = if (isSpk) pref.getSpk(state) else pref.getHp(state)
                repository.setIntPreference(key, value)
            }

            is BoolPref -> {
                val key = if (isSpk) pref.spkPrefKey else pref.hpPrefKey
                val value = if (isSpk) pref.getSpk(state) else pref.getHp(state)
                repository.setBooleanPreference(key, value)
            }

            is StringPref -> {
                val key = if (isSpk) pref.spkPrefKey else pref.hpPrefKey
                val value = if (isSpk) pref.getSpk(state) else pref.getHp(state)
                repository.setStringPreference(key, value)
            }

            is NullableLongPref -> {
                val key = if (isSpk) pref.spkPrefKey else pref.hpPrefKey
                val value = if (isSpk) pref.getSpk(state) else pref.getHp(state)
                repository.setIntPreference(key, value?.toInt() ?: -1)
            }
        }
    }
}

fun serializeEffectPrefs(state: MainUiState, isSpk: Boolean): JSONObject {
    val obj = JSONObject()
    for (pref in EFFECT_PREFS) {
        val jsonKey = if (isSpk) pref.spkJsonKey else pref.jsonKey
        when (pref) {
            is IntPref -> {
                val value = if (isSpk) pref.getSpk(state) else pref.getHp(state)
                obj.put(jsonKey, value)
            }

            is BoolPref -> {
                val value = if (isSpk) pref.getSpk(state) else pref.getHp(state)
                obj.put(jsonKey, value)
            }

            is StringPref -> {
                val value = if (isSpk) pref.getSpk(state) else pref.getHp(state)
                obj.put(jsonKey, value)
            }

            is NullableLongPref -> {
                val value = if (isSpk) pref.getSpk(state) else pref.getHp(state)
                obj.put(jsonKey, value ?: -1)
            }
        }
    }
    return obj
}

fun deserializeEffectPrefs(
    obj: JSONObject,
    state: MainUiState,
    isSpk: Boolean
): MainUiState {
    var s = state
    for (pref in EFFECT_PREFS) {
        val jsonKey = if (isSpk) pref.spkJsonKey else pref.jsonKey
        s = when (pref) {
            is IntPref -> {
                val fallback = if (isSpk) pref.getSpk(s) else pref.getHp(s)
                val value = obj.optInt(jsonKey, fallback)
                if (isSpk) pref.setSpk(s, value) else pref.setHp(s, value)
            }

            is BoolPref -> {
                val fallback = if (isSpk) pref.getSpk(s) else pref.getHp(s)
                val value = obj.optBoolean(jsonKey, fallback)
                if (isSpk) pref.setSpk(s, value) else pref.setHp(s, value)
            }

            is StringPref -> {
                val fallback = if (isSpk) pref.getSpk(s) else pref.getHp(s)
                val value = obj.optString(jsonKey, fallback)
                if (isSpk) pref.setSpk(s, value) else pref.setHp(s, value)
            }

            is NullableLongPref -> {
                val value = obj.optInt(jsonKey, -1).let { if (it < 0) null else it.toLong() }
                if (isSpk) pref.setSpk(s, value) else pref.setHp(s, value)
            }
        }
    }
    return s
}
