#!/bin/bash
# -*- mode: shell-script; indent-tabs-mode: nil; sh-basic-offset: 4; -*-

set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "$(readlink -f "$0")")" && pwd)

usage() {
    cat <<EOF
Usage: $(basename "$0") [prod|dev|shell] [kas-options...]

Build EezyBot Yocto images using kas-container.

Targets:
  prod, production    Build eezybot-image (minimal headless production image)
  dev, development    Build eezybot-image-dev (headless dev image with Python/dev tooling)
  shell               Launch an interactive shell in the build environment

Environment variables:
  KAS_SSH_DIR         Directory containing SSH configuration/deploy keys
                      (default: ~/.config/kas-ssh)

Examples:
  $(basename "$0") prod
  $(basename "$0") dev
  $(basename "$0") shell
EOF
}

TARGET="${1:-}"
if [ -z "$TARGET" ]; then
    usage
    exit 1
fi
shift

case "$TARGET" in
    prod|production)
        CMD="build"
        CONFIG="kas/base.yml:kas/production.yml"
        ;;
    dev|development)
        CMD="build"
        CONFIG="kas/base.yml:kas/dev.yml"
        ;;
    shell)
        CMD="shell"
        CONFIG="kas/base.yml:kas/dev.yml"
        ;;
    -h|--help)
        usage
        exit 0
        ;;
    *)
        echo "Error: Unknown target '$TARGET'. Expected 'prod', 'dev', or 'shell'." >&2
        usage
        exit 1
        ;;
esac

if ! command -v kas-container &>/dev/null; then
    echo "Error: 'kas-container' command not found." >&2
    echo "Please install kas (e.g. 'pip install kas') or ensure kas-container is in your PATH." >&2
    exit 1
fi

SSH_DIR="${KAS_SSH_DIR:-$HOME/.config/kas-ssh}"
KAS_ARGS=()

if [ -d "$SSH_DIR" ]; then
    KAS_ARGS+=(--ssh-dir "$SSH_DIR")
fi

cd "$SCRIPT_DIR"
exec kas-container "${KAS_ARGS[@]}" "$CMD" "$CONFIG" "$@"
