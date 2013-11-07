LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_JAVA_LIBRARIES := framework
LOCAL_CERTIFICATE := platform

LOCAL_SRC_FILES := $(call all-java-files-under, src) \
	src/com/android/music/IMediaPlaybackService.aidl

LOCAL_STATIC_JAVA_LIBRARIES := \
				android-common \
				android-support-v13 \
        android-support-v4 
        
LOCAL_PACKAGE_NAME := scene
 

include $(BUILD_PACKAGE)

include $(call all-makefiles-under,$(LOCAL_PATH))
