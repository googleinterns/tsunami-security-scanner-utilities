#!/usr/bin/env bash
# Copyright 2020 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

set -eu

bold=$(tput bold)
yellow=$(tput setaf 3)
normal=$(tput sgr0)
IMAGE_NAME="testbed-hadoop"
GCR_IMAGE="gcr.io/${GCP_PROJECT_ID}/${IMAGE_NAME}"

while read version; do
  printf "${bold}${yellow}Building hadoop docker image for version ${version}.${normal}\n"
  docker build --build-arg HADOOP_VERSION=${version} -t ${IMAGE_NAME}:${version} .

  printf "${bold}${yellow}Pushing ${IMAGE_NAME}:${version} to ${GCR_IMAGE}:${version}.${normal}\n"
  docker tag "${IMAGE_NAME}:${version}" "${GCR_IMAGE}:${version}"
  docker push "${GCR_IMAGE}:${version}"
done <versions.txt
