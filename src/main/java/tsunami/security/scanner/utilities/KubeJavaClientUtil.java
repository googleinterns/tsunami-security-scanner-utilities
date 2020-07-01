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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1PersistentVolumeClaim;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.util.Yaml;
import java.io.File;
import java.io.IOException;

/**
 * This class is a wrapper for Kubernetes Java Client Api
 * Usage: KubeJavaClientUtil.createResources(File resourceConfigFile);
 * Purpose: Read in a Kubernetes config file for a certain application,
 *          parse it into resources needed and create them using Java Client Api.
 * */
public final class KubeJavaClientUtil {

  private static CoreV1Api coreV1Api = new CoreV1Api();

  private static AppsV1Api appsV1Api = new AppsV1Api();

  private KubeJavaClientUtil() {}

  @FunctionalInterface
  private interface ResourceCreator {
    void createResource(Object o) throws ApiException;
  }

  // Use the map to relate a Class object to a Handler
  private static final ImmutableMap<Class, ResourceCreator> apiCallByClass =
      ImmutableMap.of(
          V1Deployment.class,
          deployment -> createDeployment((V1Deployment) deployment),
          V1PersistentVolumeClaim.class,
          v1Pvc -> createPvc((V1PersistentVolumeClaim) v1Pvc),
          V1Service.class,
          v1Service -> createService((V1Service) v1Service));

  private static void createDeployment(V1Deployment v1Deployment) throws ApiException {
    appsV1Api.createNamespacedDeployment("default", v1Deployment, null, null, null);
  }

  private static void createPvc(V1PersistentVolumeClaim v1Pvc) throws ApiException {
    coreV1Api.createNamespacedPersistentVolumeClaim("default", v1Pvc, null, null, null);
  }

  private static void createService(V1Service v1Service) throws ApiException {
    coreV1Api.createNamespacedService("default", v1Service, null, null, null);
  }

  public static void createResources(File resourceConfigFile) throws ApiException, IOException {
    ImmutableList<Object> resources = ImmutableList.copyOf(Yaml.loadAll(resourceConfigFile));
    for (Object resource : resources) {
      ResourceCreator h = apiCallByClass.get(resource.getClass());
      if (h != null) h.createResource(resource);
    }
  }
}
