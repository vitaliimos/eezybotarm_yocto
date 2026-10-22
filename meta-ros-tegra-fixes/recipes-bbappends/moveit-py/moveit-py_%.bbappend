# moveit-py (superflore-generated recipe) is pulled in by eezybotarm-control for
# the MoveIt 2 Python bindings, and it cannot configure in a Yocto build:
# find_package(Bullet) has to happen before moveit_core's exported targets are
# loaded. See the patch for the full explanation.
FILESEXTRAPATHS:prepend := "${THISDIR}:"

SRC_URI += "\
    file://0001-find-bullet-before-moveit-core.patch \
"

# pybind11_add_module() calls pybind11_strip(), which strips the CPython
# extension modules in the build tree because the cross toolchain file sets
# CMAKE_STRIP. Yocto's own do_package strip pass then reports them as
# already-stripped, so no separate -dbg data is produced for them.
INSANE_SKIP:${PN} += "already-stripped"

# moveit_py_utils is built with a so-name, so the non -dev package ends up with a
# libmoveit_py_utils.so symlink.
inherit ros_insane_dev_so

do_install:append() {
    # QA Issue: File /opt/ros/jazzy/share/moveit_py/cmake/moveit_py_utilsTargetsExport.cmake
    # in package moveit-py-dev contains reference to TMPDIR [buildpaths]
    sed -i -e "s#${RECIPE_SYSROOT}${includedir}##g" \
        ${D}${ros_prefix}/share/moveit_py/cmake/moveit_py_utilsTargetsExport.cmake

    # pybind11 cross-compilation may produce modules with host triplet suffix (x86_64-linux-gnu).
    # Rename them to target architecture suffix (aarch64-linux-gnu) so Python can import them.
    for f in $(find ${D} -name "*.cpython-*-x86_64-linux-gnu.so"); do
        target_f=$(echo "$f" | sed 's/x86_64-linux-gnu/aarch64-linux-gnu/')
        mv "$f" "$target_f"
    done
}
