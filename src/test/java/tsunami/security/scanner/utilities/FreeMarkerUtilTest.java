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

import freemarker.template.TemplateException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class FreeMarkerUtilTest {

  @Test
  public void testFreeMarkerUtil() throws IOException, TemplateException {
    Map<String, String> templateDataMap = new HashMap<>();
    templateDataMap.put("jupyter_version", "0.0.3");

    File configFile =
        new File(System.getProperty("user.dir") + "/application/jupyter/jupyter.yaml");
    String res = FreeMarkerUtil.replaceTemplates(templateDataMap, configFile);

    String expected =
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
            + "      image: skippbox/jupyter:0.0.3\n"
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

    assertThat(res).isEqualTo(expected);
  }

  @Test
  public void testFreeMarkerUtil_whenNoInputTemplateDataMatched()
      throws IOException, TemplateException {
    Map<String, String> templateDataMap = new HashMap<>();
    templateDataMap.put("mysql_version", "5.6");
    templateDataMap.put("password", "dfhieuwhfhdsfj");

    // Missed data: ${jupyter_version}
    File configFile =
        new File(System.getProperty("user.dir") + "/application/jupyter/jupyter.yaml");
    assertThrows(
        TemplateException.class,
        () -> FreeMarkerUtil.replaceTemplates(templateDataMap, configFile));
  }
}
