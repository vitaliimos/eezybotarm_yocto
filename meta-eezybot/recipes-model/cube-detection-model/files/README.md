# Model Assets Directory

This directory holds the pre-trained YOLO cube detection ONNX model (`model.onnx`, tracked via Git LFS) packaged into `/usr/share/models/model.onnx` on the Yocto image.

---

## How Models Are Used Across Distros

### 1. Dev Distro (`eezybot-image-dev`)
- Uses `model.onnx` directly via **ONNX Runtime** (`python3-onnxruntime`).
- Launched by `jetson.launch.py` with `profile:=dev`, avoiding delays for TensorRT engine compilation during development and testing.

### 2. Production Distro (`eezybot-image`)
- Automatically compiles `/usr/share/models/model.onnx` into `/usr/share/models/model.engine` on the Jetson Orin Nano GPU using `trtexec --fp16` during the first boot.
- Handled automatically via `ExecStartPre=/usr/bin/eezybotarm-ensure-engine.sh` in `eezybotarm.service`.
- Once compiled, subsequent boots and restarts immediately use the generated `model.engine` with TensorRT hardware acceleration in `jetson.launch.py` (`profile:=prod`).

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
