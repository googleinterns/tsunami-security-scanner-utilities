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
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertThrows;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import freemarker.template.TemplateException;
import java.io.File;
import java.io.IOException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class FreeMarkerUtilTest {

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  @Test
  public void replaceTemplates_whenAllTemplatesMatched_success()
      throws IOException, TemplateException {
    ImmutableMap<String, String> templateData = ImmutableMap
        .of("jupyter_version", "notebook-6.0.3");

    File configFile = folder.newFile("test.yaml");
    Files.asCharSink(configFile, UTF_8).write("jupyter_version:${jupyter_version}\n");

    String res = FreeMarkerUtil.replaceTemplates(templateData, configFile);

    assertThat(res).isEqualTo("jupyter_version:notebook-6.0.3\n");
  }

  @Test
  public void replaceTemplates_whenNoTemplateDataMatched_failed() throws IOException {
    ImmutableMap<String, String> templateData = ImmutableMap.of("mysql_version", "5.6");

    File configFile = folder.newFile("test.yaml");
    Files.asCharSink(configFile, UTF_8).write("jupyter_version:${jupyter_version}\n");

    assertThrows(
        TemplateException.class,
        () -> FreeMarkerUtil.replaceTemplates(templateData, configFile));
  }
}
