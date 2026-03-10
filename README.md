# ViPER4Android

Material Design 3 UI for ViPER4Android FX. This app provides the full feature set of ViPER4Android FX with a modern interface

## Features

### Audio Effects

- [x] Playback Gain Control
- [x] FET Compressor
- [x] ViPER-DDC
- [x] Spectrum Extension
- [x] FIR Equalizer
- [x] Convolver
- [x] Field Surround
- [x] Differential Surround
- [x] Headphone Surround+
- [x] Reverberation
- [x] Dynamic System
- [x] Tube Simulator
- [x] ViPER Bass
- [x] ViPER Clarity
- [x] Auditory System Protection
- [x] AnalogX
- [x] Speaker Optimization

---

### App Features

- [x] Material Design 3 UI
- [x] Support for both AIDL and non-AIDL devices
- [x] Device auto-detection (Speaker & Headphone)
- [x] In app log debugging (tap the `Driver Version` 7 times in the `Settings`)

## Installation

1. Download the latest APK from the [releases](https://github.com/likelikeslike/ViPER4Android/releases)
2. Install the APK on your Android device
3. Flash the module from this [repo](https://github.com/likelikeslike/ViPERFX_RE)
4. Reboot your device
5. Open the app and enable `AIDL mode` in the settings if your device use AIDL for HALs
6. Enjoy

## Important Notes

- This app may requires root access if:
  - You are on AIDL mode and the driver is not properly installed or configured during the module installation (to create shm for AIDL driver)
  - In app log debugging (to `logcat` driver's log)
- Please make sure the source of any modified APK is trustworthy to avoid any security risks

## Contributing

Contributions are welcome! Please open an issue or submit a pull request if you have any ideas or improvements for the app.

### Localization

If you want to help with localization, please follow [this guide](app/res-template/values-template/strings.xml)
