# Model Assets Directory

The single source of truth (SSOT) for `model.onnx` is `src/eezybotarm_vision/models/model.onnx` in the `eeazybot` (`eeazybot_ros`) repository.

This directory (`files/`) stores an optional pre-compiled TensorRT `model.engine` (gitignored, since `.engine` plans are specific to the Jetson Orin Nano GPU and TensorRT version).

---

## How Models Are Deployed

Both `eezybot-image` (prod) and `eezybot-image-dev` (dev) run inference using `/usr/share/models/model.engine` via TensorRT:

1. **When `files/model.engine` is present locally during the Yocto build:**
   `cube-detection-model` packages `model.engine` directly into `/usr/share/models/model.engine` (and omits `model.onnx` from the image).
2. **When `files/model.engine` is NOT present locally during the Yocto build:**
   `cube-detection-model` fetches `model.onnx` from `eeazybot_ros` at `EEZYBOT_ROS_APP_GIT_REV` and installs it into `/usr/share/models/model.onnx`. On first boot, `eezybotarm.service` automatically compiles `/usr/share/models/model.onnx` into `/usr/share/models/model.engine` on the Jetson Orin Nano GPU using `trtexec --fp16 --skipInference` (`ExecStartPre=/usr/bin/eezybotarm-ensure-engine.sh`).
3. **Compiling `model.engine` remotely on a running Jetson:**
   You can compile `model.onnx` remotely on a running Jetson and pull `model.engine` back into this directory for subsequent builds:
   ```bash
   ./compile-engine-remote.sh <jetson-ip>
   ```

---

## End-to-End Model Update Workflow

1. **Export in `eeazybot_cv`**:
   ```bash
   python3 export.py <experiment_name>
   ```
   This exports directly to `../eeazybot/src/eezybotarm_vision/models/model.onnx` and removes any stale local `model.engine` files in `eeazybot` and `eeazybot_yocto`.
2. **Commit & push in `eeazybot`**:
   Commit the updated `src/eezybotarm_vision/models/model.onnx` (tracked via Git LFS) and push.
3. **Update `EEZYBOT_ROS_APP_GIT_REV` in `eeazybot_yocto`**:
   Update the commit hash in `meta-eezybot/recipes-ros2/eezybotarm/eezybotarm-app.inc` (and optionally run `./compile-engine-remote.sh <jetson-ip>` to precompile `files/model.engine`), then rebuild with `./build.sh dev` or `./build.sh prod`.
