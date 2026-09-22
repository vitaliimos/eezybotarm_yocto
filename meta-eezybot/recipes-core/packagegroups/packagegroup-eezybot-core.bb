SUMMARY = "EezyBot core runtime: ROS 2 (Jazzy) + EezyBotArm service + networking"
DESCRIPTION = "Packagegroup shared by all EezyBot images. Installs the ROS 2 (Jazzy) \
runtime and the EezyBotArm application (built from the private eeazybot_ros Git \
repository) which is auto-started at boot via its eezybot-arm.service unit. Also \
installs the Jetson Orin Nano devkit NIC drivers (Realtek RTL8168 Ethernet, Realtek \
RTL8822CE Wi-Fi) that are otherwise only weakly recommended by meta-tegra and can be \
silently dropped from the rootfs."
LICENSE = "MIT"

inherit packagegroup

# ros-core aggregates the ROS 2 runtime; eezybotarm-bringup assembles and starts
# the EezyBotArm at boot (MoveIt 2 + ros2_control + pick-and-place server).
RDEPENDS:${PN} = " \
    ros-core \
    eezybotarm-bringup \
"

# Jetson Orin Nano devkit NIC drivers + firmware. Installed explicitly (rather
# than relying on MACHINE_EXTRA_RRECOMMENDS) so eth0/wlan0 always exist.
RDEPENDS:${PN} += " \
    nv-kernel-module-r8168 \
    linux-firmware-rtl8168 \
    nv-kernel-module-rtl8822ce \
    tegra-firmware-rtl8822 \
"
