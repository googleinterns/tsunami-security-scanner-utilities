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
import com.beust.jcommander.Parameters;

@Parameters(separators = "=")
public class DeployerArgs {

  @Parameter(names = "--app_name", description = "Application name", required = true)
  public String appName;

  @Parameter(names = "--config_path", description = "Path for config files", required = true)
  public String configPath;

  @Parameter(
      names = "--template_data",
      description = "Template Data needs to be substituted in Json String type.")
  public String templateData;
}
