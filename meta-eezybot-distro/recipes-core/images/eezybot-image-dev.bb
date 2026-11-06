SUMMARY = "EezyBot dev image - production plus development, Python, and debugging tooling"
DESCRIPTION = "Development image for the EezyBot robot. Everything from the production \
image plus developer tools (Python pip/venv, text editors, i2c-tools, gdb, Wi-Fi)."
LICENSE = "MIT"

require eezybot-image.bb

# Pull in dev packages (python, editors, i2c-tools, debugging tools)
IMAGE_INSTALL += "packagegroup-eezybot-dev"