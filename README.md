# Utilities for Tsunami Security Scanner

This project aims to provide useful utilities for Tsunami Security Scanner.

## TODOs

- Add APIs to automatically delete deployed resources.

## How to use

### 1. Prerequisite

1.  Make sure you have the following utilities installed:
    -   `gcloud`: [instructions](https://cloud.google.com/sdk/install)
    -   `kubectl`: use `gcloud components install kubectl` to install `kubectl`
    -   `docker`: [instructions](https://docs.docker.com/get-docker/) or
        go/installdocker
    -   `protoc`: [instructions](https://grpc.io/docs/protoc-installation/)
1.  Setup a GCP project and a GKE cluster. Take a note of the GCP project name
    as well as the GKE cluster name.
    
    NOTE: For simplicity reasons, make sure your GKE cluster has access to all
    Cloud APIs. You can do so via the Cloud Console when you create the GKE
    cluster, under `Node Pool > Security`. Or enable
    [all scopes](https://cloud.google.com/sdk/gcloud/reference/container/clusters/create#--scopes)
    for the `--scopes` option if you use `gcloud` sdk.
1.  [Generate an API key](https://console.cloud.google.com/apis/credentials)
    which will be used later for RPC client.
1.  Open a terminal, set the following environment variables:
    ```shell script
    export GCP_PROJECT_ID=[replace with project id]
    export GKE_CLUSTER_ZONE=[replace with cluster zone]
    export GKE_CLUSTER_NAME=[replace with cluster name]
    ```
1.  Authenticate the `gcloud` tool:
    ```
    gcloud auth login
    ```
1.  Set the `gcloud` project:
    ```
    gcloud config set project "${GCP_PROJECT_ID}"
    ```
1.  Fetch credentials for the GKE cluster:
    ```
    gcloud container clusters get-credentials "${GKE_CLUSTER_NAME}" --zone="${GKE_CLUSTER_ZONE}"
    ```
1.  Make sure `kubectl` works and the output matches the GKE cluster: `kubectl
    cluster-info`

### 2. Bootstrap the cluster

1.  Clone this repo:
    ```
    git clone https://github.com/googleinterns/tsunami-security-scanner-utilities && cd tsunami-security-scanner-utilities
    ```
1.  Checkout the cleanup branch:
    ```
    git checkout cleanup
    ```
1.  Execute the `bootstrap_cluster.sh` script:
    ```
    bash scripts/bootstrap_cluster.sh
    ```
    The bootstrap process would take around 10 minutes. The bootstrap script
    performs the following tasks.
    1. Instantiates some deployment template files with the `GCP_PROJECT_ID`
       variable.
    1. Builds the docker images for the application deployer and the API server,
       then pushes the images to GCR.
    1. Creates a Cloud Endpoint service for the API server.
    1. Deploys the API server image to the GKE cluster and wait for it to be
       live.
       
### 3. Deploy vulnerable applications

#### Option 1. Using the API

The bootstrap script will build a client JAR file located at `client/build/libs`
directory. Use the following command to call the deployment API and deploy an
application on the GKE cluster:

```shell script
# Deploys an unauthenticated Jupyter notebook.
java -jar client/build/libs/client.jar \
    --tsunami_testbed=[testbed master service IP and port] \
    --api_key=[API key generated at prerequisite step] \
    --operation=createDeployment \
    --app_name=unauthenticated_jupyter \
    --template_data=""
# Fetch the IP and port of the unauthenticated_jupyter service.
java -jar client/build/libs/client.jar \
    --tsunami_testbed=[testbed master service IP and port] \
    --api_key=[API key generated at prerequisite step] \
    --operation=getApplication \
    --app_name=unauthenticated_jupyter

# Or similarly for an exposed WordPress with installation page.
java -jar client/build/libs/client.jar \
    --tsunami_testbed=[testbed master service IP and port] \
    --api_key=[API key generated at prerequisite step] \
    --operation=createDeployment \
    --app_name=pre_setup_wordpress \
    --template_data="{db_password: password}"
# Fetch the IP and port of the wordpress service.
java -jar client/build/libs/client.jar \
    --tsunami_testbed=[testbed master service IP and port] \
    --api_key=[API key generated at prerequisite step] \
    --operation=getApplication \
    --app_name=pre_setup_wordpress
```

For the required arguments and template values, see the README file of each
vulnerable application located under `deployer/src/main/resources/application`.

#### Option 2. Using the deployer image

The other way to deploy a vulnerable application is to execute the deployer
image on the GKE cluster manually. Edit the `deployer/deployer-job.yaml` file to
specify the `app_name` as well as `template_data`, then run the following
command to start the deployment:

```shell script
cd deployer && kubectl apply -f deployer-job.yaml
```

### 4. Adding new deploy configs for new applications / vulnerabilities.

1. Determine the docker image to use for your deployment. Usually DockerHub will
   host the official images for the application. If there is no official ones,
   you have to build the image on your own.

   TODO: add steps for bringing your own docker images.

1. Create a new directory in `deployer/src/main/resources/application` for the
   application or vulnerabilities. All the deployment configs goes into this
   directory. The same directory name will be used in the `--app_name` deployer
   argument to identify the application during deployment.

1. The testbed deployer uses the k8s config for specifying the deployment logic.
   Usually your deployment config should look similar to the following example:

   ```yaml
   # Following defines an External LoadBalancer service, which is required by k8s
   # to route external traffic to the application Pod.
   apiVersion: v1
   kind: Service
   metadata:
     name: NAME_OF_THE_SERVICE
     labels:
       app: NAME_OF_THE_SERVICE
   spec:
     ports:
     # External user visit port 80.
     - port: 80
       # k8s route traffic to port xyz of the container
       targetPort: xyz
     selector:
       app: NAME_OF_THE_SERVICE
     type: LoadBalancer
   # Separate different k8s resources with triple-dash (i.e. ---)
   ---
   # Following defines a Deployment for the target application
   apiVersion: apps/v1
   kind: Deployment
   metadata:
     name: NAME_OF_THE_SERVICE
     labels:
       app: NAME_OF_THE_SERVICE
   spec:
     selector:
       matchLabels:
         app: NAME_OF_THE_SERVICE
         tier: frontend
     strategy:
       type: Recreate
     template:
       metadata:
         labels:
           app: NAME_OF_THE_SERVICE
           tier: frontend
       spec:
         containers:
         - name: NAME_OF_THE_SERVICE
           # Config files support template arguments in the format of ${arg}. In
           # this example, the ${app_version} argument allows users to select
           # the appropriate versions for the application.
           #
           # Substitution data for all templates of a single application can be
           # passed in via the --template_data argument of the deployer. The
           # --template_data argument should have value in the json format, maps
           # each argument to its value, e.g. {app_version: latest} for this
           # example.
           image: DOCKER_IMAGE_FOR_APPLICATION:${app_version}
           ports:
           # The port to open on the container pod.
           - containerPort: xyz
           # Following are optional, if you need special start-up commands.
           command: [ "start-up-command" ]
           args: [ "args", "for", "start-up-command" ]
   ```

   But you could use arbitrary k8s configs to define your deployment logic.

1. You can test your deployment config manually using commands like
   `kubectl apply -f config.yaml`. Make sure you use real data instead of
   templates during manual run.

1. When new configs are tested, push new configs to the GCP project by running
   `bash scripts/build_docker_image.sh`. This script will recompile all docker
   images and push them to GCR.
 
1. Create new deployments using commands mentioned earlier.

1. If you need to delete the deployed applications,

   - either manually delete resources from Cloud Console,
   - or execute `kubectl delete -f .` under the directory of corresponding
     applications in `deployer/src/main/resources/application`.

## Disclaimer

This is not an officially supported Google product.

## Source Code Headers

Every file containing source code must include copyright and license
information. This includes any JS/CSS files that you might be serving out to
browsers. (This is to help well-intentioned people avoid accidental copying that
doesn't comply with the license.)

Apache header:

```
Copyright 2020 Google LLC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
