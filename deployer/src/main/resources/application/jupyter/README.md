# Jupyter Notebook

This directory contains the deployment configs for a simple Jupyter Notebook
application. The service listens on port `80`.

This configs deploys the following services:

- `jupyter`: the Jupyter Notebook application.

## Template data

```json
{
  "jupyter_version": "version_string",
  "notebook_token": "token_for_notebook"
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
    --app_name=jupyter \
    --template_data="{\"jupyter_version\":\"latest\",\"notebook_token\":\"notebook_token\"}"
```
