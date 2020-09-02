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
import freemarker.template.TemplateException;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ResourceList;
import io.github.classgraph.ScanResult;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.util.Config;
import java.io.IOException;
import java.util.regex.Pattern;

public final class Deployer {

  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  private final KubeJavaClientUtil kubeJavaClientUtil;
  private final ScanResult scanResult;

  public Deployer(KubeJavaClientUtil kubeJavaClientUtil, ScanResult scanResult) {
    this.kubeJavaClientUtil = kubeJavaClientUtil;
    this.scanResult = scanResult;
  }

  public void run(String[] args) throws ApiException, TemplateException, IOException {
    // Parse args read from command line
    DeployerArgs deployerArgs = new DeployerArgs();
    JCommander.newBuilder().addObject(deployerArgs).build().parse(args);

    String appName = deployerArgs.appName;
    String templateData = Strings.nullToEmpty(deployerArgs.templateData);
    logger.atInfo().log("Deploying '%s' with template data '%s'", appName, templateData);

    ImmutableMap<String, String> templateDataMap =
        TemplateDataUtil.parseTemplateDataJson(templateData);
    ResourceList configs = scanResult.getResourcesMatchingPattern(
        Pattern.compile(String.format("^application/%s/.*\\.yaml", appName)));
    if (configs.isEmpty()) {
      throw new AssertionError(String.format("No configs found for '%s'.", appName));
    }
    for (Resource config : configs) {
      String resourceConfig = FreeMarkerUtil.replaceTemplates(templateDataMap, config.getPath());
      kubeJavaClientUtil.createResources(resourceConfig);
      logger.atInfo().log("Resource file '%s' deployed.", config.getPath());
    }
  }

  public static void main(String[] args) throws IOException, ApiException, TemplateException {
    try (ScanResult scanResult = new ClassGraph().whitelistPaths("/application").scan()) {
      ApiClient client = Config.defaultClient();
      Configuration.setDefaultApiClient(client);

      Deployer deployer = new Deployer(new KubeJavaClientUtil(), scanResult);
      deployer.run(args);
    }
  }
}
