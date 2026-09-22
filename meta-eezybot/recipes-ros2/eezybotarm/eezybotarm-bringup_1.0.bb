SUMMARY = "EezyBotArm ROS 2 boot service"
DESCRIPTION = "Assembles the EezyBotArm runtime and starts it at boot via systemd \
(eezybot-arm.service). Launches production.launch.py headless by default; the dev image \
switches to dev.launch.py (RViz enabled) via /etc/default/eezybot-arm."
SECTION = "devel"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = " \
    file://eezybot-arm.service \
"

S = "${UNPACKDIR}"

inherit systemd

# The MoveIt 2/ros2_control runtime that brings up the real arm: controller
# manager + spawners, move_group, planning pipeline and the pick-and-place
# server. RViz/visualization is intentionally NOT here (dev image only).
RDEPENDS:${PN} = " \
    controller-manager \
    eezybotarm-control \
    eezybotarm-description \
    eezybotarm-hardware \
    eezybotarm-interfaces \
    eezybotarm-moveit-config \
    joint-state-broadcaster \
    joint-trajectory-controller \
    moveit-configs-utils \
    moveit-kinematics \
    moveit-planners-ompl \
    moveit-py \
    moveit-ros-move-group \
    moveit-simple-controller-manager \
    robot-state-publisher \
    ros2-control \
    tf2-ros \
    warehouse-ros-sqlite \
    xacro \
"

do_install() {
    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${S}/eezybot-arm.service ${D}${systemd_system_unitdir}/eezybot-arm.service
}

SYSTEMD_SERVICE:${PN} = "eezybot-arm.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"
