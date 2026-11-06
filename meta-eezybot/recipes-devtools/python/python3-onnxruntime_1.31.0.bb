SUMMARY = "ONNX Runtime Python package for aarch64"
HOMEPAGE = "https://onnxruntime.ai"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "https://files.pythonhosted.org/packages/37/fb/8be04665b700cb6e874d944e9932bb3c3969d3f53e820f5c42bfd26565d0/onnxruntime-1.31.0-cp314-cp314-manylinux_2_28_aarch64.whl;downloadfilename=onnxruntime-${PV}-aarch64.zip;subdir=${BP}"
SRC_URI[sha256sum] = "984c0a2c1ad6a41fbc101dc3949abe4a72254892d01a5e70d9b792711e0bfa54"

inherit python3-dir

COMPATIBLE_HOST = "aarch64.*-linux"

python do_configure() {
    py_ver = d.getVar('PYTHON_BASEVERSION')
    if py_ver != '3.14':
        bb.fatal(f"{d.getVar('PN')} contains pre-built aarch64 binaries compiled for Python 3.14 ABI (cp314), but target PYTHON_BASEVERSION is '{py_ver}'.")
}

do_install() {
    install -d ${D}${PYTHON_SITEPACKAGES_DIR}
    cp -R --no-preserve=ownership ${S}/onnxruntime* ${D}${PYTHON_SITEPACKAGES_DIR}/
    chown -R root:root ${D}${PYTHON_SITEPACKAGES_DIR}
    find ${D}${PYTHON_SITEPACKAGES_DIR} -type d -exec chmod 0755 {} +
    find ${D}${PYTHON_SITEPACKAGES_DIR} -type f -name "*.so*" -exec chmod 0755 {} +
    find ${D}${PYTHON_SITEPACKAGES_DIR} -type f ! -name "*.so*" -exec chmod 0644 {} +
}

FILES:${PN} += "${PYTHON_SITEPACKAGES_DIR}"
INSANE_SKIP:${PN} += "already-stripped textrel"

RDEPENDS:${PN} = " \
    python3-core \
    python3-numpy \
    python3-protobuf \
    python3-flatbuffers \
    python3-packaging \
"
