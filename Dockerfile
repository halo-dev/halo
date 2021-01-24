FROM adoptopenjdk/openjdk11-openj9
VOLUME /tmp

ARG JAR_FILE=build/libs/*.jar
ARG TIME_ZONE=Asia/Shanghai

ENV JVM_XMS="256m" \
    JVM_XMX="256m" \
    JVM_OPTS="" \
    TZ=${TIME_ZONE}


COPY ${JAR_FILE} halo.jar

EXPOSE 8090

ENTRYPOINT java -Xms${JVM_XMS} -Xmx${JVM_XMX} ${JVM_OPTS} -Djava.security.egd=file:/dev/./urandom -server -jar halo.jar