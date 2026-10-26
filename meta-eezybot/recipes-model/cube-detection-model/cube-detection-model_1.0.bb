SUMMARY = "Pre-trained YOLO cube detection ONNX model"
DESCRIPTION = "Installs model.onnx into /usr/share/models/ for the EezyBotArm cube detection vision node."

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = " \
    ${@'file://model.onnx' if os.path.isfile(os.path.join(d.getVar('THISDIR'), 'files', 'model.onnx')) else ''} \
    ${@'file://model.engine' if os.path.isfile(os.path.join(d.getVar('THISDIR'), 'files', 'model.engine')) else ''} \
"

S = "${UNPACKDIR}"

inherit allarch
ALLOW_EMPTY:${PN} = "1"

python do_warn_missing_model() {
    import os
    model_path = os.path.join(d.getVar('THISDIR'), 'files', 'model.onnx')
    if not os.path.isfile(model_path):
        bb.warn("model.onnx not found in meta-eezybot/recipes-model/cube-detection-model/files/.")
}
addtask warn_missing_model before do_install

do_install() {
    install -d ${D}/usr/share/models
    if [ -f "${UNPACKDIR}/model.onnx" ]; then
        install -m 0644 ${UNPACKDIR}/model.onnx ${D}/usr/share/models/model.onnx
    fi
    if [ -f "${UNPACKDIR}/model.engine" ]; then
        install -m 0644 ${UNPACKDIR}/model.engine ${D}/usr/share/models/model.engine
    fi
}

FILES:${PN} = "/usr/share/models"
