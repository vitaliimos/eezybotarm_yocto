SUMMARY = "EezyBotArm pick-and-place action server"
DESCRIPTION = "Python pick-and-place server for the EezyBotArm built on MoveIt 2 (moveit_py), \
exposing a ROS 2 action server (pick_and_place_server)."
HOMEPAGE = "https://github.com/vitaliimos/eeazybot_ros"
SECTION = "devel"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

inherit ros_distro_jazzy
inherit ros_superflore_generated
require eezybotarm-app.inc

ROS_BPN = "eezybotarm_control"
ROS_BUILD_TYPE = "ament_python"

ROS_EXEC_DEPENDS = " \
    eezybotarm-interfaces \
    geometry-msgs \
    moveit-msgs \
    moveit-py \
    rclpy \
    shape-msgs \
"

RDEPENDS:${PN} += "python3 ${ROS_EXEC_DEPENDS}"

inherit ros_${ROS_BUILD_TYPE}
