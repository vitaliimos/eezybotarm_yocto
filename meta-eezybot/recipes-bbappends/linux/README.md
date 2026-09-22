# Linux Kernel bbappend (`linux-noble-nvidia-tegra`)

## Purpose

Enables kernel probe (`kprobes`) support in the NVIDIA Tegra Linux kernel for Jetson Orin platforms.

## Background & Rationale

- **Dependency Chain:** The build includes ROS 2 and MoveIt 2. MoveIt 2 relies on `tracetools` for instrumentation, which triggers the following dependency chain:
  ```
  tracetools -> lttng-tools -> lttng-modules
  ```
- **Build Failure:** `lttng-modules` (version 2.14+) requires `CONFIG_KPROBES` on Linux kernels $\ge$ 5.7.0. Without it, the build fails with:
  ```
  src/wrapper/kallsyms.c:20: #error "LTTng-modules requires CONFIG_KPROBES on kernels >= 5.7.0"
  ```
- **Default NVIDIA Configuration:** The default `defconfig` for the NVIDIA Tegra 6.8 kernel used for Orin has `# CONFIG_KPROBES is not set`, although prerequisites (`CONFIG_HAVE_KPROBES`, `CONFIG_TRACEPOINTS`, and `CONFIG_FTRACE`) are already enabled.

## Applied Changes

The configuration fragment [`kprobes.cfg`](./kprobes.cfg) is added to `SRC_URI` in [`linux-noble-nvidia-tegra_%.bbappend`](./linux-noble-nvidia-tegra_%.bbappend):

```ini
CONFIG_KPROBES=y
CONFIG_KPROBE_EVENTS=y
```

This allows `lttng-modules` to compile cleanly without needing to strip out LTTng tracing or modify the ROS 2 MoveIt 2 dependency graph.
