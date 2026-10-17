SUMMARY = "EezyBotArm MoveIt 2 configuration and launch files"
DESCRIPTION = "MoveIt 2 (MoveIt Setup Assistant generated) configuration and launch files for \
the EezyBotArm: URDF/srdf, kinematics, controllers, OMPL planning pipeline, RViz config and \
the bringup/production/dev launch files."
HOMEPAGE = "https://github.com/vitaliimos/eeazybot_ros"
SECTION = "devel"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

inherit ros_distro_jazzy
inherit ros_superflore_generated
require eezybotarm-app.inc

ROS_BPN = "eezybotarm_moveit_config"
ROS_BUILD_TYPE = "ament_cmake"

ROS_BUILD_DEPENDS = ""
ROS_BUILDTOOL_DEPENDS = " \
    ament-cmake-native \
"
# Runtime deps are deliberately kept to what the launch files load directly;
# RViz/visualization stack is pulled in by the dev packagegroup only.
ROS_EXEC_DEPENDS = " \
    controller-manager \
    eezybotarm-vision \
    moveit-configs-utils \
    robot-state-publisher \
    tf2-ros \
    xacro \
"

DEPENDS = "${ROS_BUILD_DEPENDS} ${ROS_BUILDTOOL_DEPENDS}"
RDEPENDS:${PN} += "${ROS_EXEC_DEPENDS}"

inherit ros_${ROS_BUILD_TYPE}
