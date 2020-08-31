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

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import freemarker.template.TemplateException;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.BatchV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.Yaml;
import java.io.File;
import java.io.IOException;
import java.util.List;

/** The internal implementation of grpc requests. */
final class TsunamiTestbedUtil {

  TsunamiTestbedUtil() {}

  public String createDeployment(
      String application, String configPath, String templateData, String deployerJobPath)
      throws IOException, TemplateException, ApiException, InterruptedException {
    String jobId = "initialID";

    System.out.println("[GRPC REQUEST: CreateDeployment] Creating deployment of : " + application);
    System.out.println(
        "app: "
            + application
            + " config path: "
            + configPath
            + " template data: "
            + templateData
            + " job yaml path: "
            + deployerJobPath);

    BatchV1Api batchV1Api = new BatchV1Api();

    File configFile = new File(deployerJobPath);
    // Replace template data in deployer yaml file.
    ImmutableMap<String, String> templateDataMap =
        ImmutableMap.of("app", application, "configPath", configPath, "templateData", templateData);
    String resourceConfig = FreeMarkerUtil.replaceTemplates(templateDataMap, configFile);

    // Load deployer job yaml file.
    Yaml.addModelMap("v1", "Job", V1Job.class);
    V1Job v1Job = (V1Job) Yaml.load(resourceConfig);
    System.out.println("Yaml Loaded.");

    // Create deployer job.
    batchV1Api.createNamespacedJob("default", v1Job, null, null, null);
    System.out.println("Created Job");

    // Get Unique Id of certain application.
    V1JobList v1JobList =
        batchV1Api.listNamespacedJob(
            "default", null, null, null, null, null, null, null, null, null);

    for (V1Job job : v1JobList.getItems()) {
      if (job.getMetadata().getName().equals(application)) {
        jobId = job.getMetadata().getUid();
        System.out.println("Application " + application + "'s unique id is: " + jobId);
        break;
      }
    }

    return jobId;
  }

  public Iterable<String> listApplications() throws ApiException {
    System.out.println("[GRPC REQUEST: ListApplications] Listing all applications.");

    CoreV1Api coreV1Api = new CoreV1Api();

    // List all running services.
    V1ServiceList v1ServiceList =
        coreV1Api.listNamespacedService(
            "default", null, null, null, null, null, null, null, null, null);
    ImmutableList<String> applications =
        v1ServiceList.getItems().stream()
            .map(v1Service -> v1Service.getMetadata().getName())
            .collect(toImmutableList());

    System.out.println("Applications list: " + applications);

    return applications;
  }

  public ServiceEndpoint getApplication(String application) throws ApiException {
    // Set default ip and port.
    ServiceEndpoint.Builder serviceEndpointBuilder = ServiceEndpoint.newBuilder();

    System.out.println(
        "[GRPC REQUEST: GetApplication] Getting ip and port information of : " + application);

    // Get services list.
    CoreV1Api coreV1Api = new CoreV1Api();
    V1ServiceList v1ServiceList =
        coreV1Api.listNamespacedService(
            "default", null, null, null, null, null, null, null, null, null);

    for (V1Service svc : v1ServiceList.getItems()) {
      // Find the required service.
      if (svc.getMetadata().getName().equals(application)) {

        // Get service's ip.
        try {
          List<V1LoadBalancerIngress> svcIP = svc.getStatus().getLoadBalancer().getIngress();
          for (V1LoadBalancerIngress ip : svcIP) {
            System.out.println("Application " + application + "'s IP: " + ip.getIp());
            serviceEndpointBuilder.setIp(ip.getIp());
          }
        } catch (NullPointerException e) {
          System.out.println("Caught null pointer exception, no IP address found.");
          throw e;
        }

        // Get service's port.
        List<V1ServicePort> ports = svc.getSpec().getPorts();
        for (V1ServicePort port : ports) {
          System.out.println("Application " + application + "'s port: " + port.getPort());
          serviceEndpointBuilder.setPort(port.getPort().toString());
        }
      }
    }

    return serviceEndpointBuilder.build();
  }
}
