SUMMARY = "A fast and complete Python implementation of Markdown"
HOMEPAGE = "https://github.com/trentm/python-markdown2"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "https://files.pythonhosted.org/packages/43/af/4b3891eb0a49d6cfd5cbf3e9bf514c943afc2b0f13e2c57cc57cd88ecc21/markdown2-2.5.5-py3-none-any.whl;downloadfilename=markdown2-${PV}-py3-none-any.zip;subdir=${BP}"
SRC_URI[sha256sum] = "be798587e09d1f52d2e4d96a649c4b82a778c75f9929aad52a2c95747fa26941"

inherit python3-dir

do_install() {
    install -d ${D}${PYTHON_SITEPACKAGES_DIR}
    cp -r ${S}/markdown2* ${D}${PYTHON_SITEPACKAGES_DIR}/
}

FILES:${PN} += "${PYTHON_SITEPACKAGES_DIR}"

RDEPENDS:${PN} = " \
    python3-core \
"
