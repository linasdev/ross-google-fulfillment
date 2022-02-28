#!/bin/bash
./gradlew bootJar
docker build --build-arg JAR_FILE=build/libs/\*.jar -t ross-google-fulfillment ./
docker stack deploy -c ./stack.yml ross-google-fulfillment
