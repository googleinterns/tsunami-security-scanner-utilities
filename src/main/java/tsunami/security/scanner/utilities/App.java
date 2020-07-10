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

import static com.google.common.base.Strings.isNullOrEmpty;
import com.beust.jcommander.JCommander;
import com.google.common.io.Files;
import freemarker.template.TemplateException;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.util.Config;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

public class App {

  public static void main(String[] args)
      throws IOException, ApiException, ClassNotFoundException, TemplateException {

    // Parse args read from command line
    ApplicationArgs jArgs = new ApplicationArgs();
    JCommander helloCmd = JCommander.newBuilder().addObject(jArgs).build();
    helloCmd.parse(args);

    String appName = jArgs.getName();
    String appConfigPath = jArgs.getConfigPath();
    String templateData = jArgs.getTemplateData();
    // Output the input app info.
    System.out.println(
        "App: " + appName + " Config Path: " + appConfigPath + " templateData: " + templateData);

    // Set a default directory for configs if no config path is passed in.
    if (isNullOrEmpty(appConfigPath)) {
      appConfigPath = System.getProperty("user.dir") + "/application";
    }

    // Combine the file path with application name as a directory.
    String configPath = appConfigPath + "/" + appName + "/";

    // Transform input template data Json String to Map.
    Map<String, String> templateDataMap = TemplateDataUtil.parseTemplateDataJson(templateData);

    // Initialize Kubernetes Java Client Api.
    ApiClient client = Config.defaultClient();
    Configuration.setDefaultApiClient(client);

    // Load all application's config files, run services and deploy the app on GKE.
    try {
      File configFiles = new File(configPath);

      // Check if configFiles is an existed directory
      if (!configFiles.isDirectory()) throw new FileNotFoundException("Wrong directory.");

      // Traverse all files under certain application's config path
      for (File configFile : Files.fileTraverser().depthFirstPreOrder(configFiles)) {

        if (configFile.isFile()) {
          String resourceConfig = FreeMarkerUtil.replaceTemplates(templateDataMap, configFile);

          // Parse all config to Kubernetes Objects and create them.
          KubeJavaClientUtil.createResources(resourceConfig);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }

    /* Example for creation of objects and deletion:
     *   https://github.com/kubernetes-client/java/blob/master/examples/src/main/java/io/kubernetes/client/examples/YamlExample.java
     */
  }
}
