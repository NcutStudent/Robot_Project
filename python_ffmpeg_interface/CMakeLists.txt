cmake_minimum_required(VERSION 3.5)

project(face_project)

set(CMAKE_MODULE_PATH ${CMAKE_CURRENT_SOURCE_DIR}/CMakeModules)

include_directories("./")

include(dlib/cmake)

#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -shared -fPIC")

set(Boost_USE_STATIC_LIBS OFF) 
set(Boost_USE_MULTITHREADED ON)  
set(Boost_USE_STATIC_RUNTIME OFF)

find_package(Boost REQUIRED python27 numpy27 system)
find_package(OpenCV)
find_package(FFmpeg)
find_package(Threads)
find_package(PythonLibs 2.7 REQUIRED)

macro(configure_files srcDir destDir)
    message(STATUS "Configuring directory ${destDir}")
    make_directory(${destDir})

    file(GLOB templateFiles RELATIVE ${srcDir} ${srcDir}/*)
    foreach(templateFile ${templateFiles})
        set(srcTemplatePath ${srcDir}/${templateFile})
        if(NOT IS_DIRECTORY ${srcTemplatePath})
            message(STATUS "Configuring file ${templateFile}")
            configure_file(
                    ${srcTemplatePath}
                    ${destDir}/${templateFile}
                    @ONLY)
        endif(NOT IS_DIRECTORY ${srcTemplatePath})
    endforeach(templateFile)
endmacro(configure_files)

if((Boost_FOUND) AND (OpenCV_FOUND) AND (FFMPEG_FOUND) AND (PYTHONLIBS_FOUND))

    include_directories(${FFMPEG_INCLUDE_DIR})
    include_directories(${Boost_INCLUDE_DIRS})
    include_directories(${OpenCV_INCLUDE_DIRS})
    include_directories(${PYTHON_INCLUDE_DIR})

#    configure_files(python test)
    configure_file(python/utils/tcp_client.py utils/tcp_client.py COPYONLY)
    configure_file(python/utils/constants.py utils/constants.py COPYONLY)
    configure_file(python/utils/__init__.py utils/__init__.py COPYONLY)
    configure_file(python/utils/util.py utils/util.py COPYONLY)

    configure_file(python/testInterface.py testPythonInterface.py COPYONLY)
    configure_file(python/sliding_window.py sliding_window.py COPYONLY)
    configure_file(python/detectedFace.py detectedFace.py COPYONLY)
    configure_file(python/wire_socket.py wire_socket.py COPYONLY)
    configure_file(python/camera.py camera.py COPYONLY)
    configure_file(python/audio.py audio.py COPYONLY)
    configure_file(python/gui.py gui.py COPYONLY)
    
    add_library(FaceDetected SHARED include/FaceDetected.h src/FaceDetected.cpp)
    target_link_libraries(FaceDetected dlib::dlib_shared ${OpenCV_LIBS})

    add_library(FaceDetected_Python SHARED include/FaceDetected_python.h src/FaceDetected_python.cpp)
    target_link_libraries(FaceDetected_Python dlib::dlib_shared ${OpenCV_LIBS})

    add_library(H264Decoder SHARED src/H264Decoder.cpp include/H264Decoder.h)
    target_link_libraries(H264Decoder ${FFMPEG_LIBRARIES})

    add_library(H264Encoder SHARED src/H264Encoder.cpp include/H264Encoder.h)
    target_link_libraries(H264Encoder ${OpenCV_LIBS})
    target_link_libraries(H264Encoder ${FFMPEG_LIBRARIES})

    add_library(H264ImgProcess SHARED include/H264ImgProcess.h src/H264ImgProcess.cpp)
    target_link_libraries(H264ImgProcess dlib::dlib_shared ${OpenCV_LIBS} H264Encoder H264Decoder)

    add_library(boost_test SHARED boost_test.cpp)
    target_link_libraries(boost_test ${PYTHON_LIBRARY} ${Boost_LIBRARIES} Threads::Threads)

    message("${PYTHON_LIBRARY} ${Boost_LIBRARIES}")

    add_executable(testFfmpeg testFfmpeg.cpp)
    target_link_libraries(testFfmpeg ${FFMPEG_LIBRARIES} ${OpenCV_LIBS} Threads::Threads H264Decoder H264Encoder)
    
    add_library(python_interface SHARED src/python_interface.cpp)
    target_link_libraries(python_interface ${FFMPEG_LIBRARIES} ${OpenCV_LIBS} Threads::Threads ${Boost_LIBRARIES} ${PYTHON_LIBRARY} H264ImgProcess FaceDetected_Python)

#    add_executable(progname file1.cxx file2.cxx) 
#    target_link_libraries(progname ${Boost_LIBRARIES})

else()
    message("boost not found")
endif()
