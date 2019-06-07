# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        native-lib

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        native-lib.cpp)

add_library( ffmpeglinker
        SHARED
        cmdutils.c
        ffmpeg.c
        ffmpeg_filter.c
        ffmpeg_opt.c
        ffmpeg-linker.c)

#add_library( avutil-56
add_library( avutil-55
        SHARED
        IMPORTED )
set_target_properties( avutil-55
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/ffmpeg/armeabi-v7a/libavutil-55.so)

#add_library( avcodec-58
add_library( avcodec-57
        SHARED
        IMPORTED )
set_target_properties( avcodec-57
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/ffmpeg/armeabi-v7a/libavcodec-57.so)

#add_library( avformat-58
add_library( avformat-57
        SHARED
        IMPORTED )
set_target_properties( avformat-57
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/ffmpeg/armeabi-v7a/libavformat-57.so)

#add_library( avdevice-58
#        SHARED
#        IMPORTED )
#set_target_properties( avdevice-58
#        PROPERTIES IMPORTED_LOCATION
#        ${CMAKE_SOURCE_DIR}/ffmpeg/armeabi-v7a/libavdevice-58.so)

#add_library( avfilter-7
add_library( avfilter-6
        SHARED
        IMPORTED )
set_target_properties( avfilter-6
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/ffmpeg/armeabi-v7a/libavfilter-6.so)

#add_library( swscale-5
add_library( swscale-4
        SHARED
        IMPORTED )
set_target_properties( swscale-4
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/ffmpeg/armeabi-v7a/libswscale-4.so)

#add_library( swresample-3
add_library( swresample-2
        SHARED
        IMPORTED )
set_target_properties( swresample-2
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/ffmpeg/armeabi-v7a/libswresample-2.so)

#add_library( postproc-5
#        SHARED
#        IMPORTED )
#set_target_properties( postproc-5
#        PROPERTIES IMPORTED_LOCATION
#        ${CMAKE_SOURCE_DIR}/ffmpeg/armeabi-v7a/libpostproc-5.so)

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

include_directories(src/main/jni/ffmpeg/include)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")

target_link_libraries( # Specifies the target library.
        ffmpeglinker
        avutil-55
        avcodec-57
        avformat-57
        #        avdevice-58
        avfilter-6
        swscale-4
        swresample-2
        #        postproc-5

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})
