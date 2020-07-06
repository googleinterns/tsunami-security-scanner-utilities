/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tsunami.security.scanner.utilities;

import com.beust.jcommander.Parameter;

public class ApplicationArgs {
  @Parameter(names = "--app", description = "Application name", required = true)
  private String name;

  @Parameter(
      names = "--versionList",
      description =
          "Version of each sub applications, format: --versionList app1::version1,app2::version2")
  private List<String> versionList;

  @Parameter(names = "--configPath", description = "Path for config files")
  private String configPath;

  @Parameter(names = "--password", description = "Resources' password")
  private String password;

  public String getName() {
    return name;
  }

  public List<String> getVersionList() {
    return versionList;
  }

  public String getConfigPath() {
    return configPath;
  }

  public String getPassword() {
    return password;
  }
}
