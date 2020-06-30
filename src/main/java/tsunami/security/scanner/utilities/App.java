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
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.Config;
import java.io.File;
import java.io.IOException;

public class App {

  public static void main(String[] args) throws IOException, ApiException, ClassNotFoundException {

    // Parse args read from command line
    ApplicationArgs jArgs = new ApplicationArgs();
    JCommander helloCmd = JCommander.newBuilder().addObject(jArgs).build();
    helloCmd.parse(args);

    String appName = jArgs.getName();
    String appVersion = jArgs.getVersion();
    String appConfigPath = jArgs.getConfigPath();
    // Output the input app info.
    System.out.println("App: " + appName + " Version: " + appVersion + " Config Path: " + appConfigPath);

    // Combine the file path with application name as a directory.
    String configPath = appConfigPath + "/" + appName + "/";

    // Initialize Kubernetes Java Client Api.
    ApiClient client = Config.defaultClient();
    Configuration.setDefaultApiClient(client);

    // Initialize Util tool for Java Client Api.
    KubeJavaClientUtil kubeUtil = new KubeJavaClientUtil();

    // Load all application's config files, run services and deploy the app on GKE.
    try {
      File configFile = new File(configPath);
      for (File file : Files.fileTraverser().depthFirstPreOrder(configFile)) {
        if (file.isFile()) {
          // TODO: Replace passwords in configs.
          // parse all config files to Kubernetes Objects and create them.
          System.out.println("File being loaded: " + file);
          kubeUtil.loadAllConfigs(file);
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