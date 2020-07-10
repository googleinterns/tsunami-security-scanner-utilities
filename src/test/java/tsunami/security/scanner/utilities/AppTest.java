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
import static com.google.common.truth.Truth8.assertThat;
import static org.junit.Assert.assertThrows;

import com.google.gson.JsonSyntaxException;
import freemarker.template.TemplateException;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1DeleteOptions;
import io.kubernetes.client.util.Config;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class AppTest {

  @Test
  public void testMain()
      throws ApiException, IOException, ClassNotFoundException, TemplateException,
          InterruptedException {
    String[] args = setArgs();

    App classUnderTest = new App();
    classUnderTest.main(args);

    ApiClient client = Config.defaultClient();
    Configuration.setDefaultApiClient(client);

    List<String> podList = KubeJavaClientUtil.getPods();
    List<String> serviceList = KubeJavaClientUtil.getServices();

    // Check started services and pods.
    assertThat(serviceList).contains("jupyter");
    assertThat(podList).contains("jupyter");

    Thread.sleep(1000);

    // Delete all created resources
    CoreV1Api coreV1Api = new CoreV1Api();

    coreV1Api.deleteNamespacedService(
        "jupyter", "default", null, null, null, null, null, new V1DeleteOptions());

    try {
      coreV1Api.deleteNamespacedPod(
          "jupyter", "default", null, null, null, null, null, new V1DeleteOptions());
    } catch (JsonSyntaxException exception) {
      if (exception.getCause() instanceof IllegalStateException) {
        IllegalStateException ise = (IllegalStateException) exception.getCause();
        if (ise.getMessage() == null
            || !ise.getMessage().contains("Expected a string but was BEGIN_OBJECT"))
          throw exception;
      } else throw exception;
    }

    Thread.sleep(10000);
  }

  @Test
  public void testMain_whenInputArgsIsIllegal() {
    String[] args = new String[2];
    args[0] = "--app";
    args[1] = "jupyter";

    App classUnderTest = new App();
    assertThrows(TemplateException.class, () -> classUnderTest.main(args));
  }

  @Test
  public void testMain_whenConfigPathDoesNotExist() {
    String[] args = setArgs();
    args[3] = System.getProperty("user.dir");

    App classUnderTest = new App();
    assertThrows(FileNotFoundException.class, () -> classUnderTest.main(args));

    args[1] = "jupytr";
    args[3] = System.getProperty("user.dir") + "/application";

    assertThrows(FileNotFoundException.class, () -> classUnderTest.main(args));
  }

  private String[] setArgs() {
    String[] args = new String[6];
    args[0] = "--app";
    args[1] = "jupyter";
    args[2] = "--configPath";
    args[3] = System.getProperty("user.dir") + "/application";
    args[4] = "--templateData";
    args[5] = "{'jupyter_version':'0.0.3'}";
    return args;
  }
}
