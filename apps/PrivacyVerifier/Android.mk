# This work is authored by Two Six Labs employees. (C) 2020 Two Six Labs, LLC.  All rights reserved.


LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := $(call all-subdir-java-files)

LOCAL_RESOURCE_DIR := \
        $(LOCAL_PATH)/res 

LOCAL_JAVA_LIBRARIES := com.twosixlabs.peandroid.pal

LOCAL_PROGUARD_ENABLED := disabled
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true
LOCAL_PACKAGE_NAME := PrivacyVerifier
LOCAL_PRIVATE_PLATFORM_APIS := true
LOCAL_MODULE_OWNER := twosixlabs
include $(BUILD_PACKAGE)
