#!/usr/bin/env python3
"""
Export a trained YOLO .pt model to an ephemeral ONNX file, compile it into a
TensorRT FP16 .engine on a target Jetson device over SSH, and fetch the compiled
model.engine into `meta-eezybot/recipes-model/cube-detection-model/files/model.engine`
for tracking in Git LFS.
"""

import argparse
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path
from ultralytics import YOLO


def run_cmd(cmd: list[str], desc: str) -> None:
    """Run a subprocess command and exit with an error message if it fails."""
    result = subprocess.run(cmd)
    if result.returncode != 0:
        print(f"\n[ERROR] Failed during: {desc}", file=sys.stderr)
        sys.exit(result.returncode)


def export_pt_to_onnx(pt_path: Path, onnx_dest: Path, imgsz: int, opset: int) -> None:
    """Export a PyTorch YOLO (.pt) weights file to ONNX in a temporary directory."""
    # Copy .pt into the temporary directory before exporting so Ultralytics writes
    # the intermediate .onnx next to the temporary copy rather than in the source tree.
    temp_pt = onnx_dest.parent / pt_path.name
    shutil.copyfile(pt_path, temp_pt)

    print(f"[1/4] Exporting '{pt_path}' to temporary ONNX (imgsz={imgsz}, opset={opset})...")
    try:
        model = YOLO(str(temp_pt))
        exported_str = model.export(
            format="onnx",
            imgsz=imgsz,
            opset=opset,
            dynamic=False,
            device="cpu",
        )
    except Exception as exc:
        print(f"Error exporting YOLO model to ONNX: {exc}", file=sys.stderr)
        sys.exit(1)

    exported_path = Path(exported_str)
    if not exported_path.is_file():
        print(f"Error: Expected exported ONNX file at '{exported_path}' not found.", file=sys.stderr)
        sys.exit(1)

    if exported_path.resolve() != onnx_dest.resolve():
        shutil.move(str(exported_path), str(onnx_dest))


def main() -> None:
    script_dir = Path(__file__).resolve().parent
    repo_root = script_dir.parent
    default_pt = repo_root.parent / "eeazybot" / "src" / "eezybotarm_vision" / "models" / "weights.pt"
    default_output_dir = repo_root / "meta-eezybot" / "recipes-model" / "cube-detection-model" / "files"

    parser = argparse.ArgumentParser(
        description=(
            "Convert a trained YOLO .pt model to a TensorRT .engine on Jetson over SSH "
            "and store it in meta-eezybot/recipes-model/cube-detection-model/files/model.engine."
        )
    )
    parser.add_argument(
        "--model",
        "-m",
        type=str,
        default=str(default_pt) if default_pt.is_file() else None,
        required=not default_pt.is_file(),
        help="Path to the trained YOLO .pt weights file (default: ../eeazybot/src/eezybotarm_vision/models/weights.pt).",
    )
    parser.add_argument(
        "--jetson",
        "-j",
        type=str,
        required=True,
        help="Target Jetson SSH address (e.g. root@192.168.1.50 or 192.168.1.50). Must run eezybot-image-dev.",
    )
    parser.add_argument(
        "--imgsz",
        type=int,
        default=640,
        help="Model input image size (default: 640).",
    )
    parser.add_argument(
        "--opset",
        type=int,
        default=17,
        help="ONNX opset version for intermediate export (default: 17).",
    )
    parser.add_argument(
        "--output",
        "-o",
        type=str,
        default=str(default_output_dir / "model.engine"),
        help="Destination path for compiled model.engine (default: meta-eezybot/recipes-model/cube-detection-model/files/model.engine).",
    )
    parser.add_argument(
        "--ssh-port",
        "-p",
        type=int,
        default=22,
        help="SSH port on the target Jetson (default: 22).",
    )

    args = parser.parse_args()

    model_path = Path(args.model).expanduser().resolve()
    if not model_path.is_file():
        print(f"Error: Model file '{model_path}' does not exist.", file=sys.stderr)
        sys.exit(1)

    if model_path.suffix.lower() != ".pt":
        print(
            f"Error: Expected a trained PyTorch '.pt' file, got '{model_path.name}'.",
            file=sys.stderr,
        )
        sys.exit(1)

    target_host = args.jetson if "@" in args.jetson else f"root@{args.jetson}"
    output_engine = Path(args.output).expanduser().resolve()
    output_engine.parent.mkdir(parents=True, exist_ok=True)

    ssh_base = ["ssh", "-p", str(args.ssh_port), target_host]
    scp_base = ["scp", "-P", str(args.ssh_port)]

    remote_tmp_onnx = "/tmp/yolo_model_convert.onnx"
    remote_engine = "/usr/share/models/model.engine"
    remote_tmp_engine = f"{remote_engine}.tmp"

    with tempfile.TemporaryDirectory(prefix="yolo_trt_export_") as tmp_dir_str:
        tmp_dir = Path(tmp_dir_str)
        local_tmp_onnx = tmp_dir / "model.onnx"

        # Step 1: Export .pt -> temporary .onnx locally
        export_pt_to_onnx(model_path, local_tmp_onnx, args.imgsz, args.opset)

        # Step 2: Upload temporary .onnx to Jetson /tmp
        print(f"[2/4] Uploading intermediate ONNX to {target_host}:{remote_tmp_onnx}...")
        run_cmd(
            scp_base + [str(local_tmp_onnx), f"{target_host}:{remote_tmp_onnx}"],
            "uploading ONNX model to Jetson via SCP",
        )

        # Step 3: Run trtexec on Jetson and clean up remote /tmp/yolo_model_convert.onnx
        print(f"[3/4] Compiling TensorRT FP16 engine on {target_host} (this takes a few minutes)...")
        remote_script = (
            "set -e; "
            "trap 'rm -f " + remote_tmp_onnx + " " + remote_tmp_engine + "' EXIT; "
            "TRTEXEC=$(command -v trtexec || echo /usr/src/tensorrt/bin/trtexec); "
            "if [ ! -x \"$TRTEXEC\" ]; then "
            "  echo \"Error: trtexec not found on Jetson. Ensure the dev image (eezybot-image-dev) is running.\" >&2; "
            "  exit 1; "
            "fi; "
            "mkdir -p /usr/share/models; "
            "\"$TRTEXEC\" "
            f"--onnx={remote_tmp_onnx} "
            f"--saveEngine={remote_tmp_engine} "
            "--fp16; "
            f"mv {remote_tmp_engine} {remote_engine}; "
            f"chmod 0644 {remote_engine}"
        )
        run_cmd(
            ssh_base + [remote_script],
            "compiling TensorRT engine on Jetson via trtexec",
        )

        # Step 4: Download compiled model.engine back to host Yocto repository
        print(f"[4/4] Downloading compiled engine from {target_host}:{remote_engine} to {output_engine}...")
        run_cmd(
            scp_base + [f"{target_host}:{remote_engine}", str(output_engine)],
            "downloading compiled model.engine from Jetson via SCP",
        )

    size_mb = output_engine.stat().st_size / (1024 * 1024)
    print("\n[SUCCESS] TensorRT engine successfully compiled and fetched!")
    print(f"  Local artifact (Git LFS): {output_engine} ({size_mb:.2f} MB)")
    print(f"  Active on Jetson:         {target_host}:{remote_engine}")
    print("\nNext steps:")
    print(f"  1. Commit the engine to Git LFS: git add {output_engine.relative_to(repo_root)} && git commit")
    print("  2. Build the Yocto image:        ./build.sh prod   (or ./build.sh dev)")


if __name__ == "__main__":
    main()
