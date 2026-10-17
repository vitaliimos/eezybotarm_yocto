# Model Assets Directory

This directory holds the pre-compiled TensorRT engine (`model.engine`, tracked via Git LFS) packaged into `/usr/share/models/model.engine` on the Yocto image.

## Generating / Updating `model.engine`

Because TensorRT `.engine` plans are specific to the target GPU architecture (Jetson Orin Nano `tegra234`) and exact TensorRT/CUDA version, compilation is performed over SSH on a Jetson running `eezybot-image-dev` (which includes `trtexec`):

```bash
# On your PC/workstation (with PyTorch and Ultralytics installed):
python3 scripts/prepare_yolo_model.py \
    --model ../eeazybot/src/eezybotarm_vision/models/weights.pt \
    --jetson root@<jetson-ip>
```

What this script does:
1. Exports the trained `.pt` model to a temporary `.onnx` file on your PC.
2. Uploads the temporary `.onnx` to `/tmp/` on the Jetson over SCP.
3. Runs `trtexec --fp16` on the Jetson to produce `/usr/share/models/model.engine` and removes the temporary `.onnx`.
4. Downloads the compiled `model.engine` into `meta-eezybot/recipes-model/cube-detection-model/files/model.engine`.

After generating a new `model.engine`, commit it via Git LFS and rebuild the Yocto image (`./build.sh prod` or `./build.sh dev`).
