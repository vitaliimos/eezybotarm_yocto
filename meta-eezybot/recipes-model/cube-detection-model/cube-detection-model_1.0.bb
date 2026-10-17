SUMMARY = "Pre-compiled TensorRT engine for EezyBotArm cube detection"
DESCRIPTION = "Installs the pre-compiled TensorRT FP16 engine (model.engine) \
into /usr/share/models/ for the EezyBotArm cube detection vision node."

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "${@'file://model.engine' if os.path.isfile(os.path.join(d.getVar('THISDIR'), 'files', 'model.engine')) else ''}"

S = "${UNPACKDIR}"

# Binary TensorRT engine plan is architecture-specific and pre-compiled on target GPU
INSANE_SKIP:${PN} = "already-stripped arch"
ALLOW_EMPTY:${PN} = "1"

python do_warn_missing_engine() {
    import os
    engine_path = os.path.join(d.getVar('THISDIR'), 'files', 'model.engine')
    if not os.path.isfile(engine_path):
        bb.warn("model.engine not found in meta-eezybot/recipes-model/cube-detection-model/files/. "
                "Boot the dev image on Jetson and run: "
                "python3 scripts/prepare_yolo_model.py --model <weights.pt> --jetson root@<ip>")
}
addtask warn_missing_engine before do_install

do_install() {
    install -d ${D}/usr/share/models
    if [ -f "${UNPACKDIR}/model.engine" ]; then
        install -m 0644 ${UNPACKDIR}/model.engine ${D}/usr/share/models/model.engine
    fi
}

FILES:${PN} = "/usr/share/models"
