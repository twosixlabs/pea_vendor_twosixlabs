# This work is authored by TwoSix Labs employees.  (C) 2020 TwoSix Labs, LLC.  All rights reserved.

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := PrivacyCheckup
LOCAL_SRC_FILES := privacy-checkup.apk
LOCAL_MODULE_SUFFIX := .apk
LOCAL_MODULE_CLASS := APPS
LOCAL_CERTIFICATE := platform
LOCAL_MODULE_PATH := $(TARGET_OUT)/app
LOCAL_MODULE_OWNER := twosixlabs
include $(BUILD_PREBUILT)
