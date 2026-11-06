SUMMARY = "A fast and complete Python implementation of Markdown"
HOMEPAGE = "https://github.com/trentm/python-markdown2"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=0ad015e3507344d0963fc15585661b1b"

SRC_URI[sha256sum] = "001547e68f6e7fcf0f1cb83f7e82f48aa7d48b2c6a321f0cd20a853a8a2d1664"

inherit pypi setuptools3

PYPI_PACKAGE = "markdown2"

do_install:append() {
    # setup.py packages testing/tox.ini into ${prefix}/testing, which is unwanted in the rootfs
    rm -rf ${D}${prefix}/testing
}

RDEPENDS:${PN} += " \
    python3-core \
"
