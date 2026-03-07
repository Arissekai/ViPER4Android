# ViPER4Android APK Build System

GRADLE  := ./gradlew
OUT_DIR := app/build/outputs/apk

.PHONY: debug release clean lint check help

debug:
	$(GRADLE) assembleDebug
	@echo "APK: $(OUT_DIR)/debug/app-debug.apk"

release:
	$(GRADLE) assembleRelease
	@echo "APK: $(OUT_DIR)/release/app-release.apk"

lint:
	$(GRADLE) lint

check:
	$(GRADLE) check

clean:
	$(GRADLE) clean

help:
	@echo "ViPER4Android APK Build System"
	@echo ""
	@echo "Prerequisites:"
	@echo "  - Android SDK (set ANDROID_HOME)"
	@echo "  - JDK 17+"
	@echo ""
	@echo "Targets:"
	@echo "  make release   Build release APK (default)"
	@echo "  make debug     Build debug APK"
	@echo "  make lint      Run Android lint"
	@echo "  make check     Run all checks"
	@echo "  make clean     Remove build artifacts"
