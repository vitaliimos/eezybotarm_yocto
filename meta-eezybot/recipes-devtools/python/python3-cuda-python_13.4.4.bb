SUMMARY = "CUDA Python low-level bindings for aarch64"
HOMEPAGE = "https://nvidia.github.io/cuda-python"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

CUDA_PATHFINDER_PV = "1.8.4"

SRC_URI = " \
    https://files.pythonhosted.org/packages/e6/f0/d71d1dd45caa358a39c6898dcc9f1bd22a9f22b92376b967835c047b50bc/cuda_bindings-${PV}-cp314-cp314-manylinux_2_24_aarch64.manylinux_2_28_aarch64.whl;downloadfilename=cuda_bindings-${PV}-aarch64.zip;subdir=${BP};name=bindings \
    https://files.pythonhosted.org/packages/8e/93/f0904fa7d954ecc4c0db17c8d63d185d442dd59fc6757f1d1f448c59c256/cuda_pathfinder-${CUDA_PATHFINDER_PV}-py3-none-any.whl;downloadfilename=cuda_pathfinder-${CUDA_PATHFINDER_PV}-py3-none-any.zip;subdir=${BP};name=pathfinder \
    https://files.pythonhosted.org/packages/e1/2c/04c83864345051c6281396cd29d7190de1a23e2a533fa038c30b5552eb59/cuda_python-${PV}-py3-none-any.whl;downloadfilename=cuda_python-${PV}-py3-none-any.zip;subdir=${BP};name=cudapython \
"
SRC_URI[bindings.sha256sum] = "4a429132f06d0c3a853aa419aabe4c0a7bfc3f3886fb4c336d81729f63754d31"
SRC_URI[pathfinder.sha256sum] = "85a4e708c725d603f576042ef32781a3d1737c51e84c35fe361d2680191af65a"
SRC_URI[cudapython.sha256sum] = "2a4a35c7f102e3c51b9e18e65b34a24b82d7363893b6e2f228978c18bafa1957"

inherit python3-dir

COMPATIBLE_HOST = "aarch64.*-linux"

do_install() {
    install -d ${D}${PYTHON_SITEPACKAGES_DIR}
    cp -r ${S}/cuda* ${D}${PYTHON_SITEPACKAGES_DIR}/
}

FILES:${PN} += "${PYTHON_SITEPACKAGES_DIR}"
INSANE_SKIP:${PN} += "already-stripped textrel file-rdeps"

RDEPENDS:${PN} = " \
    cuda-cudart \
    tegra-libraries-cuda \
    python3-compression \
    python3-core \
    python3-ctypes \
    python3-json \
"
