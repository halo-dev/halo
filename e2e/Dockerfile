FROM ghcr.io/linuxsuren/api-testing:v0.0.16
WORKDIR /workspace
COPY testsuite.yaml .
CMD [ "atest", "run", "-p", "testsuite.yaml", "--level=debug" ]
