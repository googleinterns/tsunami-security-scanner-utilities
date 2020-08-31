FROM gradle:6.6.1-jdk11 AS builder
COPY . /tmp/repo/
WORKDIR /tmp/repo/
RUN gradle shadowJar

FROM openjdk:11-jdk-slim-buster

RUN apt-get update -y

RUN mkdir /deployer
COPY --from=builder /tmp/repo/build/libs/deployer.jar /deployer/deployer.jar
COPY --from=builder /tmp/repo/application /deployer/application

CMD \
java -jar /deployer/deployer.jar \
  --app_name=unauthenticated-jupyter \
  --config_path=/deployer/application \
  --template_data ''
