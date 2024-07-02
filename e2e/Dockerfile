FROM ghcr.io/linuxsuren/api-testing:v0.0.17
WORKDIR /workspace
COPY testsuite.yaml .
CMD [ "atest", "run", "-p", "testsuite.yaml", "--level=trace", "--request-ignore-error", "--report=md" ]
