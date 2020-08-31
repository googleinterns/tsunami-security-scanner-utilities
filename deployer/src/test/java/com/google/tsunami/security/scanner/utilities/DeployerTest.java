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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

import com.beust.jcommander.ParameterException;
import com.google.common.io.Files;
import freemarker.template.TemplateException;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.util.Yaml;
import java.io.File;
import java.io.IOException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@RunWith(JUnit4.class)
public final class DeployerTest {

  private static final String RESOURCE_CONFIG =
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
  private Deployer classUnderTest;

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();
  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  @Mock
  CoreV1Api mockCoreV1Api;
  @Mock
  AppsV1Api mockAppsV1Api;

  @Before
  public void setUp() {
    this.classUnderTest = new Deployer(new KubeJavaClientUtil(mockCoreV1Api, mockAppsV1Api));
  }

  @Test
  public void runMain_whenInputValid_success() throws ApiException, IOException, TemplateException {
    File jupyterFolder = folder.newFolder("jupyter");
    File configFile = new File(jupyterFolder + "/jupyter.yaml");
    Files.asCharSink(configFile, UTF_8).write(RESOURCE_CONFIG);

    String[] args =
        new String[]{
            "--app_name",
            "jupyter",
            "--config_path",
            folder.getRoot().getPath(),
            "--template_data",
            "{'jupyter_version':'notebook-6.0.3'}"
        };

    classUnderTest.run(args);

    verify(mockCoreV1Api)
        .createNamespacedService(
            "default", (V1Service) Yaml.load(RESOURCE_CONFIG), null, null, null);
  }

  @Test
  public void runMain_whenConfigPathIsMissing_throws() {
    String[] args =
        new String[]{"--app_name", "jupyter", "--template_data",
            "{'jupyter_version':'notebook-6.0.3'}"};

    assertThrows(ParameterException.class, () -> classUnderTest.run(args));
  }

  @Test
  public void runMain_whenConfigPathDoesNotExist_throws() {
    String[] args =
        new String[]{
            "--app_name",
            "jupyter",
            "--config_path",
            folder.getRoot().getPath(),
            "--template_data",
            "{'jupyter_version':'notebook-6.0.3'}"
        };

    assertThrows(AssertionError.class, () -> classUnderTest.run(args));
  }
}
