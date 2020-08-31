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
import com.google.gson.JsonSyntaxException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class TemplateDataUtilTest {

  @Test
  public void parseTemplateData_whenInputValid_success() {
    String templateDataJson =
        "{'mysql_version':'5.6','password':'dfhieuwhfhdsfj','wordpress_version':'4.8-apache'}";

    ImmutableMap<String, String> resultMap = TemplateDataUtil
        .parseTemplateDataJson(templateDataJson);

    assertThat(resultMap)
        .containsExactly(
            "mysql_version",
            "5.6",
            "password",
            "dfhieuwhfhdsfj",
            "wordpress_version",
            "4.8-apache");
  }

  @Test
  public void parseTemplateData_whenMapDataEmpty_success() {
    ImmutableMap<String, String> resultMap = TemplateDataUtil.parseTemplateDataJson("{}");

    assertThat(resultMap).isEmpty();
  }

  @Test
  public void parseTemplateData_whenEmptyString_success() {
    ImmutableMap<String, String> resultMap = TemplateDataUtil.parseTemplateDataJson("");

    assertThat(resultMap).isEmpty();
  }

  @Test
  public void parseTemplateData_whenStringEmpty_failed() {
    String templateDataJson = "{'mysql_version':''}";

    ImmutableMap<String, String> resultMap = TemplateDataUtil
        .parseTemplateDataJson(templateDataJson);

    assertThat(resultMap).containsExactly("mysql_version", "");
  }

  @Test
  public void parseTemplateData_whenJsonNotMap_failed() {
    String templateDataJson = "{'mysql_version':['5.6','1.8']}";

    assertThrows(
        JsonSyntaxException.class, () -> TemplateDataUtil.parseTemplateDataJson(templateDataJson));
  }
}
