# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# 1. 定义so库和头文件所在目录，方面后面使用
set(opencv_lib_dir ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI})
set(opencv_head_dir ${CMAKE_SOURCE_DIR})

# 2. 添加头文件目录
include_directories(include)

# 3. 添加相关的so库
add_library(opencv-lib SHARED IMPORTED)
set_target_properties(opencv-lib
        PROPERTIES IMPORTED_LOCATION
        ${opencv_lib_dir}/libopencv_java3.so)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library(native-lib
        SHARED
        jni_part.cpp)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.
target_link_libraries( # Specifies the target library.

        native-lib
        opencv-lib
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})