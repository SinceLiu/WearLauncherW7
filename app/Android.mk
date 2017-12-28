#
# Copyright (C) 2013 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

LOCAL_PATH := $(call my-dir)

#
# Build app code.
#
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_STATIC_JAVA_LIBRARIES := \
    android-support-v4 \
    android-support-v7-appcompat \
    android-support-v7-recyclerview \
    android-support-v7-palette \
    android-support-v13
    
LOCAL_JAVA_LIBRARIES := telephony-common
LOCAL_JAVA_LIBRARIES += ims-common

LOCAL_SRC_FILES := \
    $(call all-java-files-under, src) \
    $(call all-renderscript-files-under, src)

LOCAL_RESOURCE_DIR := \
    $(LOCAL_PATH)/res \
    frameworks/support/v7/recyclerview/res \
    frameworks/support/v7/cardview/res

LOCAL_AAPT_FLAGS := \
    --auto-add-overlay \
    --extra-packages com.android.keyguard \
    

LOCAL_CERTIFICATE := platform
#LOCAL_SDK_VERSION := current
LOCAL_MIN_SDK_VERSION := 21
LOCAL_PACKAGE_NAME := WearLauncher
LOCAL_PRIVILEGED_MODULE := true


LOCAL_OVERRIDES_PACKAGES += Launcher3

ifeq ($(strip $(CENON_SIMPLIFY_VERSION)), yes)
LOCAL_OVERRIDES_PACKAGES += \
    AtciService \
    DataTransfer \
    AutoDialer \
    BasicDreams \
    BSPTelephonyDevTool \
    BookmarkProvider \
    BluetoothMidiService \
    BtTool \
    BackupRestoreConfirmation \
    CallLogBackup \
    WallpaperCropper \
    WallpaperBackup \
    PhotoTable \
    PicoTts \
    PrintSpooler \
    LiveWallpapers \
    LiveWallpapersPicker \
    MagicSmokeWallpapers \
    VisualizationWallpapers \
    Galaxy4 \
    HoloSpiralWallpaper \
    NoiseField \
    PhaseBeam \
    YahooNewsWidget \
    CarrierConfig \
    MtkQuickSearchBox \
    QuickSearchBox \
    MtkFloatMenu \
    TouchPal \
    Development \
    MultiCoreObserver \
    CtsShimPrebuilt \
    EasterEgg \
    DuraSpeed \
    ExactCalculator \
    PrintRecommendationService \
    CallLogBackup \
    CtsShimPrivPrebuilt \
    SimRecoveryTestTool \
    FileManagerTest \
    Stk \
    Stk1

#LOCAL_PROGUARD_FLAG_FILES := proguard.flags

## set MTK_BASIC_PACKAGE

LOCAL_OVERRIDES_PACKAGES += \
    Email \
    Exchange2 \
    Calculator \
    MtkBrowser \
    MtkCalendar \
    Music \
    MusicFX \
    FMRadio \
    DeskClock \
    EmergencyInfo \
    SimProcessor \
    WiFiTest \
    SensorHub \
    MTKThermalManager \
    QuickSearchBox \
    FileManager \
    Contacts \
    messaging \
    MtkMms \
    BatteryWarning \
    SchedulePowerOnOff \
    DownloadProviderUi \
    SoundRecorder

LOCAL_OVERRIDES_PACKAGES += LatinIME

endif

include $(BUILD_PACKAGE)

#include $(BUILD_HOST_JAVA_LIBRARY)

# ==================================================
include $(call all-makefiles-under,$(LOCAL_PATH))

