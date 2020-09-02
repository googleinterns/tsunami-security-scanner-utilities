# Unauthenticated Jupyter Notebook

This directory contains the deployment configs for an unauthenticated Jupyter
Notebook.

The deployed service has name `unauthenticated-jupyter` and listens on port
`80`.

## Template data

This config does have any template arguments.

## Example deployment command:

```shell script
# Compile the client jar if not exist. Assumes PWD is the root directory of this
project.
$ ./gradlew :client:shadowJar

$ java -jar client/build/libs/client.jar \
    --tsunami_testbed=[testbed master service IP and port] \
    --api_key=[API key generated at prerequisite step] \
    --operation=createDeployment \
    --app_name=unauthenticated_jupyter \
    --template_data=""
```