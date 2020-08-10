# https://github.com/GoogleCloudPlatform/openjdk-runtime
FROM openjdk:13-jdk-slim-buster

RUN apt-get update \
    && apt-get -y -q upgrade \
    && rm -rf /var/lib/apt/lists/*

ADD ./server/build/libs/server-1.0-all.jar /bookstore/server.jar

EXPOSE 8000

ENTRYPOINT ["java", "-jar", "/bookstore/server.jar"]
