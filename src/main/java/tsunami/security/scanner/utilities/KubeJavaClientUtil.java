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
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1PersistentVolumeClaim;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.util.Yaml;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KubeJavaClientUtil {
  private static CoreV1Api coreV1Api = new CoreV1Api();
  private static AppsV1Api appsV1Api = new AppsV1Api();

  private interface Handler {
    void handle(Object o) throws ApiException;
  }

  /* Use the map to relate a Class object to a Handler */
  private static final Map<Class, Handler> apiCallByClass = new HashMap<Class, Handler>();

  // public static init map
  static {
    apiCallByClass.put(
        V1Deployment.class,
        new Handler() {
          public void handle(Object o) throws ApiException {
            createDeployment((V1Deployment) o);
          }
        });

    apiCallByClass.put(
        V1PersistentVolumeClaim.class,
        new Handler() {
          public void handle(Object o) throws ApiException {
            createPVC((V1PersistentVolumeClaim) o);
          }
        });

    apiCallByClass.put(
        V1Service.class,
        new Handler() {
          public void handle(Object o) throws ApiException {
            createService((V1Service) o);
          }
        });
  }

  private static void createDeployment(V1Deployment o) throws ApiException {
    V1Deployment createResultDeployment =
        appsV1Api.createNamespacedDeployment("default", o, null, null, null);
  }

  private static void createPVC(V1PersistentVolumeClaim o) throws ApiException {
    V1PersistentVolumeClaim createResultPVC =
        coreV1Api.createNamespacedPersistentVolumeClaim("default", o, null, null, null);
  }

  private static void createService(V1Service o) throws ApiException {
    V1Service createResultSvc = coreV1Api.createNamespacedService("default", o, null, null, null);
  }

  public static void loadAllConfigs(File config) throws ApiException, IOException {
    ImmutableList<Object> objects = ImmutableList.copyOf((List<Object>) Yaml.loadAll(config));
    for (Object obj : objects) {
      Handler h = apiCallByClass.get(obj.getClass());
      if (h != null) h.handle(obj);
    }
  }
}