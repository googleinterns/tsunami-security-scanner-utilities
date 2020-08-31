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

package com.google.tsunami.security.scanner.utilities;

import com.beust.jcommander.JCommander;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.flogger.GoogleLogger;
import com.google.common.io.Files;
import freemarker.template.TemplateException;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.util.Config;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Deployer {

  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  private final KubeJavaClientUtil kubeJavaClientUtil;

  public Deployer(KubeJavaClientUtil kubeJavaClientUtil) {
    this.kubeJavaClientUtil = kubeJavaClientUtil;
  }

  public void run(String[] args) throws ApiException, TemplateException, IOException {
    // Parse args read from command line
    DeployerArgs deployerArgs = new DeployerArgs();
    JCommander.newBuilder().addObject(deployerArgs).build().parse(args);

    String appName = deployerArgs.appName;
    String configPath = deployerArgs.configPath;
    String templateData = Strings.nullToEmpty(deployerArgs.templateData);
    logger.atInfo()
        .log("Deploying '%s' with config path '%s' and template data '%s'", appName, configPath,
            templateData);

    Path appConfigPath = Paths.get(configPath, appName);
    if (!appConfigPath.toFile().isDirectory()) {
      throw new AssertionError(String
          .format("Application config path '%s' is not a directory.", appConfigPath.toString()));
    }

    // Transform input template data Json String to Map.
    ImmutableMap<String, String> templateDataMap =
        TemplateDataUtil.parseTemplateDataJson(templateData);
    // Traverse all files under certain application's config path
    for (File configFile : Files.fileTraverser().depthFirstPreOrder(appConfigPath.toFile())) {
      if (configFile.isFile()) {
        String resourceConfig = FreeMarkerUtil.replaceTemplates(templateDataMap, configFile);
        kubeJavaClientUtil.createResources(resourceConfig);
        logger.atInfo().log("Resource file '%s' deployed.", configFile.getAbsolutePath());
      }
    }
  }

  public static void main(String[] args) throws IOException, ApiException, TemplateException {
    // Initialize Kubernetes Java Client Api.
    ApiClient client = Config.defaultClient();
    Configuration.setDefaultApiClient(client);

    Deployer deployer = new Deployer(new KubeJavaClientUtil());
    deployer.run(args);
  }
}
