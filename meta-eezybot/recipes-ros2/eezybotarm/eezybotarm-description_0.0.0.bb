SUMMARY = "EezyBotArm robot description (URDF/xacro)"
DESCRIPTION = "EezyBotArm kinematic description: URDF xacro, meshes and launch helpers for \
the 6-DOF arm."
HOMEPAGE = "https://github.com/vitaliimos/eeazybot_ros"
SECTION = "devel"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

inherit ros_distro_jazzy
inherit ros_superflore_generated
require eezybotarm-app.inc

ROS_BPN = "eezybotarm_description"
ROS_BUILD_TYPE = "ament_cmake"

ROS_BUILD_DEPENDS = ""
ROS_BUILDTOOL_DEPENDS = " \
    ament-cmake-native \
"
ROS_EXEC_DEPENDS = " \
    robot-state-publisher \
    xacro \
"

DEPENDS = "${ROS_BUILD_DEPENDS} ${ROS_BUILDTOOL_DEPENDS}"
RDEPENDS:${PN} += "${ROS_EXEC_DEPENDS}"

inherit ros_${ROS_BUILD_TYPE}
