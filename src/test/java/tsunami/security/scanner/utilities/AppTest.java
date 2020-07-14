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
import static org.mockito.Mockito.*;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.util.Yaml;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@RunWith(JUnit4.class)
public final class AppTest {

  @Rule public TemporaryFolder folder = new TemporaryFolder();

  @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock CoreV1Api mockCoreV1Api;

  @Mock AppsV1Api mockAppsV1Api;

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
          + "  type: LoadBalancer";

  @Test
  public void runMain_whenInputValid_success() throws ApiException, IOException {
    File jupyterFolder = folder.newFolder("jupyter");
    File configFile = new File(jupyterFolder + "/jupyter.yaml");
    FileWriter writer = new FileWriter(configFile);
    writer.write(resourceConfig);
    writer.close();

    String[] args =
        new String[] {
          "--app",
          "jupyter",
          "--configPath",
          folder.getRoot().getPath(),
          "--templateData",
          "{'jupyter_version':'notebook-6.0.3'}"
        };

    KubeJavaClientUtil kubeJavaClientUtil = new KubeJavaClientUtil(mockCoreV1Api, mockAppsV1Api);
    App classUnderTest = new App(kubeJavaClientUtil);

    V1Service resource = (V1Service) Yaml.load(resourceConfig);
    doThrow(new RuntimeException("Skip the service creation steps."))
        .when(mockCoreV1Api)
        .createNamespacedService("default", resource, null, null, null);

    assertThrows(RuntimeException.class, () -> classUnderTest.run(args));
  }

  @Test
  public void runMain_whenConfigPathIsMissing_Success() throws ApiException, IOException {
    String[] args =
        new String[] {"--app", "jupyter", "--templateData", "{'jupyter_version':'notebook-6.0.3'}"};

    KubeJavaClientUtil kubeJavaClientUtil = new KubeJavaClientUtil(mockCoreV1Api, mockAppsV1Api);
    App classUnderTest = new App(kubeJavaClientUtil);

    V1Service resource = (V1Service) Yaml.load(resourceConfig);
    doThrow(new RuntimeException("Skip the service creation steps."))
        .when(mockCoreV1Api)
        .createNamespacedService("default", resource, null, null, null);

    assertThrows(RuntimeException.class, () -> classUnderTest.run(args));
  }

  @Test
  public void runMain_whenConfigPathDoesNotExist_failed() {
    String[] args =
        new String[] {
          "--app",
          "jupyter",
          "--configPath",
          folder.getRoot().getPath(),
          "--templateData",
          "{'jupyter_version':'notebook-6.0.3'}"
        };

    KubeJavaClientUtil kubeJavaClientUtil = new KubeJavaClientUtil(mockCoreV1Api, mockAppsV1Api);
    App classUnderTest = new App(kubeJavaClientUtil);

    assertThrows(FileNotFoundException.class, () -> classUnderTest.main(args));
  }
}
