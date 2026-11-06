# meta-eezybot-distro

Yocto/OpenEmbedded distribution and platform integration layer for EezyBot on NVIDIA Jetson (`jetson-orin-nano-devkit-nvme`).

## Contents

- **Distro Configuration (`conf/distro/eezybot.conf`)**: Defines the `eezybot` distribution (`systemd`, `DISTRO_FEATURES`, kernel module autoloading, and `meta-ros` build configuration).
- **Images (`recipes-core/images/`)**:
  - `eezybot-image.bb`: Minimal headless production image.
  - `eezybot-image-dev.bb`: Development image with debugging tools and web dashboard.
- **Packagegroups (`recipes-core/packagegroups/`)**:
  - `packagegroup-eezybot-core.bb`: Core ROS 2 runtime, EezyBotArm bringup, NIC firmware/drivers, CUDA/TensorRT runtime, and camera drivers.
  - `packagegroup-eezybot-dev.bb`: Developer and diagnostic tooling, ONNX Runtime, and NiceGUI web dashboard.
- **Connectivity (`recipes-connectivity/wpa-supplicant/`)**: Auto-enables `wpa_supplicant@wlan0.service`.
- **Integration `bbappend`s (`recipes-bbappends/`)**:
  - **`linux-noble-nvidia-tegra`**: Enables `CONFIG_KPROBES` required by `lttng-modules` in the ROS 2 dependency tree.
  - **`cv-bridge`**: Inherits `cuda`, sets `CUDAToolkit_ROOT` for cross-compilation against OpenCV with CUDA enabled on Tegra, and injects `CUDA::cudart` target definitions into `cv_bridgeConfig.cmake`.
  - **`moveit-py`**: Patches `CMakeLists.txt` (`0001-find-bullet-before-moveit-core.patch`) so `Bullet` is found before `moveit_core` targets, strips buildpath references, and fixes `pybind11` cross-compiled shared library suffixes.
  - **`v4l2-camera`**: Removes the obsolete `fix_warning.patch` from `meta-ros2-jazzy` which fails to apply on `v4l2-camera` 0.7.3+.
  - **`python3-tinycss2`**: Adds missing runtime dependency `python3-webencodings`.
