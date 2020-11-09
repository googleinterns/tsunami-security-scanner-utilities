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
APPS_PATH="${SCRIPT_PATH}/../app_images"

printf "${bold}${yellow}Building docker images for custom vulnerable applications.${normal}\n"

pushd "${APPS_PATH}" >/dev/null

for app_dir in $(find ${APPS_PATH} -name 'build_and_push_to_gcr.sh' -print0 | xargs -0 -n1 dirname | sort --unique) ; do
  app_name="${app_dir##*"${APPS_PATH}/"}"
  printf "\n${yellow}Building ${app_name} ...\n${normal}"
  pushd "${app_dir}" >/dev/null
  ./build_and_push_to_gcr.sh
  popd >/dev/null
done

popd >/dev/null
