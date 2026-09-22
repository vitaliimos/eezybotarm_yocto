SUMMARY = "EezyBotArm ros2_control hardware interface (PCA9685 I2C)"
DESCRIPTION = "ros2_control SystemInterface hardware plugin driving the PCA9685 servo \
controller over I2C (/dev/i2c-1, address 0x40) for the EezyBotArm."
HOMEPAGE = "https://github.com/vitaliimos/eeazybot_ros"
SECTION = "devel"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

inherit ros_distro_jazzy
inherit ros_superflore_generated
require eezybotarm-app.inc

ROS_BPN = "eezybotarm_hardware"
ROS_BUILD_TYPE = "ament_cmake"

ROS_BUILD_DEPENDS = " \
    hardware-interface \
    pluginlib \
    rclcpp \
    rclcpp-lifecycle \
"
ROS_BUILDTOOL_DEPENDS = " \
    ament-cmake-native \
"
ROS_EXEC_DEPENDS = " \
    hardware-interface \
    pluginlib \
    rclcpp \
    rclcpp-lifecycle \
"

DEPENDS = "${ROS_BUILD_DEPENDS} ${ROS_BUILDTOOL_DEPENDS}"
RDEPENDS:${PN} += "${ROS_EXEC_DEPENDS}"

inherit ros_${ROS_BUILD_TYPE}
