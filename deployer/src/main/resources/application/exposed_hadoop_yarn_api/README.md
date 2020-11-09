# Exposed Hadoop Yarn Resource Management API

This directory contains the deployment configs for the exposed Hadoop Yarn
Resource Management API vulnerability. The Yarn Resource Manager service is
exposed on port `8088`.

This configs deploys the following services:

- `exposed-hadoop-yarn-api`: the Hadoop Yarn Resource Manager application.

## Template data

```json
{
  "hadoop_version": "version for the deployed hadoop"
}
```

## Example deployment command:

```shell script
# Compile the client jar if not exist. Assumes PWD is the root directory of this
project.
$ ./gradlew :client:shadowJar

$ java -jar client/build/libs/client.jar \
    --tsunami_testbed=[testbed master service IP and port] \
    --api_key=[API key generated at prerequisite step] \
    --operation=createDeployment \
    --app_name=exposed_hadoop_yarn_api \
    --template_data="{\"hadoop_version\":\"3.3.0\"}"
```
