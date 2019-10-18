FROM openjdk:8-jre

MAINTAINER bing_huang <1278032416@qq.com>

ENV GIT_URL=https://github.com/halo-dev/halo/releases/download \
BASE_DIR=/home/halo \
SERVER_PORT=8090 \
TIME_ZONE=Asia/Shanghai \
JVM_XMS=256m \
JVM_XMX=256m \
JVM_XMN=256m \
JVM_MS=128m \
JVM_MMS=320m 

ARG HALO_VERSION=1.1.1

WORKDIR $BASE_DIR


ADD ${GIT_URL}/v${HALO_VERSION}/halo-${HALO_VERSION}.jar ./

ADD bin/docker-entrypoint.sh bin/docker-entrypoint.sh
ADD conf/application.properties conf/application.properties

RUN chmod +x bin/docker-entrypoint.sh
RUN mv halo-${HALO_VERSION}.jar halo.jar

EXPOSE ${SERVER_PORT}

ENTRYPOINT ["bin/docker-entrypoint.sh"]