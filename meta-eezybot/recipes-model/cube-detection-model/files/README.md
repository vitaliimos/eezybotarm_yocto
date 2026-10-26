# Model Assets Directory

This directory holds the pre-trained YOLO cube detection ONNX model (`model.onnx`, tracked via Git LFS) packaged into `/usr/share/models/model.onnx` on the Yocto image.

---

## How Models Are Used Across Distros

### 1. Dev Distro (`eezybot-image-dev`)
- Uses `model.onnx` directly via **ONNX Runtime** (`python3-onnxruntime`).
- Launched by `jetson.launch.py` with `profile:=dev`, avoiding delays for TensorRT engine compilation during development and testing.

### 2. Production Distro (`eezybot-image`)
- If `model.engine` is present in `meta-eezybot/recipes-model/cube-detection-model/files/model.engine` during the Yocto build, it is packaged directly into `/usr/share/models/model.engine`.
- Otherwise, `eezybotarm.service` automatically compiles `/usr/share/models/model.onnx` into `/usr/share/models/model.engine` on the Jetson Orin Nano GPU using `trtexec --fp16 --skipInference` during the first boot (`ExecStartPre=/usr/bin/eezybotarm-ensure-engine.sh`).
- You can also compile `model.onnx` remotely on a running Jetson and pull `model.engine` back into this directory for subsequent builds:
  ```bash
  ./compile-engine-remote.sh <jetson-ip>
  ```

---

## Updating `model.onnx`

When new weights (`weights.pt`) are trained copy from ROS 2 package
```bash
cp ../eeazybot/src/eezybotarm_vision/models/model.onnx meta-eezybot/recipes-model/cube-detection-model/files/model.onnx
```

### Commit via Git LFS:
```bash
git add meta-eezybot/recipes-model/cube-detection-model/files/model.onnx
git commit -m "model: update cube detection ONNX model"
```
Rebuild the target image using `./build.sh dev` or `./build.sh prod`.
