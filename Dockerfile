FROM debian:latest

RUN \
apt-get update -y && \
apt-get install default-jre -y

ADD ./build/libs/tsunami-security-scanner-utilities.jar tsunami-test-hello-world.jar

CMD java -jar tsunami-test-hello-world.jar
