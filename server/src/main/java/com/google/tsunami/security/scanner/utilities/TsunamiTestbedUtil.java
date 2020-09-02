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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.flogger.GoogleLogger;
import io.grpc.Status;
import io.grpc.StatusException;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.BatchV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import java.util.Optional;

/** The internal implementation of grpc requests. */
final class TsunamiTestbedUtil {

  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  private final String deployerImage;

  TsunamiTestbedUtil(String deployerImage) {
    this.deployerImage = deployerImage;
  }

  public V1Job createDeployment(String applicationName, String templateData)
      throws StatusException {
    try {
      logger.atInfo().log(
          "Creating deployment for '%s' with template data '%s'.", applicationName, templateData);

      BatchV1Api batchV1Api = new BatchV1Api();
      V1Job createdJob =
          batchV1Api.createNamespacedJob(
              "default", buildDeployerBatchJob(applicationName, templateData), null, null, null);
      logger.atInfo().log(
          "Created new deployer job '%s' with id '%s'.",
          Optional.ofNullable(createdJob.getMetadata())
              .map(V1ObjectMeta::getName)
              .orElse("UNKNOWN_JOB_NAME"),
          Optional.ofNullable(createdJob.getMetadata())
              .map(V1ObjectMeta::getUid)
              .orElse("UNKNOWN_JOB_ID"));
      return createdJob;
    } catch (ApiException e) {
      throw Status.INTERNAL
          .withDescription(
              String.format("Unable to create new deployment for %s.", applicationName))
          .augmentDescription("Exception message: " + e.getMessage())
          .augmentDescription("Code: " + e.getCode())
          .augmentDescription("Body: " + e.getResponseBody())
          .withCause(e)
          .asException();
    }
  }

  private V1Job buildDeployerBatchJob(String applicationName, String templateData) {
    return new V1Job()
        .apiVersion("batch/v1")
        .kind("Job")
        .metadata(
            new V1ObjectMeta()
                .generateName("testbed-deployer-")
                .putLabelsItem("app-name", applicationName))
        .spec(
            new V1JobSpec()
                .backoffLimit(1)
                .template(
                    new V1PodTemplateSpec()
                        .spec(
                            new V1PodSpec()
                                .restartPolicy("Never")
                                .addContainersItem(
                                    new V1Container()
                                        .name("testbed-deployer")
                                        .image(deployerImage)
                                        .addCommandItem("java")
                                        .args(
                                            ImmutableList.of(
                                                "-jar",
                                                "/deployer/deployer.jar",
                                                "--app_name",
                                                applicationName,
                                                "--template_data",
                                                Strings.nullToEmpty(templateData)))))));
  }

  public Iterable<String> listApplications() throws StatusException {
    try {
      logger.atInfo().log("Listing all applications.");

      // List all running services.
      V1ServiceList v1ServiceList =
          new CoreV1Api()
              .listNamespacedService(
                  "default", null, null, null, null, null, null, null, null, null);
      ImmutableList<String> applications =
          v1ServiceList.getItems().stream()
              .map(v1Service -> v1Service.getMetadata().getName())
              .collect(toImmutableList());

      logger.atInfo().log("Applications list: " + applications);
      return applications;
    } catch (ApiException e) {
      throw Status.INTERNAL
          .withDescription("Unable to list applications.")
          .augmentDescription("Exception message: " + e.getMessage())
          .augmentDescription("Code: " + e.getCode())
          .augmentDescription("Body: " + e.getResponseBody())
          .withCause(e)
          .asException();
    }
  }

  public ServiceEndpoint getApplication(String applicationName) throws StatusException {
    try {
      logger.atInfo().log("Getting application info for application '%s'.", applicationName);

      // Get services list.
      V1ServiceList v1ServiceList =
          new CoreV1Api()
              .listNamespacedService(
                  "default", null, null, null, null, null, null, null, null, null);

      for (V1Service svc : v1ServiceList.getItems()) {
        if (svc.getMetadata().getName().equals(applicationName)) {
          String ip =
              Optional.ofNullable(svc.getStatus())
                  .map(V1ServiceStatus::getLoadBalancer)
                  .map(V1LoadBalancerStatus::getIngress)
                  .flatMap(
                      loadBalancers ->
                          loadBalancers.stream().map(V1LoadBalancerIngress::getIp).findFirst())
                  .orElse("UNKNOWN");
          String port =
              Optional.ofNullable(svc.getSpec())
                  .map(V1ServiceSpec::getPorts)
                  .flatMap(
                      ports ->
                          ports.stream()
                              .map(V1ServicePort::getPort)
                              .map(String::valueOf)
                              .findFirst())
                  .orElse("UNKNOWN");
          logger.atInfo().log("Application '%s' running on '%s:%s'.", applicationName, ip, port);
          return ServiceEndpoint.newBuilder().setIp(ip).setPort(port).build();
        }
      }
    } catch (ApiException e) {
      throw Status.INTERNAL
          .withDescription(
              String.format("Unable to fetch application info for '%s'.", applicationName))
          .augmentDescription("Exception message: " + e.getMessage())
          .augmentDescription("Code: " + e.getCode())
          .augmentDescription("Body: " + e.getResponseBody())
          .withCause(e)
          .asException();
    }

    throw Status.NOT_FOUND
        .withDescription(
            String.format("Application with name '%s' not found on the cluster.", applicationName))
        .asException();
  }
}
