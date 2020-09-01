FROM openjdk:11-jdk-slim-buster

RUN apt-get update -y

RUN mkdir /deployer
COPY --from=testbed-build:latest /testbed/deployer/build/libs/deployer.jar /deployer/deployer.jar
COPY --from=testbed-build:latest /testbed/deployer/application /deployer/application

CMD \
java -jar /deployer/deployer.jar \
  --app_name=unauthenticated-jupyter \
  --config_path=/deployer/application \
  --template_data ''
