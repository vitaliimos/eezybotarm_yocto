SUMMARY = "EezyBot dev image - production plus development, Python, and debugging tooling"
DESCRIPTION = "Development image for the EezyBot robot. Everything from the production \
image plus developer tools (Python pip/venv, text editors, i2c-tools, gdb, Wi-Fi)."
LICENSE = "MIT"

require eezybot-image.bb

# Pull in dev packages (python, editors, i2c-tools, debugging tools)
IMAGE_INSTALL += "packagegroup-eezybot-dev"

# Dev image boots the dev.launch.py launch headless (same service defaults as
# production apart from the launch file). RViz is NOT enabled on the target; the
# developer runs RViz on their workstation against the robot's topics over DDS
# (see packagegroup-eezybot-dev and README "Dev image: host-side RViz").
python configure_eezybot_arm() {
    import os

    rootfs = d.getVar('IMAGE_ROOTFS')
    defaults = os.path.join(rootfs, 'etc', 'default')
    os.makedirs(defaults, exist_ok=True)
    with open(os.path.join(defaults, 'eezybot-arm'), 'w') as f:
        f.write('# Dev image: launch the dev.launch.py, headless (RViz runs on\n'
                '# the developer workstation, not on the target).\n'
                'EEZYBOT_ARM_LAUNCH=dev.launch.py\n'
                'EEZYBOT_ARM_USE_RVIZ=0\n')
}

ROOTFS_POSTPROCESS_COMMAND += "configure_eezybot_arm;"