# lttng-modules reaches the image through the ROS 2 dependency chain
# tracetools -> lttng-tools -> lttng-modules (tracetools is an instrumented
# MoveIt 2 dependency), and lttng-modules 2.14 refuses to build against a
# kernel without kprobes:
#   src/wrapper/kallsyms.c:20: #error "LTTng-modules requires CONFIG_KPROBES
#   on kernels >= 5.7.0"
# The NVIDIA tegra 6.8 defconfig used for the Orin ships with
# "# CONFIG_KPROBES is not set", so enable kprobes (and the kprobe event
# overlay that comes with it) instead of dropping LTTng tracing from the
# image. CONFIG_HAVE_KPROBES, CONFIG_TRACEPOINTS and CONFIG_FTRACE are already
# enabled in that config.
FILESEXTRAPATHS:prepend := "${THISDIR}:"

SRC_URI += "file://kprobes.cfg"
