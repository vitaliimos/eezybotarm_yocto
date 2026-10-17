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

## Layer Architecture & Recipes
* **`meta-eezybot/`**: Custom project layer.
  * **`recipes-ros2/eezybotarm/`**: Recipes packaging the ROS 2 workspace:
    * `eezybotarm-app.inc`: Defines git repository URL and commit revision (`EEZYBOT_ROS_APP_GIT_REV`).
    * `eezybotarm-bringup`: Systemd unit and launch scripts for automated startup.
    * `eezybotarm-control`, `eezybotarm-description`, `eezybotarm-hardware`, `eezybotarm-interfaces`, `eezybotarm-moveit-config`.
  * **`recipes-core/images/`**: `eezybot-image.bb` (production) and `eezybot-image-dev.bb` (dev tooling).
  * **`recipes-bbappends/`**: Kernel and package append configurations (e.g., `linux-noble-nvidia-tegra`, `moveit-py`).
  * **`conf/credentials.inc`**: WiFi SSIDs and SSH public keys (gitignored).

## Relationship with ROS 2 Workspace (`../eeazybot`)
* ROS 2 development is done in `../eeazybot`.
* When changes in `eeazybot` are ready for target deployment:
  1. Commit and push changes to the remote git branch.
  2. Update `EEZYBOT_ROS_APP_GIT_REV` in `meta-eezybot/recipes-ros2/eezybotarm/eezybotarm-app.inc` to match the target commit hash.
  3. Rebuild the image with `./build.sh dev` (or rebuild the specific recipe via `./build.sh shell`).

## Ignored & Heavy Directories
* **Do NOT search or parse inside:** `build/tmp/`, `build/sstate-cache/`, `build/downloads/`, `build/cache/`.
