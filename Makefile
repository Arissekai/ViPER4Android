# ViPER4Android APK Build System

GRADLE  := ./gradlew
OUT_DIR := app/build/outputs/apk
APK_DIR := apk
VERSION ?= $(shell grep versionName app/build.gradle.kts | awk -F'"' '{print $$2}')

.PHONY: debug release clean lint check help

debug:
	$(GRADLE) assembleDebug
	@mkdir -p $(APK_DIR)
	@cp $(OUT_DIR)/debug/app-debug.apk $(APK_DIR)/ViPER4Android-$(VERSION)-debug.apk
	@echo "APK: $(APK_DIR)/ViPER4Android-$(VERSION)-debug.apk"

release:
	$(GRADLE) assembleRelease
	@mkdir -p $(APK_DIR)
	@cp $(OUT_DIR)/release/app-release.apk $(APK_DIR)/ViPER4Android-$(VERSION).apk
	@echo "APK: $(APK_DIR)/ViPER4Android-$(VERSION).apk"

lint:
	$(GRADLE) lint

check:
	$(GRADLE) check

clean:
	$(GRADLE) clean
	@rm -rf $(APK_DIR)

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
