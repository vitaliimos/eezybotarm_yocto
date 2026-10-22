SUMMARY = "Compile your vuejs components (*.vue) to standalone html/js/css"
HOMEPAGE = "https://github.com/manatlan/vbuild"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "https://files.pythonhosted.org/packages/a6/3d/7b22abbdb059d551507275a2815bc2b1974e3b9f6a13781c1eac9e858965/vbuild-0.8.2-py2.py3-none-any.whl;downloadfilename=vbuild-${PV}-py2.py3-none-any.zip;subdir=${BP}"
SRC_URI[sha256sum] = "d76bcc976a1c53b6a5776ac947606f9e7786c25df33a587ebe33ed09dd8a1076"

inherit python3-dir

do_install() {
    install -d ${D}${PYTHON_SITEPACKAGES_DIR}
    cp -r ${S}/vbuild* ${D}${PYTHON_SITEPACKAGES_DIR}/
}

FILES:${PN} += "${PYTHON_SITEPACKAGES_DIR}"

RDEPENDS:${PN} = " \
    python3-core \
"
