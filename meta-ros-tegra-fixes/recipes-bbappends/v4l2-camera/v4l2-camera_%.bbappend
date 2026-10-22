# The fix_warning.patch from meta-ros2-jazzy was created for v4l2-camera 0.7.0.
# In v4l2-camera 0.7.3+, <cv_bridge/cv_bridge.hpp> is already included upstream,
# causing fix_warning.patch to fail to apply.
SRC_URI:remove = "file://fix_warning.patch"
