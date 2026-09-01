cmake_minimum_required(VERSION 3.22)

project(nexoai_native LANGUAGES C CXX)

set(CMAKE_C_STANDARD 17)
set(CMAKE_C_STANDARD_REQUIRED ON)
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

set(LLAMA_ROOT "${CMAKE_CURRENT_SOURCE_DIR}/../../../../../../engine/llama.cpp")
set(PREBUILT_DIR "${LLAMA_ROOT}/prebuilt/arm64-v8a")

find_library(log-lib log)

add_library(llama STATIC IMPORTED)
set_target_properties(llama PROPERTIES
    IMPORTED_LOCATION "${PREBUILT_DIR}/libllama.a"
    IMPORTED_LINK_INTERFACE_LANGUAGES "CXX"
)

add_library(llama_common STATIC IMPORTED)
set_target_properties(llama_common PROPERTIES
    IMPORTED_LOCATION "${PREBUILT_DIR}/libllama-common.a"
    IMPORTED_LINK_INTERFACE_LANGUAGES "CXX"
)

add_library(llama_common_base STATIC IMPORTED)
set_target_properties(llama_common_base PROPERTIES
    IMPORTED_LOCATION "${PREBUILT_DIR}/libllama-common-base.a"
    IMPORTED_LINK_INTERFACE_LANGUAGES "CXX"
)

add_library(ggml STATIC IMPORTED)
set_target_properties(ggml PROPERTIES
    IMPORTED_LOCATION "${PREBUILT_DIR}/libggml.a"
    IMPORTED_LINK_INTERFACE_LANGUAGES "C"
)

add_library(ggml_cpu STATIC IMPORTED)
set_target_properties(ggml_cpu PROPERTIES
    IMPORTED_LOCATION "${PREBUILT_DIR}/libggml-cpu.a"
    IMPORTED_LINK_INTERFACE_LANGUAGES "C"
)

add_library(ggml_base STATIC IMPORTED)
set_target_properties(ggml_base PROPERTIES
    IMPORTED_LOCATION "${PREBUILT_DIR}/libggml-base.a"
    IMPORTED_LINK_INTERFACE_LANGUAGES "C"
)

add_library(nexoai_native SHARED
    native-lib.cpp
    llama_bridge.cpp
)

target_include_directories(nexoai_native PRIVATE
    ${LLAMA_ROOT}/include
    ${LLAMA_ROOT}/ggml/include
    ${CMAKE_CURRENT_SOURCE_DIR}
)

target_link_libraries(nexoai_native
    PRIVATE
    -Wl,--start-group
    llama
    llama_common
    llama_common_base
    ggml_cpu
    ggml
    ggml_base
    -Wl,--end-group
    ${log-lib}
    android
)

set_target_properties(nexoai_native PROPERTIES
    OUTPUT_NAME "nexoai_native"
)
