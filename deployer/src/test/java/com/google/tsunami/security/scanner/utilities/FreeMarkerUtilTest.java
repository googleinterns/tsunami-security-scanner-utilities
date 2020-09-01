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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import com.google.common.collect.ImmutableMap;
import freemarker.template.TemplateException;
import java.io.IOException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class FreeMarkerUtilTest {

  @Test
  public void replaceTemplates_whenAllTemplatesMatched_success()
      throws IOException, TemplateException {
    ImmutableMap<String, String> templateData =
        ImmutableMap.of("jupyter_version", "notebook-6.0.3");

    String res =
        FreeMarkerUtil.replaceTemplates(templateData, "application/template/template_test.txt");

    assertThat(res).isEqualTo("jupyter_version:notebook-6.0.3");
  }

  @Test
  public void replaceTemplates_whenNoTemplateDataMatched_failed() throws IOException {
    ImmutableMap<String, String> templateData = ImmutableMap.of("mysql_version", "5.6");

    assertThrows(
        TemplateException.class,
        () ->
            FreeMarkerUtil.replaceTemplates(
                templateData, "application/template/template_test.txt"));
  }
}
