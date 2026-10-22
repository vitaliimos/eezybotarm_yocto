inherit cuda

EXTRA_OECMAKE:append:cuda = " \
    -DCUDAToolkit_ROOT=${STAGING_DIR_HOST}/usr/local/cuda-${CUDA_VERSION} \
"

do_install:append() {
    sed -i '1i\
if(NOT DEFINED CUDAToolkit_FOUND)\
  set(CUDAToolkit_FOUND TRUE)\
  set(CUDAToolkit_VERSION "13.2.86")\
  set(CUDA_VERSION_STRING "13.2.86")\
endif()\
if(NOT TARGET CUDA::cudart)\
  add_library(CUDA::cudart INTERFACE IMPORTED)\
endif()\
' ${D}${ros_prefix}/share/cv_bridge/cmake/cv_bridgeConfig.cmake
}
