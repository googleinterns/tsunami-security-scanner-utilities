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
green=$(tput setaf 2)
yellow=$(tput setaf 3)
normal=$(tput sgr0)
SCRIPT_PATH="$( cd "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"

printf "${bold}${yellow}Setting up testbed for ${GKE_CLUSTER_NAME} on project ${GCP_PROJECT_ID}.${normal}\n"

bash "${SCRIPT_PATH}/update_template.sh"
bash "${SCRIPT_PATH}/build_docker_image.sh"
bash "${SCRIPT_PATH}/build_app_images.sh"
bash "${SCRIPT_PATH}/setup_endpoint_service.sh"
bash "${SCRIPT_PATH}/deploy_api_server.sh"

printf "${bold}${green}Bootstrap finished.${normal}\n"
