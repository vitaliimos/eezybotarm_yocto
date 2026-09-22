#!/bin/bash
# -*- mode: shell-script; indent-tabs-mode: nil; sh-basic-offset: 4; -*-

set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "$(readlink -f "$0")")" && pwd)
MACHINE=$(sed -n 's/^machine:[[:space:]]*//p' "$SCRIPT_DIR/kas/base.yml")
DEPLOY_DIR="$SCRIPT_DIR/build/tmp/deploy/images/$MACHINE"
FLASH_DIR="$SCRIPT_DIR/flash"

full_flash=0
erase_nvme=0
debug=0
package=

usage() {
    cat <<EOF
Usage: $(basename "$0") [options] [package]

Flash the Jetson device using the current build. By default only the
external storage device (NVMe) is flashed and the boot firmware (QSPI)
is left untouched; use --with-bootloader for the very first flash.

Options:
  --with-bootloader  Full flash: also write the boot firmware (QSPI)
  --erase-nvme       Erase the NVMe drive during flashing
  -D|--debug         Enable debug logging in the flashing script
  -h|--help          Show this help

Arguments:
  package            Path to a .rootfs-*.tegraflash-tar.zst package.
                     Defaults to the newest package in:
                       $DEPLOY_DIR

Steps:
  1. Ensure the device is in recovery mode:
     power off -> hold Recovery -> connect USB-C OTG port -> power on
  2. Verify with: lsusb -d 0955:
EOF
}

while [ $# -gt 0 ]; do
    case "$1" in
        --with-bootloader) full_flash=1; shift ;;
        --erase-nvme) erase_nvme=1; shift ;;
        -D|--debug) debug=1; shift ;;
        -h|--help) usage; exit 0 ;;
        -*) echo "Unknown option: $1" >&2; usage >&2; exit 1 ;;
        *) package="$1"; shift ;;
    esac
done

if [ -z "$MACHINE" ]; then
    echo "ERR: could not determine machine from kas/base.yml" >&2
    exit 1
fi

if [ -z "$package" ]; then
    if [ ! -d "$DEPLOY_DIR" ]; then
        echo "ERR: deploy dir $DEPLOY_DIR not found" >&2
        echo "     build first: kas-container build kas/base.yml:kas/dev.yml (or kas/production.yml)" >&2
        exit 1
    fi
    package=$(ls -1t "$DEPLOY_DIR"/*.rootfs-*.tegraflash-tar.zst 2>/dev/null | head -1)
    if [ -z "$package" ]; then
        echo "ERR: no *.rootfs-*.tegraflash-tar.zst package in $DEPLOY_DIR" >&2
        echo "     build first: kas-container build kas/base.yml:kas/dev.yml (or kas/production.yml)" >&2
        exit 1
    fi
fi

if [ ! -f "$package" ]; then
    echo "ERR: package not found: $package" >&2
    exit 1
fi

echo "Machine:   $MACHINE"
echo "Package:   $(basename "$package")"
echo "Flash dir: $FLASH_DIR"
echo "Boot firmware (QSPI) write: $([ $full_flash -eq 1 ] && echo yes || echo "no (external only)")"

missing=()
for tool in dtc gcc gdisk udisksctl bmaptool lsusb; do
    command -v "$tool" >/dev/null 2>&1 || missing+=("$tool")
done
if [ ${#missing[@]} -gt 0 ]; then
    echo "ERR: missing required tools: ${missing[*]}" >&2
    echo "     install: sudo apt install device-tree-compiler gcc gdisk udisks2 bmap-tools usbutils" >&2
    exit 1
fi

if ! tar --help 2>&1 | grep -q -- '--zstd'; then
    echo "ERR: tar does not support --zstd (need GNU tar)" >&2
    exit 1
fi

if ! lsusb -d 0955: >/dev/null 2>&1; then
    cat >&2 <<EOF
ERR: no NVIDIA device in recovery mode found on USB.

Put the device in recovery mode:
  1. power off the device
  2. hold the Recovery button
  3. while holding it, connect the USB-C OTG port to this host
  4. power on; keep holding Recovery until this shows a device:
     lsusb -d 0955:
EOF
    exit 1
fi

mkdir -p "$FLASH_DIR"
rm -rf "$FLASH_DIR"/*
tar --zstd -xf "$package" -C "$FLASH_DIR"

if [ ! -x "$FLASH_DIR/initrd-flash" ]; then
    echo "ERR: initrd-flash not found in package" >&2
    exit 1
fi

flash_args=()
if [ $full_flash -eq 0 ]; then
    flash_args+=(--external-only)
fi
[ $erase_nvme -eq 1 ] && flash_args+=(--erase-nvme)
[ $debug -eq 1 ] && flash_args+=(-D)

log="$FLASH_DIR/log.flash.$(date +%Y-%m-%d-%H.%M.%S)"
cd "$FLASH_DIR"
echo "== ./initrd-flash ${flash_args[*]:-} ==" | tee "$log"
if ./initrd-flash "${flash_args[@]}" 2>&1 | tee -a "$log"; then
    echo "SUCCESS: device flashed." | tee -a "$log"
    echo "Log: $(basename "$log")"
    echo "Connect: ssh root@<device-ip>"
else
    echo "ERR: flashing failed. See $(basename "$log")" >&2
    exit 1
fi