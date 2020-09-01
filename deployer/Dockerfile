FROM openjdk:11-jdk-slim-buster

RUN apt-get update -y

RUN mkdir /deployer
COPY --from=testbed-build:latest /testbed/deployer/build/libs/deployer.jar /deployer/deployer.jar

CMD java -jar /deployer/deployer.jar --app_name=unauthenticated-jupyter
