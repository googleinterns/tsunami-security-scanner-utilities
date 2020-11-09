#!/usr/bin/env bash
service ssh start
"${HADOOP_HOME}"/sbin/start-dfs.sh
"${HADOOP_HOME}"/sbin/start-yarn.sh
tail -f /dev/null

