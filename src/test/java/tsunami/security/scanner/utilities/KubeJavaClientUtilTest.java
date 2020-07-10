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

import static com.google.common.truth.Truth.assertThat;

import com.google.gson.JsonSyntaxException;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1DeleteOptions;
import io.kubernetes.client.util.Config;
import java.io.IOException;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class KubeJavaClientUtilTest {

  @Test
  public void createResources_whenInputValid_success()
      throws IOException, ApiException, InterruptedException {
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
            + "  type: LoadBalancer\n"
            + "---\n"
            + "apiVersion: v1\n"
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

    ApiClient client = Config.defaultClient();
    Configuration.setDefaultApiClient(client);

    KubeJavaClientUtil.createResources(resourceConfig);

    Thread.sleep(1000);

    List<String> podList = KubeJavaClientUtil.getPods();
    List<String> serviceList = KubeJavaClientUtil.getServices();

    // Check started services and pods.
    assertThat(serviceList).contains("jupyter");
    assertThat(podList).contains("jupyter");

    // Delete all created resources.
    CoreV1Api coreV1Api = new CoreV1Api();

    coreV1Api.deleteNamespacedService(
        "jupyter", "default", null, null, null, null, null, new V1DeleteOptions());

    try {
      coreV1Api.deleteNamespacedPod(
          "jupyter", "default", null, null, null, null, null, new V1DeleteOptions());
    } catch (JsonSyntaxException e) {
      if (e.getCause() instanceof IllegalStateException) {
        IllegalStateException ise = (IllegalStateException) e.getCause();
        if (ise.getMessage() == null
            || !ise.getMessage().contains("Expected a string but was BEGIN_OBJECT")) throw e;
      } else throw e;
    }

    Thread.sleep(10000);
  }
}
