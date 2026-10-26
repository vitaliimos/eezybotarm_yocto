#!/bin/sh
set -e

if [ ! -f /usr/share/models/model.engine ] && [ -f /usr/share/models/model.onnx ]; then
    echo "[eezybotarm] model.engine not found. Compiling from model.onnx via trtexec..."
    TRTEXEC=$(command -v trtexec 2>/dev/null || echo /usr/src/tensorrt/bin/trtexec)
    if [ -x "$TRTEXEC" ]; then
        "$TRTEXEC" \
            --onnx=/usr/share/models/model.onnx \
            --saveEngine=/usr/share/models/model.engine.tmp \
            --timingCacheFile=/usr/share/models/model.timing.cache \
            --fp16 \
            --skipInference
        mv /usr/share/models/model.engine.tmp /usr/share/models/model.engine
        chmod 0644 /usr/share/models/model.engine
        echo "[eezybotarm] TensorRT engine compiled successfully."
    else
        echo "[eezybotarm] ERROR: trtexec binary not found!" >&2
        exit 1
    fi
fi
