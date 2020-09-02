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

printf "${bold}${yellow}Deploying testbed API server.${normal}\n"
pushd "${SCRIPT_PATH}/../server" >/dev/null
kubectl create -f "deployment.yaml"
printf "waiting until service is ready...\n"
external_ip=""
while [[ -z $external_ip ]]; do
  sleep 10
  external_ip=$(kubectl get svc testbed-api --template="{{range .status.loadBalancer.ingress}}{{.ip}}{{end}}")
done
kubectl apply -f rbac.yaml
printf "testbed-api service is ready to take traffic.\n"
kubectl get svc testbed-api
popd >/dev/null
