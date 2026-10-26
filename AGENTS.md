# EezyBot Yocto Guidelines

## Overview
This repository contains the Yocto/OpenEmbedded project for building an embedded Linux distribution (`eezybot`) for NVIDIA Jetson (`jetson-orin-nano-devkit-nvme`) with integrated ROS 2 Jazzy and EezyBotArm services.

## Build & Environment Rules
* **Tooling:** All builds and BitBake operations run in Docker via `kas-container`.
* **CRITICAL RULE:** **Never** run `bitbake` or setup scripts directly on the host machine.
* Use the provided `./build.sh` script to drive `kas-container`.

## Key Commands
* **Build Production Image (`eezybot-image`):**
  ```bash
  ./build.sh prod
  ```
* **Build Development Image (`eezybot-image-dev`):**
  ```bash
  ./build.sh dev
  ```
* **Interactive BitBake / kas shell:**
  ```bash
  ./build.sh shell
  # Inside the kas shell, you can run bitbake commands directly:
  # bitbake eezybotarm-control
  # bitbake -c clean eezybotarm-hardware
  ```
* **Flash target Jetson device (in recovery mode):**
  ```bash
  ./flash-jetson.sh dev    # or: ./flash-jetson.sh prod
  ```
* **Compile TensorRT engine remotely on a running Jetson:**
  ```bash
  ./compile-engine-remote.sh [user@]<jetson-host>
  ```

## Layer Architecture & Recipes
* **`meta-eezybot/`**: Custom project layer.
  * **`recipes-ros2/eezybotarm/`**: Recipes packaging the ROS 2 workspace:
    * `eezybotarm-app.inc`: Defines git repository URL and commit revision (`EEZYBOT_ROS_APP_GIT_REV`).
    * `eezybotarm-bringup`: Systemd unit (`eezybotarm.service`) and first-boot TensorRT engine builder (`eezybotarm-ensure-engine.sh`).
    * `eezybotarm-dashboard`: NiceGUI web dashboard node and systemd unit (`eezybot-dashboard.service`, enabled in dev image).
    * `eezybotarm-control`, `eezybotarm-description`, `eezybotarm-hardware`, `eezybotarm-interfaces`, `eezybotarm-moveit-config`, `eezybotarm-vision`.
  * **`recipes-model/`**:
    * `cube-detection-model`: Deploys `model.onnx` (and optional precompiled `model.engine`) into `/usr/share/models/`.
  * **`recipes-devtools/python/`**: Custom Python recipes and bbappends for AI & dashboard (`python3-cuda-python`, `python3-onnxruntime`, `python3-nicegui`, `python3-vbuild`, `python3-markdown2`, `python3-tinycss2`).
  * **`recipes-core/images/`**: `eezybot-image.bb` (production) and `eezybot-image-dev.bb` (dev tooling).
  * **`recipes-core/packagegroups/`**: `packagegroup-eezybot-core.bb` and `packagegroup-eezybot-dev.bb`.
  * **`recipes-bbappends/linux/`**: Kernel configuration fragment (`linux-noble-nvidia-tegra`).
  * **`conf/credentials.inc`**: WiFi SSIDs and SSH public keys (gitignored).
* **`meta-ros-tegra-fixes/`**: Upstream integration fixes and workarounds for `meta-ros` and `meta-tegra` (`cv-bridge`, `moveit-py`, `v4l2-camera`).

## Relationship with ROS 2 Workspace (`../eeazybot`)
* ROS 2 development is done in `../eeazybot`.
* When changes in `eeazybot` are ready for target deployment:
  1. Commit and push changes to the remote git branch.
  2. Update `EEZYBOT_ROS_APP_GIT_REV` in `meta-eezybot/recipes-ros2/eezybotarm/eezybotarm-app.inc` to match the target commit hash.
  3. Rebuild the image with `./build.sh dev` (or rebuild the specific recipe via `./build.sh shell`).

## Ignored & Heavy Directories
* **Do NOT search or parse inside:** `build/tmp/`, `build/sstate-cache/`, `build/downloads/`, `build/cache/`, `flash/`.
