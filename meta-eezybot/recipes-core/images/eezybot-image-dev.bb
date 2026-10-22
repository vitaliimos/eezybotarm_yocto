SUMMARY = "EezyBot dev image - production plus development, Python, and debugging tooling"
DESCRIPTION = "Development image for the EezyBot robot. Everything from the production \
image plus developer tools (Python pip/venv, text editors, i2c-tools, gdb, Wi-Fi)."
LICENSE = "MIT"

require eezybot-image.bb

# Pull in dev packages (python, editors, i2c-tools, debugging tools)
IMAGE_INSTALL += "packagegroup-eezybot-dev"

# Dev image boots jetson.launch.py with profile=dev (ONNX runtime model) via
# /etc/default/eezybotarm.
python configure_eezybotarm() {
    import os

    rootfs = d.getVar('IMAGE_ROOTFS')
    defaults = os.path.join(rootfs, 'etc', 'default')
    os.makedirs(defaults, exist_ok=True)
    with open(os.path.join(defaults, 'eezybotarm'), 'w') as f:
        f.write('# Dev image: run jetson.launch.py with dev profile (uses model.onnx).\n'
                'EEZYBOTARM_PROFILE=dev\n')
}

ROOTFS_POSTPROCESS_COMMAND += "configure_eezybotarm;"