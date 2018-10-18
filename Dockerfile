FROM maven:3
LABEL maintainer="Ryan Wang<i@ryanc.cc>"

WORKDIR /opt/halo
ADD . /tmp

RUN cd /tmp && mvn package -Pci && mv target/dist/halo/* /opt/halo/ \
    && rm -rf /tmp/* && rm -rf ~/.m2

EXPOSE 8090

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/opt/halo/halo-latest.jar","--spring.profiles.active=docker"]
