SUMMARY = "EezyBot development and debugging tools"
DESCRIPTION = "Packagegroup for the EezyBot dev image. Installs Python development \
tooling (pip, venv), text editors (nano, vim), hardware debugging (i2c-tools), \
Wi-Fi networking, and system diagnostic tools."
LICENSE = "MIT"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

# Wi-Fi client support and tools (autoconfiguration is baked into the image:
# wpa_supplicant@wlan0 via SYSTEMD_SERVICE, config/credentials written by the
# image postprocess from credentials.inc).
RDEPENDS:${PN} = " \
    iw \
    wpa-supplicant \
    wireless-regdb-static \
"

# Hardware debugging
RDEPENDS:${PN} += " \
    i2c-tools \
"

# Python development tooling and editors
RDEPENDS:${PN} += " \
    python3 \
    python3-pip \
    python3-venv \
    nano \
    vim \
"

# Common system development / debugging tooling
RDEPENDS:${PN} += " \
    bash \
    git \
    gdb \
    strace \
    tcpdump \
    sudo \
    v4l-utils \
"

# TensorRT CLI builder for compiling .engine models over SSH
RDEPENDS:${PN} += " \
    tensorrt-trtexec \
"

# ONNX runtime for dev image
RDEPENDS:${PN} += " \
    python3-onnxruntime \
"
