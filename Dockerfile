FROM openjdk:8-jre-alpine

VOLUME /tmp

ARG JAR_FILE=build/libs/halo.jar
ARG PORT=8090
ARG TIME_ZONE=Asia/Shanghai

ENV TZ=${TIME_ZONE}
ENV JVM_XMS="256m"
ENV JVM_XMX="256m"

COPY ${JAR_FILE} halo.jar

EXPOSE ${PORT}

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-server","-jar","-Xms${JVM_XMS}","-Xmx${JVM_XMX}","halo.jar"]