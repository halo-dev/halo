FROM openjdk:8-jdk-alpine
LABEL maintainer="Ryan Wang<i@ryanc.cc>"

WORKDIR /opt/halo
ADD . /tmp
ENV TZ=Asia/Shanghai \
DB_USER="admin" \
DB_PASSWORD="123456"

RUN ln -snf /usr/share/zoneinfo/${TZ} /etc/localtime && echo ${TZ} > /etc/timezone

RUN cd /tmp && ./gradlew bootjar && mv build/libs/* /opt/halo/ \
    && rm -rf /tmp/* && rm -rf ~/.gradle

EXPOSE 8090

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Dlogging.level.run.halo.app=INFO", "-jar","/opt/halo/halo-1.0.0.jar","--spring.datasource.username=${DB_USER}","--spring.datasource.password=${DB_PASSWORD}"]
