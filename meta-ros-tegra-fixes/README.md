# meta-ros-tegra-fixes

Yocto/OpenEmbedded layer containing build fixes and workarounds (`bbappend`s) for integrating upstream `meta-ros` (ROS 2 Jazzy) and `meta-tegra` recipes.

## Included Fixes

- **`cv-bridge` (`recipes-bbappends/cv-bridge/cv-bridge_%.bbappend`)**:
  Inherits `cuda`, sets `CUDAToolkit_ROOT` for cross-compilation against OpenCV with CUDA enabled on Tegra, and injects `CUDA::cudart` target definitions into the installed `cv_bridgeConfig.cmake`.
- **`moveit-py` (`recipes-bbappends/moveit-py/moveit-py_%.bbappend`)**:
  - Patches `CMakeLists.txt` (`0001-find-bullet-before-moveit-core.patch`) so `Bullet` is found before `moveit_core` targets are imported.
  - Strips buildpath references (`RECIPE_SYSROOT`) from exported CMake targets and fixes `pybind11` cross-compiled shared library suffixes (`x86_64-linux-gnu` -> `aarch64-linux-gnu`).
  - Handles `already-stripped` and `dev-so` QA checks.
- **`v4l2-camera` (`recipes-bbappends/v4l2-camera/v4l2-camera_%.bbappend`)**:
  Removes the obsolete `fix_warning.patch` from `meta-ros2-jazzy` which fails to apply on `v4l2-camera` 0.7.3+.
