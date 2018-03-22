
if (FFMPEG_LIBRARIES AND FFMPEG_INCLUDE_DIR)
    set(FFMPEG_FOUND TRUE)
else (FFMPEG_LIBRARIES AND FFMPEG_INCLUDE_DIR)
    # use pkg-config to get the directories and then use these values
    # in the FIND_PATH() and FIND_LIBRARY() calls
    find_package(PkgConfig)

    find_path(FFMPEG_AVCODEC_INCLUDE_DIR
        NAMES libavcodec/avcodec.h
        PATHS ${_FFMPEG_AVCODEC_INCLUDE_DIRS} /usr/include /usr/local/include /opt/local/include /sw/include
        PATH_SUFFIXES ffmpeg libav
    )

    MACRO(FFMPEG_FIND varname libname)
        if (PKG_CONFIG_FOUND)
            pkg_check_modules(${varname} REQUIRED lib${libname})
        endif(PKG_CONFIG_FOUND)

#        find_library(${varname}
#            NAMES ${libname}
#            PATHS ${_${pkg_check_modules}_LIBRARY_DIRS} /usr/lib /usr/local/lib /opt/local/lib /sw/lib
#        )
        
    ENDMACRO(FFMPEG_FIND)

    FFMPEG_FIND(FFMPEG_LIBAVFORMAT avformat)
    FFMPEG_FIND(FFMPEG_LIBAVDEVICE avdevice)
    FFMPEG_FIND(FFMPEG_LIBAVDILTER avfilter)
    FFMPEG_FIND(FFMPEG_LIBAVCODEC  avcodec )
    FFMPEG_FIND(FFMPEG_LIBAVUTIL   avutil  )
    FFMPEG_FIND(FFMPEG_LIBSWSCALE  swscale )

    if( FFMPEG_LIBAVFORMAT_FOUND AND
        FFMPEG_LIBAVDEVICE_FOUND AND
        FFMPEG_LIBAVDILTER_FOUND AND
        FFMPEG_LIBAVCODEC_FOUND  AND
        FFMPEG_LIBAVUTIL_FOUND   AND
        FFMPEG_LIBSWSCALE_FOUND
        )
        set(FFMPEG_FOUND TRUE)
    endif()

    if(FFMPEG_FOUND)
        set(FFMPEG_LIBRARIES
            ${FFMPEG_LIBSWSCALE_LIBRARIES}
            ${FFMPEG_LIBAVDEVICE_LIBRARIES}
#            ${FFMPEG_LIBAVDILTER_LIBRARIES}
            ${FFMPEG_LIBAVCODEC_LIBRARIES}
            ${FFMPEG_LIBAVFORMAT_LIBRARIES}
            ${FFMPEG_LIBAVUTIL_LIBRARIES}
        )
        set(FFMPEG_INCLUDE_DIR ${FFMPEG_AVCODEC_INCLUDE_DIR})
        message("FOUND ALL FFMPEG")
    else()
    endif(FFMPEG_FOUND)
endif()
