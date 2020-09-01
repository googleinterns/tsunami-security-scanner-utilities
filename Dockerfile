FROM gradle:6.6.1-jdk11
COPY . /testbed/

WORKDIR /testbed/
RUN gradle :deployer:shadowJar :client:shadowJar :server:shadowJar
