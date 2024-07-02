#!/bin/bash

file=$1
if [ "$file" == "" ]
then
    file=compose.yaml
fi

docker-compose -f "$file" down
docker-compose -f "$file" up --build testing --exit-code-from testing --remove-orphans
