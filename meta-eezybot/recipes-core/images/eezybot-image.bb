SUMMARY = "EezyBot production image - minimal console image with ROS 2 (Jazzy) runtime and EezyBotArm"
DESCRIPTION = "Minimal production image for the EezyBot robot. Boots to a console, \
contains the ROS 2 (Jazzy) runtime and the EezyBotArm application (MoveIt 2 + \
ros2_control + pick-and-place server, auto-started headless via systemd) and an SSH \
server for out-of-band access."
LICENSE = "MIT"

inherit core-image

IMAGE_FEATURES += "ssh-server-openssh"

# Assign IMAGE_INSTALL directly rather than routing through
# CORE_IMAGE_EXTRA_INSTALL. The indirection only works by accident here:
# VAR = "..." stores the string unexpanded and resolves it on read, so the
# ${CORE_IMAGE_EXTRA_INSTALL} below happens to be picked up. That accident does
# not survive the dev image, which sets CORE_IMAGE_EXTRA_INSTALL from a *different*
# file after requiring this one - see eezybot-image-dev.bb. Setting IMAGE_INSTALL
# directly makes the package list explicit and behaves the same in both images.
# CORE_IMAGE_EXTRA_INSTALL is also documented as local.conf-only
# (meta/conf/documentation.conf). Assigning IMAGE_INSTALL here also overrides
# the core-image class default, so packagegroup-base-extended stays out and the
# production image remains minimal.
IMAGE_INSTALL = "packagegroup-core-boot"
IMAGE_INSTALL += "packagegroup-eezybot-core"

# WiFi on the dev image is enabled out-of-the-box: the stock
# wpa_supplicant@.service instance (see distro conf) reads
# /etc/wpa_supplicant/wpa_supplicant-wlan0.conf and systemd-networkd gets DHCP
# for wireless interfaces from /usr/lib/systemd/network/25-wireless.network.
# Both files are baked below; the SSID/psk network block is added when
# EEZYBOT_WIFI_SSID/PASSWORD are provided.

# Credentials come from the gitignored credentials.inc (or weak overrides such
# as kas local_conf_header). SSH is key-only: EEZYBOT_SSH_PUBLIC_KEYS is written
# to /root/.ssh/authorized_keys in the image postprocess, and OpenSSH's default
# PermitRootLogin=prohibit-password is exactly what we want - no password ever
# reaches /etc/shadow, so there is no extrausers machinery here.
# EEZYBOT_ROOT_PASSWORD is consumed by the dev image only, which bakes it into
# the VNC password file.
include conf/credentials.inc

EEZYBOT_ROOT_PASSWORD ?= ""
EEZYBOT_SSH_PUBLIC_KEYS ?= ""
EEZYBOT_WIFI_SSID ?= ""
EEZYBOT_WIFI_PASSWORD ?= ""

python warn_missing_ssh_keys() {
    if not (d.getVar('EEZYBOT_SSH_PUBLIC_KEYS', False) or '').strip():
        bb.warn('EEZYBOT_SSH_PUBLIC_KEYS is unset - the image will have no '
                'way to log in over SSH. Create meta-eezybot/conf/credentials.inc '
                'from credentials.inc.template.')
}

python configure_ssh_keys() {
    import os

    rootfs = d.getVar('IMAGE_ROOTFS')

    keys = (d.getVar('EEZYBOT_SSH_PUBLIC_KEYS', False) or '').strip()
    if keys:
        key_list = [k for k in keys.replace('\\n', '\n').split('\n') if k.strip()]
        auth_dir = os.path.join(rootfs, 'root', '.ssh')
        os.makedirs(auth_dir, exist_ok=True)
        auth_file = os.path.join(auth_dir, 'authorized_keys')
        with open(auth_file, 'w') as f:
            f.write('\n'.join(key_list) + '\n')
        os.chmod(auth_file, 0o600)
        os.chmod(auth_dir, 0o700)
}

python configure_wifi() {
    import os

    rootfs = d.getVar('IMAGE_ROOTFS')

    # Always write the config so the (distro-enababled) wpa_supplicant@wlan0
    # instance starts cleanly; the network block is only added when creds exist.
    wpa_dir = os.path.join(rootfs, 'etc', 'wpa_supplicant')
    os.makedirs(wpa_dir, exist_ok=True)
    wpa_conf = os.path.join(wpa_dir, 'wpa_supplicant-wlan0.conf')
    with open(wpa_conf, 'w') as f:
        f.write('ctrl_interface=/var/run/wpa_supplicant\n'
                'update_config=1\n')
        ssid = (d.getVar('EEZYBOT_WIFI_SSID', False) or '').strip()
        if ssid:
            psk = (d.getVar('EEZYBOT_WIFI_PASSWORD', False) or '').strip()
            f.write('\n'
                    'network={\n'
                    '    ssid="%s"\n'
                    '    psk="%s"\n'
                    '}\n' % (ssid, psk))
    os.chmod(wpa_conf, 0o600)

    net_dir = os.path.join(rootfs, 'usr', 'lib', 'systemd', 'network')
    os.makedirs(net_dir, exist_ok=True)
    network_file = os.path.join(net_dir, '25-wireless.network')
    with open(network_file, 'w') as f:
        f.write('[Match]\n'
                'Type=wlan\n\n'
                '[Network]\n'
                'DHCP=yes\n'
                'IgnoreCarrierLoss=3s\n')
}

ROOTFS_POSTPROCESS_COMMAND += "configure_ssh_keys; configure_wifi;"

# Make do_rootfs re-run whenever the credentials change (reads via d.getVar()
# are otherwise not part of the task hash).
do_rootfs[vardeps] += "EEZYBOT_SSH_PUBLIC_KEYS EEZYBOT_WIFI_SSID EEZYBOT_WIFI_PASSWORD"