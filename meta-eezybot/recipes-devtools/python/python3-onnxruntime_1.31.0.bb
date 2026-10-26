SUMMARY = "ONNX Runtime with GPU (CUDA) support for aarch64"
HOMEPAGE = "https://onnxruntime.ai"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "https://files.pythonhosted.org/packages/4b/73/9da420e23d5298adefc2cfb0c2e54121f3a713f173061933d5cb967b6bda/onnxruntime_gpu-1.31.0-cp314-cp314-manylinux_2_34_aarch64.whl;downloadfilename=onnxruntime_gpu-${PV}-aarch64.zip;subdir=${BP}"
SRC_URI[sha256sum] = "085b6403f6ad690027320eab22d9c18362d32d132e4bb42a3a114cbb2d7c01db"

inherit python3-dir

COMPATIBLE_HOST = "aarch64.*-linux"

do_install() {
    install -d ${D}${PYTHON_SITEPACKAGES_DIR}
    cp -r ${S}/onnxruntime* ${D}${PYTHON_SITEPACKAGES_DIR}/
}

FILES:${PN} += "${PYTHON_SITEPACKAGES_DIR}"
INSANE_SKIP:${PN} += "already-stripped textrel file-rdeps"

RDEPENDS:${PN} = " \
    cuda-cudart \
    libcublas \
    libcurand \
    tegra-libraries-cuda \
    python3-core \
    python3-numpy \
    python3-protobuf \
    python3-flatbuffers \
    python3-packaging \
"
