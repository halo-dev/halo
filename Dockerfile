FROM maven:3-jdk-8-alpine
LABEL maintainer="Ryan Wang<i@ryanc.cc>"

WORKDIR /opt/halo
ADD . /tmp
ENV TZ=Asia/Shanghai \
DB_USER="admin" \
DB_PASSWORD="123456"

RUN ln -snf /usr/share/zoneinfo/${TZ} /etc/localtime && echo ${TZ} > /etc/timezone

RUN cd /tmp && mvn package -Pci && mv target/dist/halo/* /opt/halo/ \
    && rm -rf /tmp/* && rm -rf ~/.m2

EXPOSE 8090

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/opt/halo/halo-latest.jar","--spring.datasource.username=${DB_USER}","--spring.datasource.password=${DB_PASSWORD}"]
