SUMMARY = "EezyBot development and debugging tools"
DESCRIPTION = "Packagegroup for the EezyBot dev image. Installs Python development \
tooling (pip, venv), text editors (nano, vim), hardware debugging (i2c-tools), \
Wi-Fi networking, and system diagnostic tools."
LICENSE = "MIT"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

# Wi-Fi diagnostics and CLI inspection tools
RDEPENDS:${PN} = " \
    iw \
"

# Hardware debugging
RDEPENDS:${PN} += " \
    i2c-tools \
    usbutils \
    tegra-tools-tegrastats \
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
    htop \
"

# ROS 2 CLI debugging extensions
RDEPENDS:${PN} += " \
    ros2controlcli \
"

# TensorRT CLI builder for compiling .engine models over SSH
RDEPENDS:${PN} += " \
    tensorrt-trtexec \
"

# ONNX runtime for dev image
RDEPENDS:${PN} += " \
    python3-onnxruntime \
"

# Web Dashboard for dev image
RDEPENDS:${PN} += " \
    eezybotarm-dashboard \
    python3-nicegui \
"
