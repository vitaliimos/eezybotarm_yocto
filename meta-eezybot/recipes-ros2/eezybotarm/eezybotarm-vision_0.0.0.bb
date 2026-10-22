SUMMARY = "EezyBotArm computer vision and sorting coordinator"
DESCRIPTION = "Computer vision and sorting node for EezyBotArm using YOLO and zone homography."
HOMEPAGE = "https://github.com/vitaliimos/eeazybot_ros"
SECTION = "devel"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

inherit ros_distro_jazzy
inherit ros_superflore_generated
require eezybotarm-app.inc

ROS_BPN = "eezybotarm_vision"
ROS_BUILD_TYPE = "ament_python"

ROS_EXEC_DEPENDS = " \
    cuda-cudart \
    cv-bridge \
    eezybotarm-interfaces \
    geometry-msgs \
    image-publisher \
    python3-numpy \
    python3-onnxruntime \
    python3-opencv \
    python3-tensorrt \
    rclpy \
    sensor-msgs \
    std-msgs \
    std-srvs \
    v4l2-camera \
    visualization-msgs \
"

RDEPENDS:${PN} += " \
    python3 \
    ${ROS_EXEC_DEPENDS} \
    cube-detection-model \
"

inherit ros_${ROS_BUILD_TYPE}

# Do not ship training (.pt) or intermediate (.onnx) models from the ROS package
# in the target rootfs; the runtime uses /usr/share/models/model.engine from cube-detection-model.
do_install:append() {
    rm -rf ${D}${ros_datadir}/eezybotarm_vision/models
}
