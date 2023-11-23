FROM linuxsuren/api-testing:v0.0.14
WORKDIR /workspace
COPY testsuite.yaml .
CMD [ "atest", "run", "-p", "testsuite.yaml", "--level=debug" ]
