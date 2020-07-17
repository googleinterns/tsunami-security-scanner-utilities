FROM debian:latest

RUN \
apt-get update -y && \
apt-get install default-jre -y

ADD ./build/libs/tsunami-security-scanner-utilities-all.jar tsunami-test-demo.jar
ADD ./application /application

CMD java -jar tsunami-test-demo.jar --app jupyter --configPath /application --templateData {'jupyter_version':'notebook-6.0.3'}
