# EezyBot Yocto

Yocto/OpenEmbedded build for an NVIDIA Jetson device, configured with
[kas](https://kas.readthedocs.io/). Two images are provided:

| Image                 | Type       | Contents                                                            |
|-----------------------|------------|---------------------------------------------------------------------|
| `eezybot-image`      | production | minimal console image: ROS 2 (Jazzy), EezyBotArm (auto-start, headless), SSH |
| `eezybot-image-dev`  | development| production + dev tools (Python pip/venv, nano/vim, i2c-tools, gdb, Wi-Fi) |

Both are built from the `eezybot` distro defined in the local `meta-eezybot/` layer
on machine `jetson-orin-nano-devkit-nvme` (rootfs on the M.2 NVMe drive). To boot
from a microSD card instead, switch the machine to `jetson-orin-nano-devkit`
in `kas/base.yml`.

## Requirements

- Docker (or another OCI runtime supported by kas, e.g. podman)
- kas CLI `=5.x` installed (`pip install kas`, or available as `kas-container` with Docker)

## Build

Build the production or development image using the provided build scripts:

```sh
# production
./build.sh prod

# development
./build.sh dev
```

This is reproducible — all sources and layers are pinned via `kas/base.yml`
(optionally freeze dependencies with `kas lock kas/base.yml` to add commit-hash locks).
Network access is required on the first build (source fetching).

## Working with the images

### Credentials

All secrets live in `meta-eezybot/conf/credentials.inc` (gitignored; copy it from
`credentials.inc.template` and fill in the values):

- `EEZYBOT_WIFI_SSID` / `EEZYBOT_WIFI_PASSWORD` — Wi-Fi network (enables
  `wpa_supplicant@wlan0` on the dev image; see [Networking](#networking-ethernet--wi-fi))
- `EEZYBOT_SSH_PUBLIC_KEYS` — SSH public key(s), installed to
  `/root/.ssh/authorized_keys`. SSH is **key-only** — OpenSSH's default
  `PermitRootLogin=prohibit-password` is kept, so this is the only way to log
  in over the network. Separate multiple keys with `\n`.

If the file is absent the build still succeeds, but the image has no SSH public
key (no network login), no VNC password and no Wi-Fi config. For CI/production you can
instead override the variables via a `kas/*secrets*.yml` `local_conf_header`
or the `env:` mapping (see [Cache / reusability](#cache--reusability)).

### Logging in (SSH, port 22)

```sh
ssh root@<device-ip>      # key-only; the public key must be in EEZYBOT_SSH_PUBLIC_KEYS
```

### Networking (ethernet + Wi-Fi)

`systemd-networkd` (oe-core) manages interfaces and provides DHCP. Ethernet
("wired") uses the stock `80-wired.network`; on the dev image the Wi-Fi is
provisioned out-of-the-box when credentials are set: the distro enables the
stock `wpa_supplicant@wlan0.service` instance and the image postprocess bakes
`/etc/wpa_supplicant/wpa_supplicant-wlan0.conf` and
`/usr/lib/systemd/network/25-wireless.network` from `EEZYBOT_WIFI_SSID`
/`EEZYBOT_WIFI_PASSWORD`. So a dev image on Wi-Fi shows up with two IPs
(eth0 + wlan0); both serve SSH/VNC.

### EezyBotArm application

Every image auto-starts the EezyBotArm ROS 2 stack at boot
(`eezybotarm.service`): `ros2_control` drives the PCA9685 hardware interface
(real hardware, `/dev/i2c-1`/`0x40` by default), `move_group` provides MoveIt 2
planning (OMPL), and the `pick_and_place_server` action server accepts pick-place
requests. The service sources the Jazzy environment and launches one of the MoveIt
config launch files:

| Image      | Launch file          | RViz |
|------------|----------------------|------|
| production | `production.launch.py` | off  |
| dev        | `dev.launch.py`       | host-side only |

RViz 2 is deliberately not built for the target (Qt5 + OGRE vendor is a
multi-hour build with unproven llvmpipe rendering on the Orin's Xvfb), so the
dev image — like production — runs the stack headless via
`/etc/default/eezybotarm`, which the image bakes at build time and can be
edited on the device to override at runtime (e.g.
`EEZYBOTARM_LAUNCH=production.launch.py`, or extra args such as
`EEZYBOTARM_ARGS="i2c_bus:=/dev/i2c-2"`).

```sh
# on the device
systemctl status eezybotarm
journalctl -u eezybotarm -f

# interact with the stack
source /opt/ros/jazzy/setup.bash
ros2 action list
ros2 topic echo /joint_states --once
ros2 control list_controllers
```

### Dev image: host-side RViz

To visualize with RViz 2 without building it for the target, install ROS 2 Jazzy
(non-ROS packages) on a workstation on the same network, or just install the
`rviz2` Debian:

```sh
# on your workstation
export ROS_DOMAIN_ID=42                          # must match the one on the robot
source /opt/ros/jazzy/setup.bash
DISPLAY=:0 ros2 launch eezybotarm_moveit_config dev.launch.py use_rviz:=true
```

The launch (brought over from the same `eeazybot_ros` repo your host clones, or
run through the robot) starts the MoveIt launch on the workstation while the
robot's `move_group`/`ros2_control` nodes run on the target; Fast DDS discovery
links the two over the network, so RViz shows the live arm state
(`/tf`, `/joint_states`, `/monitored_planning_scene`). No Qt/OGRE is built for
the arm, and rendering uses your workstation's GPU.

The five `eezybotarm_*` packages are built from the `vitaliimos/eeazybot_ros`
Git repository at build time; their URL/revision and recipe definitions live in
`meta-eezybot/recipes-ros2/eezybotarm/`.

### Dev image: Development & Debugging tooling

The development image is headless and optimized for remote development over SSH (e.g. using VS Code Remote - SSH, Zed, or terminal editors):

- **Python Development:** Includes `python3`, `python3-pip`, and `python3-venv` for installing dependencies and running scripts.
- **Code Editing:** `nano` and `vim` are included directly on the target for quick adjustments.
- **Hardware Debugging:** `i2c-tools` (`i2cdetect`, `i2cget`, `i2cset`, `i2cdump`) for inspecting and troubleshooting connected I2C devices (such as the PCA9685 servo driver).
- **Diagnostics:** `gdb`, `strace`, `tcpdump`, `v4l-utils` for debugging ROS nodes, networks, and video streams.

## Flashing to the device

Each image build produces a self-contained flashing package
(`*.rootfs-*.tegraflash-tar.zst`) in
`build/tmp/deploy/images/jetson-orin-nano-devkit-nvme/`. It bundles the boot
firmware, kernel, device trees and the rootfs (`*.ext4`), plus the
`initrd-flash` script and `tegraflash.py` from meta-tegra; no NVIDIA
`Linux_for_Tegra` kit is needed. The process is documented in
[`meta-tegra/docs/Flashing.md`](meta-tegra/docs/Flashing.md).

> The flash package is generated for the machine it was built with. After
> switching `machine:` in `kas/base.yml` (e.g. to `jetson-orin-nano-devkit-nvme`),
> rebuild the image so the package is regenerated for that layout — the package's
> `.env.initrd-flash` must say `ROOTFS_DEVICE="nvme0n1"` and
> `EXTERNAL_ROOTFS_DRIVE=1` for the NVMe flow. Note the NVMe layout flips
> `initrd-flash` into the "external storage" path (Step 4 writes to
> `external-flash.xml.in`).

### Hardware

- A Jetson Orin Nano Developer Kit with an **M.2 NVMe drive** populated in the
  M.2 Key M socket (the bootloader and rootfs are written to QSPI + NVMe). The
  microSD slot is not used by this configuration.

### Host prerequisites

```sh
sudo apt install device-tree-compiler gcc gdisk udisks2 bmap-tools   # dtc, cpp, sgdisk, udisksctl, bmaptool
```

Disable automatic mounting of removable media in your desktop settings so the
flashing scripts can manage the NVMe (or SD card) drive themselves (on GNOME:
`gsettings set org.gnome.desktop.media-handling automount false`).

### Steps

The `flash-jetson.sh` script automates this: it finds the newest flash package,
verifies the host tools and that the device is in recovery mode, unpacks the
package into the (gitignored) `flash/` directory and runs `initrd-flash`.

```sh
# put the device in recovery mode first
#    power off -> hold the Recovery button -> connect the USB-C OTG port to
#    your host -> power on while still holding Recovery
lsusb -d 0955:     # a NVIDIA device must show up

# first flash: writes boot firmware (QSPI) + rootfs to the NVMe drive
./flash-jetson.sh --with-bootloader

# subsequent flashes: skip the slow boot-firmware write (kernel + rootfs only)
./flash-jetson.sh
```

The script defaults to `--external-only`, which skips the QSPI boot-firmware
write. Pass `--with-bootloader` for the very first flash (or to reflash the
boot firmware), `--erase-nvme` to wipe the NVMe drive while flashing, and
`-D` for debug logging. To flash a specific build, give it the path to a
`.rootfs-*.tegraflash-tar.zst` file; otherwise it uses the newest one.

If you prefer to run the flashing by hand:

```sh
# 1. unpack the flash package into an empty directory
mkdir -p ~/flash && cd ~/flash
tar --zstd -xf build/tmp/deploy/images/jetson-orin-nano-devkit-nvme/eezybot-image-dev-jetson-orin-nano-devkit-nvme.rootfs-*.tegraflash-tar.zst
```

```sh
# 2. put the device in recovery mode (as above)
# 3. first flash: writes boot firmware (QSPI) + rootfs to the NVMe drive
./initrd-flash

#    after the first flash, skip the boot-firmware write for updates:
./initrd-flash --external-only
```

The device boots and is reachable as documented below (`ssh root@<device-ip>`
and, on the dev image, VNC on port 5900). A serial console on the dev kit's
button-header UART is helpful when debugging boot issues.

## Output

Images and flash tooling are produced under `build/tmp/deploy/images/<machine>`,
e.g. `eezybot-image-dev-jetson-orin-nano-devkit-nvme.rootfs.tegraflash-tar.zst`
(see [Flashing to the device](#flashing-to-the-device)). The image includes
kernel, device trees, boot firmware and rootfs.

## Cache / reusability

`build/` is generated and gitignored; keep it on your machine to reuse:

- `build/downloads/` — fetched sources
- `build/sstate-cache/` — compiled artifact cache
- `build/tmp/` — work dirs

Set `TMPDIR`, `DL_DIR`, `SSTATE_DIR` in `local_conf_header` in `kas/base.yml`
to redirect these (e.g. to a shared volume for CI).

If `bblayers.conf`/`local.conf` change or an old non-kas `build/` exists,
delete `build/` and rebuild — kas regenerates the config every run.

## Useful commands

```sh
./build.sh prod                                         # production image
./build.sh dev                                          # dev image (VNC desktop)
./build.sh shell                                        # interactive shell in build env
kas-container dump kas/base.yml:kas/dev.yml               # show merged config
kas lock kas/base.yml                                   # pin repos to commits
```
