FROM adoptopenjdk/openjdk8-openj9:alpine

RUN apk update \
&& apk add curl bash tree tzdata\
&& cp -r -f /usr/share/zoneinfo/Asia/Shanghai /etc/localtime\
&& echo -ne "adoptopenjdk/openjdk8-openj9:jdk8u172-b11-nightly. (uname -rsv)\n" >> /root/.built

VOLUME /tmp

ARG JAR_FILE=build/libs/halo.jar
ARG PORT=8090

ENV LANG C.UTF-8
ENV JAVA_OPTS="-Xms256m -Xmx256m"

COPY ${JAR_FILE} halo.jar

EXPOSE ${PORT}

ENTRYPOINT java ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -server -jar halo.jar
