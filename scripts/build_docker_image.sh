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
SCRIPT_PATH="$( cd "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"

printf "${bold}${yellow}Building base docker image.${normal}\n"
pushd "${SCRIPT_PATH}/.." >/dev/null
docker build -t "testbed-build:latest" .
popd >/dev/null

printf "${bold}${yellow}Building deployer docker image.${normal}\n"
pushd "${SCRIPT_PATH}/../deployer" >/dev/null
docker build -t "testbed-deployer:latest" .
docker tag "testbed-deployer:latest" "gcr.io/${GCP_PROJECT_ID}/testbed-deployer:latest"
docker push "gcr.io/${GCP_PROJECT_ID}/testbed-deployer:latest"
popd >/dev/null

printf "${bold}${yellow}Building server docker image.${normal}\n"
pushd "${SCRIPT_PATH}/../server" >/dev/null
docker build -t "testbed-server:latest" .
docker tag "testbed-server:latest" "gcr.io/${GCP_PROJECT_ID}/testbed-server:latest"
docker push "gcr.io/${GCP_PROJECT_ID}/testbed-server:latest"
popd >/dev/null

printf "${bold}${yellow}Building API client.${normal}\n"
pushd "${SCRIPT_PATH}/.." >/dev/null
./gradlew :client:shadowJar
popd >/dev/null
