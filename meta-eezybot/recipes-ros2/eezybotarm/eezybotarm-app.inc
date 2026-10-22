# Build-time source definition for the EezyBotArm ROS 2 packages.

EEZYBOT_ROS_APP_GIT_URI ?= "git://git@github.com/vitaliimos/eeazybot_ros.git;protocol=ssh;branch=main;lfs=1"
EEZYBOT_ROS_APP_GIT_REV ?= "8623b1293d178d790c7490cccfca292fd273adca"

EEZYBOT_ROS_APP_PKG ?= "${ROS_BPN}"

SRC_URI = "${EEZYBOT_ROS_APP_GIT_URI}"
SRCREV = "${EEZYBOT_ROS_APP_GIT_REV}"
S = "${UNPACKDIR}/${BP}/src/${EEZYBOT_ROS_APP_PKG}"
