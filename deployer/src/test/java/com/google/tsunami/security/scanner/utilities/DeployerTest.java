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

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

import freemarker.template.TemplateException;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.util.Yaml;
import java.io.IOException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@RunWith(JUnit4.class)
public final class DeployerTest {

  private final ScanResult scanResult = new ClassGraph().whitelistPaths("/application").scan();
  private Deployer deployer;

  @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock CoreV1Api mockCoreV1Api;
  @Mock AppsV1Api mockAppsV1Api;

  @Before
  public void setUp() {
    this.deployer = new Deployer(new KubeJavaClientUtil(mockCoreV1Api, mockAppsV1Api), scanResult);
  }

  @Test
  public void runMain_whenInputValid_success() throws ApiException, IOException, TemplateException {
    String[] args = new String[] {"--app_name", "fake_jupyter"};

    deployer.run(args);

    verify(mockCoreV1Api)
        .createNamespacedService(
            "default",
            (V1Service)
                Yaml.load(
                    scanResult
                        .getResourcesWithPath("application/fake_jupyter/fake_jupyter.yaml")
                        .get(0)
                        .getContentAsString()),
            null,
            null,
            null);
  }

  @Test
  public void runMain_whenConfigPathDoesNotExist_throws() {
    String[] args = new String[] {"--app_name", "not_exist"};

    assertThrows(AssertionError.class, () -> deployer.run(args));
  }
}
