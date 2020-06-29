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
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.Yaml;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class App {

  public static void main(String[] args) throws IOException, ApiException, ClassNotFoundException {

    /* Parse args read from command line */
    ApplicationArgs jArgs = new ApplicationArgs();
    JCommander helloCmd = JCommander.newBuilder().addObject(jArgs).build();
    helloCmd.parse(args);

    String appName = jArgs.getName();
    String appVersion = jArgs.getVersion();
    String appConfigPath = jArgs.getConfigPath();

    System.out.println("Hello " + appName + " " + appVersion + " " + appConfigPath);

    /* Logic to get the relative application config */
    // TODO: determine whether parameter is successfully passed in.
    appConfigPath = System.getProperty("user.dir") + "/application/";
    String configPath = appConfigPath + "/" + appName + "/";
    File file = new File(configPath);

    for (File f : Files.fileTraverser().depthFirstPreOrder(file)) {
      if (f.isFile()) {
        /* TODO: parse all config files under a certain app name to Kubernetes Object here*/
        System.out.println(f);
      }
    }

    /* Load mysql config file
    * TODO: The method of getting file paths using user.dir will be resolved after previous TODO is done.
    */
    String mysqlConfigPath =
        System.getProperty("user.dir") + "/application/wordpress/mysql-deployment.yaml";
    File mysqlConfig = new File(mysqlConfigPath);
    ImmutableList<Object> objectList4Mysql =
        ImmutableList.copyOf((List<Object>) Yaml.loadAll(mysqlConfig));

    /* Load wordpress config file */
    String wordpressConfigPath =
        System.getProperty("user.dir") + "/application/wordpress/wordpress-deployment.yaml";
    File wordpressConfig = new File(wordpressConfigPath);
    ImmutableList<Object> objectList4Wordpress =
        ImmutableList.copyOf((List<Object>) Yaml.loadAll(wordpressConfig));

    // TODO: Replace passwords in deployment configs.

    ApiClient client = Config.defaultClient();
    Configuration.setDefaultApiClient(client);

    // TODO: Using a map from Object.getClass() to different api calls. Still IN PROCESS.

    /* Initialize apis */
    CoreV1Api coreV1Api = new CoreV1Api();
    AppsV1Api appsV1Api = new AppsV1Api();

    /* Parse mysql configs */
    V1Service mysqlSvc = (V1Service) objectList4Mysql.get(0);
    V1PersistentVolumeClaim mysqlPvc = (V1PersistentVolumeClaim) objectList4Mysql.get(1);
    V1Deployment mysqlDeployment = (V1Deployment) objectList4Mysql.get(2);

    /* Parse wordpress configs */
    V1Service wordpressSVC = (V1Service) objectList4Wordpress.get(0);
    V1PersistentVolumeClaim wordpressPVC = (V1PersistentVolumeClaim) objectList4Wordpress.get(1);
    V1Deployment wordpressDeployment = (V1Deployment) objectList4Wordpress.get(2);

    /* Create services */
    V1Service createResultSvc =
        coreV1Api.createNamespacedService("default", mysqlSvc, null, null, null);
    V1Service createResultWordpressSvc =
        coreV1Api.createNamespacedService("default", wordpressSVC, null, null, null);

    /* Create persistent volume claims */
    V1PersistentVolumeClaim createResultPvc =
        coreV1Api.createNamespacedPersistentVolumeClaim("default", mysqlPvc, null, null, null);
    V1PersistentVolumeClaim createResultWordpressPvc =
        coreV1Api.createNamespacedPersistentVolumeClaim("default", wordpressPVC, null, null, null);

    /* Deploy */
    V1Deployment createResultDeployment =
        appsV1Api.createNamespacedDeployment("default", mysqlDeployment, null, null, null);
    V1Deployment createResultWordpressDeployment =
        appsV1Api.createNamespacedDeployment("default", wordpressDeployment, null, null, null);

    /* Example for creation of objects and deletion:
     *   https://github.com/kubernetes-client/java/blob/master/examples/src/main/java/io/kubernetes/client/examples/YamlExample.java
     */
  }
}
