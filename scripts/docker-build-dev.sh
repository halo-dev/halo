#!/bin/bash

VERSION=$(ls build/libs | sed 's/.*halo-//' | sed 's/.jar$//')

echo "Halo version: $VERSION"

echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
docker build --build-arg JAR_FILE="build/libs/halo-$VERSION.jar" -t $DOCKER_USERNAME/halo:latest-dev -t $DOCKER_USERNAME/halo:$VERSION.dev .
docker images
docker push $DOCKER_USERNAME/halo
