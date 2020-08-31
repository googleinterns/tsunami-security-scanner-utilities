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

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.verify;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.util.Yaml;
import java.io.IOException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@RunWith(JUnit4.class)
public final class KubeJavaClientUtilTest {

  @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock CoreV1Api mockCoreV1Api;
  @Mock AppsV1Api mockAppsV1Api;

  @Test
  public void createResources_whenInputServiceConfig_success() throws IOException, ApiException {
    String resourceConfig =
        "apiVersion: v1\n"
            + "kind: Service\n"
            + "metadata:\n"
            + "  name: jupyter\n"
            + "  labels:\n"
            + "    app: jupyter\n"
            + "spec:\n"
            + "  ports:\n"
            + "  - port: 80\n"
            + "    name: http\n"
            + "    targetPort: 8888\n"
            + "  selector:\n"
            + "    app: jupyter\n"
            + "  type: LoadBalancer\n";

    KubeJavaClientUtil kubeJavaClientUtil = new KubeJavaClientUtil(mockCoreV1Api, mockAppsV1Api);
    kubeJavaClientUtil.createResources(resourceConfig);

    verify(mockCoreV1Api)
        .createNamespacedService(
            "default", (V1Service) Yaml.load(resourceConfig), null, null, null);
  }

  @Test
  public void createResources_whenInputPodConfig_success() throws IOException, ApiException {
    String resourceConfig =
        "apiVersion: v1\n"
            + "kind: Pod\n"
            + "metadata:\n"
            + "  name: jupyter\n"
            + "  labels:\n"
            + "    app: jupyter\n"
            + "spec:\n"
            + "  containers:\n"
            + "    - name: jupyter\n"
            + "      image: jupyter/base-notebook:notebook-6.0.3\n"
            + "      ports:\n"
            + "      - containerPort: 8888\n"
            + "        protocol: TCP\n"
            + "        name: http\n"
            + "      volumeMounts:\n"
            + "        - mountPath: /root\n"
            + "          name: notebook-volume\n"
            + "  volumes:\n"
            + "  - name: notebook-volume\n"
            + "    gitRepo:\n"
            + "      repository: \"https://github.com/kubernetes-client/python.git\"\n";

    KubeJavaClientUtil kubeJavaClientUtil = new KubeJavaClientUtil(mockCoreV1Api, mockAppsV1Api);
    kubeJavaClientUtil.createResources(resourceConfig);

    verify(mockCoreV1Api)
        .createNamespacedPod("default", (V1Pod) Yaml.load(resourceConfig), null, null, null);
  }

  @Test
  public void createResources_whenMissingResourceCreator_throws() {
    String resourceConfig = "apiVersion: apps/v1\n"
        + "kind: ReplicaSet\n"
        + "metadata:\n"
        + "  name: frontend\n"
        + "  labels:\n"
        + "    app: guestbook\n"
        + "    tier: frontend\n"
        + "spec:\n"
        + "  replicas: 3\n"
        + "  selector:\n"
        + "    matchLabels:\n"
        + "      tier: frontend\n"
        + "  template:\n"
        + "    metadata:\n"
        + "      labels:\n"
        + "        tier: frontend\n"
        + "    spec:\n"
        + "      containers:\n"
        + "      - name: php-redis\n"
        + "        image: gcr.io/google_samples/gb-frontend:v3";

    KubeJavaClientUtil kubeJavaClientUtil = new KubeJavaClientUtil(mockCoreV1Api, mockAppsV1Api);
    assertThrows(AssertionError.class, () -> kubeJavaClientUtil.createResources(resourceConfig));
  }
}
