#!/bin/bash
# -*- mode: shell-script; indent-tabs-mode: nil; sh-basic-offset: 4; -*-

set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "$(readlink -f "$0")")" && pwd)
DEFAULT_ENGINE_DIR="$SCRIPT_DIR/meta-eezybot/recipes-model/cube-detection-model/files"
DEFAULT_ENGINE="$DEFAULT_ENGINE_DIR/model.engine"
DEFAULT_ONNX="$SCRIPT_DIR/../eeazybot/src/eezybotarm_vision/models/model.onnx"

target_host=""
onnx_path=""
output_path=""
restart_service=1

usage() {
    cat <<EOF
Usage: $(basename "$0") [options] <[user@]jetson-host> [onnx-file] [output-engine]

Compile an ONNX model into a TensorRT FP16 .engine in /usr/share/models/model.engine
on a running Jetson device, and download the compiled engine back to the host PC
for packaging into subsequent Yocto prod builds.

If a local ONNX file is specified (or found at $DEFAULT_ONNX),
it is uploaded to the Jetson first; otherwise the existing /usr/share/models/model.onnx
on the Jetson is used.

Temporarily stops eezybotarm.service on the Jetson during compilation to free
GPU memory and prevent systemd start-pre conflicts, then starts it back.

Arguments:
  [user@]jetson-host   Jetson IP or hostname (defaults to root@<host> if user omitted)
  onnx-file            Input .onnx file path
                       (default: $DEFAULT_ONNX if present, else remote /usr/share/models/model.onnx)
  output-engine        Local destination path for compiled .engine
                       (default: $DEFAULT_ENGINE)

Options:
  -i, --onnx PATH      Input .onnx file path
  -o, --output PATH    Local output .engine path
  --no-restart         Do not start eezybotarm.service after compilation
  -h, --help           Show this help

Examples:
  ./$(basename "$0") 192.168.1.50
  ./$(basename "$0") root@eezybot.local path/to/model.onnx path/to/model.engine
EOF
}

positional=()
while [ $# -gt 0 ]; do
    case "$1" in
        -i|--onnx)
            onnx_path="$2"
            shift 2
            ;;
        -o|--output)
            output_path="$2"
            shift 2
            ;;
        --no-restart)
            restart_service=0
            shift
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        -*)
            echo "ERR: unknown option: $1" >&2
            usage >&2
            exit 1
            ;;
        *)
            positional+=("$1")
            shift
            ;;
    esac
done

if [ ${#positional[@]} -ge 1 ]; then
    target_host="${positional[0]}"
fi
if [ ${#positional[@]} -ge 2 ] && [ -z "$onnx_path" ]; then
    onnx_path="${positional[1]}"
fi
if [ ${#positional[@]} -ge 3 ] && [ -z "$output_path" ]; then
    output_path="${positional[2]}"
fi

if [ -z "$target_host" ]; then
    echo "ERR: Jetson host address is required." >&2
    usage >&2
    exit 1
fi

# Default SSH user to root if not specified
case "$target_host" in
    *@*) ssh_target="$target_host" ;;
    *)   ssh_target="root@$target_host" ;;
esac

if [ -n "$onnx_path" ]; then
    if [ ! -f "$onnx_path" ]; then
        echo "ERR: ONNX file not found: $onnx_path" >&2
        exit 1
    fi
elif [ -f "$DEFAULT_ONNX" ]; then
    onnx_path="$DEFAULT_ONNX"
fi

if [ -z "$output_path" ]; then
    output_path="$DEFAULT_ENGINE"
fi

mkdir -p "$(dirname "$(readlink -f -m "$output_path")")"

REMOTE_DIR="/usr/share/models"
REMOTE_ONNX="$REMOTE_DIR/model.onnx"
REMOTE_ENGINE="$REMOTE_DIR/model.engine"
REMOTE_CACHE="$REMOTE_DIR/model.timing.cache"

echo "== Target Jetson: $ssh_target =="
if [ -n "$onnx_path" ]; then
    echo "== Local ONNX:    $onnx_path =="
else
    echo "== Remote ONNX:   $ssh_target:$REMOTE_ONNX =="
fi
echo "== Local output:  $output_path =="

cleanup_remote() {
    if [ "$restart_service" -eq 1 ]; then
        echo "== Starting eezybotarm.service on $ssh_target =="
        ssh "$ssh_target" "systemctl start eezybotarm.service || true" >/dev/null 2>&1 || true
    fi
}
trap cleanup_remote EXIT

echo "== Stopping eezybotarm.service on $ssh_target to free GPU memory =="
ssh "$ssh_target" "systemctl stop eezybotarm.service || true; mkdir -p '$REMOTE_DIR'"

if [ -n "$onnx_path" ]; then
    echo "== Uploading $(basename "$onnx_path") -> $ssh_target:$REMOTE_ONNX =="
    scp "$onnx_path" "$ssh_target:$REMOTE_ONNX"
fi

echo "== Compiling TensorRT FP16 engine on $ssh_target (this may take 10-15 minutes on first run) =="
ssh "$ssh_target" "set -e
TRTEXEC=\$(command -v trtexec 2>/dev/null || echo /usr/src/tensorrt/bin/trtexec)
if [ ! -x \"\$TRTEXEC\" ]; then
    echo 'ERR: trtexec binary not found on Jetson!' >&2
    exit 1
fi
rm -f '$REMOTE_ENGINE.tmp'
\"\$TRTEXEC\" \
    --onnx='$REMOTE_ONNX' \
    --saveEngine='$REMOTE_ENGINE.tmp' \
    --timingCacheFile='$REMOTE_CACHE' \
    --fp16 \
    --skipInference
mv '$REMOTE_ENGINE.tmp' '$REMOTE_ENGINE'
chmod 0644 '$REMOTE_ENGINE'
ls -lh '$REMOTE_ENGINE'
"

echo "== Downloading $ssh_target:$REMOTE_ENGINE -> $output_path =="
scp "$ssh_target:$REMOTE_ENGINE" "$output_path.tmp"
mv "$output_path.tmp" "$output_path"

echo "== SUCCESS: TensorRT engine saved to $output_path =="
ls -lh "$output_path"
