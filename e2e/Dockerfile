FROM ghcr.io/linuxsuren/api-testing:master@sha256:589cf9742974c80755f73967c185144a7618decc25d3faa9423a0eda6e4a79c0
WORKDIR /workspace
COPY testsuite.yaml .
CMD [ "atest", "run", "-p", "testsuite.yaml", "--level=trace", "--request-ignore-error", "--report=md" ]
