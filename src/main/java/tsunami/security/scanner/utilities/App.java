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
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.KubeConfig;
import io.kubernetes.client.util.Yaml;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App {
    public static void main(String[] args) throws IOException, ApiException, ClassNotFoundException {
        /* Parse args read from command line */

        ApplicationArgs jArgs = new ApplicationArgs();
        JCommander helloCmd = JCommander.newBuilder()
                .addObject(jArgs)
                .build();
        helloCmd.parse(args);

        String appName = jArgs.getName();
        String appVersion = jArgs.getVersion();

        System.out.println("Hello " + appName + " " + appVersion);

        /* Logic to get the relative application config */
        String configPath = System.getProperty("user.dir") + "/application/" + appName + "/";
        File file = new File(configPath);
        if (file.isDirectory()) {
            try {
                File[] files = file.listFiles();
                for (File f : files) {
                    if (!f.isDirectory())
                        /* TODO: parse all config files under a certain app name to Kubernetes Object here, maybe use index to represent each config file*/
                        System.out.println(f);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        /* Creation of Pod and Service without config files
        V1Pod pod =
                new V1PodBuilder()
                        .withNewMetadata()
                        .withName("apod")
                        .endMetadata()
                        .withNewSpec()
                        .addNewContainer()
                        .withName("www")
                        .withImage("nginx")
                        .withNewResources()
                        .withLimits(new HashMap<>())
                        .endResources()
                        .endContainer()
                        .endSpec()
                        .build();
        System.out.println(Yaml.dump(pod));

        Map<String,String> m = new HashMap<>();
        Map<String,String> m2 = new HashMap<>();
        V1Service svc =
                new V1ServiceBuilder()
                        .withNewMetadata()
                        .withName("wordpress")
                        .withLabels(m)
                        .addToLabels("app", "wordpress")
                        .endMetadata()
                        .withNewSpec()
                        .addNewPort()
                        .withPort(80)
                        .endPort()
                        .withSelector(m2)
                        .addToSelector("app","wordpress")
                        .addToSelector("tier", "frontend")
                        .withType("LoadBalancer")
                        .endSpec()
                        .build();
        System.out.println(Yaml.dump(svc));
        */


        // file path to KubeConfig
        String kubeConfigPath = System.getProperty("user.home") + "/.kube/config";

        // loading the out-of-cluster config, a kubeconfig from file-system
        ApiClient client =
                ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();

        // set the global default api-client to the in-cluster one from above
        Configuration.setDefaultApiClient(client);


        // TODO: load kustomization or secret file (unfixed problem in creating namespaced secret object)
        /*
        String secretPath = System.getProperty("user.dir") + "/application/wordpress/mysql-pass.yaml";
        File secretFile = new File(secretPath);
        V1Secret secret = (V1Secret) Yaml.load(secretFile);
        System.out.println(secret);
        */

        /* Initialize apis */
        CoreV1Api api = new CoreV1Api();
        AppsV1Api api1 = new AppsV1Api();

        /* Load mysql config file */
        String mysqlConfigPath = System.getProperty("user.dir") + "/application/wordpress/mysql-deployment.yaml";
        File mysqlFile = new File(mysqlConfigPath);
        List<Object> objectList4Mysql = (List<Object>) Yaml.loadAll(mysqlFile);

        /* Load wordpress config file */
        String wordpressConfigPath = System.getProperty("user.dir") + "/application/wordpress/wordpress-deployment.yaml";
        File wordpressConfig = new File(wordpressConfigPath);
        List<Object> objectList4Wordpress = (List<Object>) Yaml.loadAll(wordpressConfig);

        /* Parse mysql configs */
        V1Service mysqlSvc = (V1Service) objectList4Mysql.get(0);
        V1PersistentVolumeClaim mysqlPvc = (V1PersistentVolumeClaim) objectList4Mysql.get(1);
        V1Deployment mysqlDeployment = (V1Deployment) objectList4Mysql.get(2);

        /* Parse wordpress configs */
        V1Service wordpressSVC = (V1Service) objectList4Wordpress.get(0);
        V1PersistentVolumeClaim wordpressPVC = (V1PersistentVolumeClaim) objectList4Wordpress.get(1);
        V1Deployment wordpressDeployment = (V1Deployment) objectList4Wordpress.get(2);


        /* Create services */
        V1Service createResultSvc = api.createNamespacedService("default", mysqlSvc, null, null, null);
        V1Service createResultWordpressSvc = api.createNamespacedService("default", wordpressSVC, null, null, null);

        /* Create persistent volume claims */
        V1PersistentVolumeClaim createResultPvc= api.createNamespacedPersistentVolumeClaim("default", mysqlPvc, null,null,null);
        V1PersistentVolumeClaim createResultWordpressPvc= api.createNamespacedPersistentVolumeClaim("default", wordpressPVC, null,null,null);

        /* Deploy */
        V1Deployment createResultDeployment= api1.createNamespacedDeployment("default", mysqlDeployment, null,null,null);
        V1Deployment createResultWordpressDeployment= api1.createNamespacedDeployment("default", wordpressDeployment, null,null,null);/*/


        /* Test created results.
        System.out.println("-----------------------");
        System.out.println(createResultSvc);
        System.out.println(createResultPvc);
        System.out.println(createResultDeployment);
        System.out.println("-----------------------");
        System.out.println(createResultWordpressSvc);
        System.out.println(createResultWordpressPvc);
        System.out.println(createResultWordpressDeployment);
        System.out.println("-----------------------"); */


        /* Deletion
        V1Status deleteResult =
                api.deleteNamespacedService(
                        curSvc.getMetadata().getName(),
                        "default",
                        null,
                        null,
                        null,
                        null,
                        null,
                        new V1DeleteOptions());
        System.out.println(deleteResult);
        */

    }
}
