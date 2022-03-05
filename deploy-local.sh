#!/bin/bash
./gradlew bootJar
docker build -t ross-google-fulfillment ./
docker stack deploy -c ./stack.yml ross-google-fulfillment
