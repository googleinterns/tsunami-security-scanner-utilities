# Docker Images for Custom Vulnerable Applications

Sometimes we need to build custom docker images or add custom configurations
to official application images to make it vulnerable and scannable by Tsunami
scanner. This directory contains all custom docker images that will be built
and pushed to the target GCP project.

## Example

An example application can be found at `testbed-hadoop` folder. This folder
contains the docker building rules for a single node hadoop cluster, and a
`build_and_push_to_gcr.sh` automation script.

*  docker building rules

   These are the required `Dockerfile` and any other dependencies for building
   a docker image. In order to make your `Dockerfile` as generic as possible,
   you could use the `ARG` command and pass in concrete values and build time.

*  `build_and_push_to_gcr.sh`

   In this example, this automation script iterates over a list of hadoop
   versions to be supported, builds the hadoop docker image, and pushes the
   image to GCR of a given GCP project specified by the `GCP_PROjECT_ID` env
   variable.

   This file is **required** for each application, but can have arbitrary
   automation steps for building and pushing your custom docker images to GCP.
