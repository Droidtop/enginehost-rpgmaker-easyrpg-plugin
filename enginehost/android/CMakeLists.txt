# EasyRPG Player in Enginehost's sandbox: the Player's own libretro core
# (PLAYER_TARGET_PLATFORM=libretro, src/platform/libretro), built static and
# linked with Enginehost's libretro frontend and file layer into the one
# library the isolated runtime loads (plugin-native/, copied verbatim from
# Droidtop/enginehost). EasyRpgPlugin.java is the entry point.
#
# Configured per ABI by .github/workflows/android-plugin.yml with the NDK and
# the dependency toolchain EasyRPG's buildscripts make
# (PLAYER_ANDROID_TOOLCHAIN_PATH); the C++ runtime is linked in statically
# (ANDROID_STL=c++_static) so nothing beside the library has to be loaded.

cmake_minimum_required(VERSION 3.22)
project(easyrpg_enginehost C CXX)

set(PLAYER_TARGET_PLATFORM "libretro" CACHE STRING "" FORCE)
set(BUILD_SHARED_LIBS OFF CACHE BOOL "" FORCE)
set(PLAYER_ENABLE_TESTS OFF CACHE BOOL "" FORCE)
set(PLAYER_GRADLE_BUILD ON CACHE BOOL "" FORCE)
add_subdirectory(${CMAKE_CURRENT_SOURCE_DIR}/../.. easyrpg)

set(PLUGIN_NATIVE_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../plugin-native)
add_library(easyrpg_enginehost SHARED
    ${PLUGIN_NATIVE_DIR}/libretro/enginehost_libretro.c
    ${PLUGIN_NATIVE_DIR}/enginehost_audio_ring.c)
target_include_directories(easyrpg_enginehost PRIVATE ${PLUGIN_NATIVE_DIR} ${PLUGIN_NATIVE_DIR}/libretro)
target_link_libraries(easyrpg_enginehost PRIVATE easyrpg_libretro android log)

include(${PLUGIN_NATIVE_DIR}/enginehost_vfs.cmake)
enginehost_vfs(easyrpg_enginehost)
