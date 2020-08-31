FROM openjdk:13-jdk-slim-buster

RUN \
apt-get update -y && \
apt-get install default-jre -y

ADD ./build/libs/tsunami-security-scanner-utilities-all.jar tsunami-test-demo.jar
ADD ./application /application

CMD java -jar tsunami-test-demo.jar --deployer jupyter --configPath /application --templateData {'jupyter_version':'notebook-6.0.3'}
