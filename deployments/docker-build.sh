#!/bin/bash

set -e

COMMAND="$1"
ARGS="${@:2}"

CURRENT_DIR="$(cd "$(dirname "$0")" && pwd)"

DOCKER_REGISTRY="registry.cn-hangzhou.aliyuncs.com"
DOCKER_LIBRARY="xblog"

RELEASE_VERSION="latest"

# ksp_server下的服务
SERVICE_LIST=(
  "halo"
)



# 构建镜像, 并上传到镜像仓库
function build() {
  local srv=$1

  IMAGE_LATEST="${DOCKER_REGISTRY}/${DOCKER_LIBRARY}/${srv}:${RELEASE_VERSION}"

  docker build -t "${IMAGE_LATEST}" -f "${CURRENT_DIR}/docker_file/${srv}.dockerfile" "${CURRENT_DIR}/.."
  docker push "${IMAGE_LATEST}"
}

function gradle_build() {
  cd ${CURRENT_DIR}/../
  ./gradlew build
}

# 默认构建全部镜像
if [ "${COMMAND}" = "" ]; then
  COMMAND="build"
fi



# 编译镜像,并推送到测试环境镜像仓库
if [ "${COMMAND}" = "build" ]; then
  gradle_build
  if [ "${ARGS}" = "" ]; then
    for srv in ${SERVICE_LIST[@]}; do
      build "${srv}"
    done
  else
    for srv in ${ARGS}; do
      build "${srv}"
    done
  fi

  exit 0
fi

echo "Command Not Found"
