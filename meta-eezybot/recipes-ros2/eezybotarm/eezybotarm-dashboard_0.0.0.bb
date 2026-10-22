SUMMARY = "EezyBotArm NiceGUI web dashboard"
DESCRIPTION = "Web dashboard node for monitoring and controlling EezyBotArm via browser."
HOMEPAGE = "https://github.com/vitaliimos/eeazybot_ros"
SECTION = "devel"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

inherit ros_distro_jazzy
inherit ros_superflore_generated
inherit systemd
require eezybotarm-app.inc

SRC_URI:append = " file://eezybot-dashboard.service"

ROS_BPN = "eezybotarm_dashboard"
ROS_BUILD_TYPE = "ament_python"

ROS_EXEC_DEPENDS = " \
    cv-bridge \
    eezybotarm-description \
    eezybotarm-interfaces \
    geometry-msgs \
    python3-nicegui \
    python3-numpy \
    python3-opencv \
    rclpy \
    sensor-msgs \
    tf2-ros \
    trajectory-msgs \
"

RDEPENDS:${PN} += "python3 ${ROS_EXEC_DEPENDS}"

do_install:append() {
    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${UNPACKDIR}/eezybot-dashboard.service ${D}${systemd_system_unitdir}/eezybot-dashboard.service
}

SYSTEMD_SERVICE:${PN} = "eezybot-dashboard.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

inherit ros_${ROS_BUILD_TYPE}
