SUMMARY = "Modular, easily extendable, and fast web UI for Python"
HOMEPAGE = "https://nicegui.io"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "https://files.pythonhosted.org/packages/af/38/dd6dc5bfa2d70343a45f83f0b52b7d6411ba65e6b3035000fd633aad0f5e/nicegui-3.18.0-py3-none-any.whl;downloadfilename=nicegui-${PV}-py3-none-any.zip;subdir=${BP}"
SRC_URI[sha256sum] = "a2bd51642312d98aab6d8b641245d796ff1b89da5829777170bcc488140c3dcd"

inherit python3-dir

do_install() {
    install -d ${D}${PYTHON_SITEPACKAGES_DIR}
    cp -r ${S}/nicegui* ${D}${PYTHON_SITEPACKAGES_DIR}/
}

FILES:${PN} += "${PYTHON_SITEPACKAGES_DIR}"

RDEPENDS:${PN} = " \
    python3-aiofiles \
    python3-anyio \
    python3-docutils \
    python3-engineio \
    python3-fastapi \
    python3-httpx \
    python3-ifaddr \
    python3-jinja2 \
    python3-markdown2 \
    python3-pydantic \
    python3-pygments \
    python3-python-multipart \
    python3-socketio \
    python3-starlette \
    python3-tinycss2 \
    python3-typing-extensions \
    python3-uvicorn \
    python3-vbuild \
    python3-websockets \
"
