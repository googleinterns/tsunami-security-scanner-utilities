FROM openjdk:8-jdk

RUN apt-get update && \
  apt-get upgrade -y && \
  apt-get install -y git curl vim ssh wget
# passwordless ssh, required by hadoop.
RUN ssh-keygen -t rsa -f ~/.ssh/id_rsa -P '' && \
  cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys && \
  echo "StrictHostKeyChecking no" >> /etc/ssh/ssh_config

ARG HADOOP_VERSION=3.3.0
ENV HADOOP_PREFIX /usr/local/hadoop
ENV HADOOP_HOME /usr/local/hadoop
ENV YARN_HOME /usr/local/hadoop
ENV HADOOP_COMMON_HOME /usr/local/hadoop
ENV HADOOP_HDFS_HOME /usr/local/hadoop
ENV HADOOP_MAPRED_HOME /usr/local/hadoop
ENV HADOOP_YARN_HOME /usr/local/hadoop
ENV HADOOP_CONF_DIR /usr/local/hadoop/etc/hadoop
ENV YARN_CONF_DIR $HADOOP_PREFIX/etc/hadoop
ENV HADOOP_CONF_DIR $HADOOP_PREFIX/etc/hadoop
ENV PATH $HADOOP_HOME/bin/:$HADOOP_HOME/sbin/:$PATH
ENV HDFS_DATANODE_USER=root
ENV HDFS_DATANODE_SECURE_USER=root
ENV HDFS_NAMENODE_USER=root
ENV HDFS_NAMENODE_SECURE_USER=root
ENV HDFS_SECONDARYNAMENODE_USER=root
ENV YARN_RESOURCEMANAGER_USER=root
ENV YARN_NODEMANAGER_USER=root

# install hadoop.
RUN groupadd hadoop
RUN useradd -d /home/hadoop -g hadoop -m hadoop --shell /bin/bash
RUN wget https://archive.apache.org/dist/hadoop/common/hadoop-${HADOOP_VERSION}/hadoop-${HADOOP_VERSION}.tar.gz && \
  tar -xzvf hadoop-${HADOOP_VERSION}.tar.gz && \
  mv hadoop-${HADOOP_VERSION} /usr/local/hadoop && \
  rm hadoop-${HADOOP_VERSION}.tar.gz

ENV HADOOP_PREFIX /usr/local/hadoop
ENV HADOOP_HOME /usr/local/hadoop
ENV PATH $HADOOP_HOME/bin/:$HADOOP_HOME/sbin/:$PATH
COPY config/* $HADOOP_HOME/etc/hadoop/
COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]

