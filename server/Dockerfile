FROM openjdk:13-jdk-slim-buster

RUN apt-get update \
    && apt-get -y -q upgrade \
    && rm -rf /var/lib/apt/lists/*

ADD ./server/build/libs/server-1.0-all.jar /testbed/server.jar
COPY --from=testbed-build:latest /testbed/server/build/libs/server.jar /server/server.jar

EXPOSE 50051

ENTRYPOINT ["java", "-jar", "/server/server.jar", "--port=50051", "--deployer_image=gcr.io/$$GCP_PROJECT_ID$$/testbed-deployer"]
