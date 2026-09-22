SUMMARY = "EezyBotArm ROS 2 message and action interfaces"
DESCRIPTION = "ROS 2 interfaces (messages/actions) used by the EezyBotArm pick-and-place \
stack (rosidl package)."
HOMEPAGE = "https://github.com/vitaliimos/eeazybot_ros"
SECTION = "devel"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

inherit ros_distro_jazzy
inherit ros_superflore_generated
require eezybotarm-app.inc

ROS_BPN = "eezybotarm_interfaces"
ROS_BUILD_TYPE = "ament_cmake"

ROS_BUILD_DEPENDS = " \
    action-msgs \
    geometry-msgs \
"
ROS_BUILDTOOL_DEPENDS = " \
    ament-cmake-native \
    rosidl-default-generators-native \
"
ROS_EXEC_DEPENDS = " \
    action-msgs \
    geometry-msgs \
    rosidl-default-runtime \
"

DEPENDS = "${ROS_BUILD_DEPENDS} ${ROS_BUILDTOOL_DEPENDS}"
RDEPENDS:${PN} += "${ROS_EXEC_DEPENDS}"

inherit ros_${ROS_BUILD_TYPE}
