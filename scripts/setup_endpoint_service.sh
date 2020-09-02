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

printf "${bold}${yellow}Deploying testbed API service.${normal}\n"
pushd "${SCRIPT_PATH}/../api" >/dev/null
protoc --include_imports --include_source_info "src/main/proto/testbed.proto" --descriptor_set_out /tmp/out.pb
gcloud endpoints services deploy /tmp/out.pb ./api_config.yaml
gcloud services enable servicemanagement.googleapis.com
gcloud services enable servicecontrol.googleapis.com
gcloud services enable endpoints.googleapis.com
popd >/dev/null
