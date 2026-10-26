SUMMARY = "Pre-trained YOLO cube detection TensorRT / ONNX model"
DESCRIPTION = "Installs pre-compiled model.engine from files/ into /usr/share/models/ if present \
locally; otherwise installs model.onnx from the eeazybot_ros repository for first-boot compilation."

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

EEZYBOT_ROS_APP_PKG = "eezybotarm_vision"
require ../../recipes-ros2/eezybotarm/eezybotarm-app.inc

BB_DONT_CACHE = "1"

SRC_URI += " \
    ${@'file://model.engine' if os.path.isfile(os.path.join(d.getVar('THISDIR'), 'files', 'model.engine')) else ''} \
"

inherit allarch

do_install() {
    install -d ${D}/usr/share/models
    if [ -f "${UNPACKDIR}/model.engine" ]; then
        install -m 0644 ${UNPACKDIR}/model.engine ${D}/usr/share/models/model.engine
    else
        install -m 0644 ${S}/models/model.onnx ${D}/usr/share/models/model.onnx
    fi
}

FILES:${PN} = "/usr/share/models"
