#!/bin/sh
set -e

# Only compile engine if we are running production launch (or engine explicitly needed)
if [ "$EEZYBOTARM_LAUNCH" = "production.launch.py" ]; then
    if [ ! -f /usr/share/models/model.engine ] && [ -f /usr/share/models/model.onnx ]; then
        echo "[eezybotarm] model.engine not found. Compiling from model.onnx via trtexec..."
        TRTEXEC=$(command -v trtexec 2>/dev/null || echo /usr/src/tensorrt/bin/trtexec)
        if [ -x "$TRTEXEC" ]; then
            "$TRTEXEC" --onnx=/usr/share/models/model.onnx --saveEngine=/usr/share/models/model.engine.tmp --fp16
            mv /usr/share/models/model.engine.tmp /usr/share/models/model.engine
            chmod 0644 /usr/share/models/model.engine
            echo "[eezybotarm] TensorRT engine compiled successfully."
        else
            echo "[eezybotarm] ERROR: trtexec binary not found!" >&2
            exit 1
        fi
    fi
fi
