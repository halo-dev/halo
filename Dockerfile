FROM ibm-semeru-runtimes:open-17-jre as builder

# Fix multiplatform build memory issues
# https://github.com/docker/build-push-action/issues/621#issuecomment-1383624173
ENV CARGO_NET_GIT_FETCH_WITH_CLI=true

WORKDIR application
ARG JAR_FILE=application/build/libs/*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

################################

FROM ibm-semeru-runtimes:open-17-jre
MAINTAINER johnniang <johnniang@fastmail.com>
WORKDIR application
COPY --from=builder application/dependencies/ ./
COPY --from=builder application/spring-boot-loader/ ./
COPY --from=builder application/snapshot-dependencies/ ./
COPY --from=builder application/application/ ./

ENV JVM_OPTS="-Xmx256m -Xms256m" \
    HALO_WORK_DIR="/root/.halo2" \
    SPRING_CONFIG_LOCATION="optional:classpath:/;optional:file:/root/.halo2/" \
    TZ=Asia/Shanghai \
    # Fix multiplatform build memory issues
    # https://github.com/docker/build-push-action/issues/621#issuecomment-1383624173
    CARGO_NET_GIT_FETCH_WITH_CLI=true

RUN ln -sf /usr/share/zoneinfo/$TZ /etc/localtime \
    && echo $TZ > /etc/timezone

ENTRYPOINT ["sh", "-c", "java ${JVM_OPTS} org.springframework.boot.loader.launch.JarLauncher ${0} ${@}"]
