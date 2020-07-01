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

import com.beust.jcommander.JCommander;
import com.google.common.io.Files;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.util.Config;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App {

  public static void main(String[] args) throws IOException, ApiException, ClassNotFoundException {

    // Parse args read from command line
    ApplicationArgs jArgs = new ApplicationArgs();
    JCommander helloCmd = JCommander.newBuilder().addObject(jArgs).build();
    helloCmd.parse(args);

    String appName = jArgs.getName();
    List<String> appVersionList = jArgs.getVersionList();
    String appConfigPath = jArgs.getConfigPath();
    String appPassword = jArgs.getPassword();
    // Output the input app info.
    System.out.println(
        "App: "
            + appName
            + " VersionList: "
            + appVersionList
            + " Config Path: "
            + appConfigPath
            + " Password: "
            + appPassword);

    // Combine the file path with application name as a directory.
    String configPath = appConfigPath + "/" + appName + "/";

    // Initialize Kubernetes Java Client Api.
    ApiClient client = Config.defaultClient();
    Configuration.setDefaultApiClient(client);

    // Translate input versionList to a map of <sub application name, version>
    Map<String, String> appVersionMap = new HashMap<>();
    for (String version : appVersionList) {
      appVersionMap.put(
          version.substring(0, version.indexOf("::")),
          version.substring(version.indexOf("::") + 2));
    }

    // Load all application's config files, run services and deploy the app on GKE.
    try {
      File configFiles = new File(configPath);

      // Traverse all files under certain application's config path
      for (File configFile : Files.fileTraverser().depthFirstPreOrder(configFiles)) {

        if (configFile.isFile()) {
          String curFile = configFile.getName();
          String curFileName = curFile.substring(0, curFile.lastIndexOf("."));

          if (appVersionMap.containsKey(curFileName)) {
            // Replace ${version} in the config file template with input version
            String version = (String) appVersionMap.get(curFileName);
            // System.out.println(version);
            File newConfigFile = FreeMarkerUtil.replaceTemplates(version, appPassword, configFile);

            // Parse all config files to Kubernetes Objects and create them.
            System.out.println("File being loaded: " + newConfigFile);
            KubeJavaClientUtil.createResources(newConfigFile);
          } else continue;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    /* Example for creation of objects and deletion:
     *   https://github.com/kubernetes-client/java/blob/master/examples/src/main/java/io/kubernetes/client/examples/YamlExample.java
     */
  }
}
