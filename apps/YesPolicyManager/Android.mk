# This work is authored by Two Six Labs employees. (C) 2020 Two Six Labs, LLC.  All rights reserved.

LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := $(call all-subdir-java-files)
LOCAL_STATIC_JAVA_LIBRARIES := \
    android-support-v7-appcompat

LOCAL_RESOURCE_DIR := \
        $(LOCAL_PATH)/res \
        prebuilts/sdk/current/support/v7/appcompat/res/

LOCAL_AAPT_FLAGS := \
        --auto-add-overlay \
        --extra-packages android.support.v7.appcompat:android.support.design

#LOCAL_DEX_PREOPT := false
LOCAL_PROGUARD_ENABLED := disabled

LOCAL_REQUIRED_MODULES := \
	com.twosixlabs.peandroid.privacymanager
LOCAL_JAVA_LIBRARIES := \
	com.twosixlabs.peandroid.privacymanager

LOCAL_PACKAGE_NAME := YesPolicyManager
LOCAL_PRIVATE_PLATFORM_APIS := true
LOCAL_MODULE_OWNER := twosixlabs
include $(BUILD_PACKAGE)
