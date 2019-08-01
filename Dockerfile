FROM openjdk:8-jre-alpine

VOLUME /tmp

ARG JAR_FILE=build/libs/halo.jar
ARG PORT=8090
ARG TIME_ZONE=Asia/Shanghai

ENV TZ=${TIME_ZONE}

COPY ${JAR_FILE} halo.jar

EXPOSE ${PORT}

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","halo.jar"]