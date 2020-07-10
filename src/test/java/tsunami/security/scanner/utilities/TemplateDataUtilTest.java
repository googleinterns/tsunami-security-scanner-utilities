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

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TemplateDataUtilTest {

  @Test
  public void testTemplateDataUtil() {
    String templateDataJson =
        "{'mysql_version':'5.6','password':'dfhieuwhfhdsfj','wordpress_version':'4.8-apache'}";

    Map<String, String> resultMap = TemplateDataUtil.parseTemplateDataJson(templateDataJson);

    Map<String, String> expectedMap = new HashMap<>();
    expectedMap.put("mysql_version", "5.6");
    expectedMap.put("password", "dfhieuwhfhdsfj");
    expectedMap.put("wordpress_version", "4.8-apache");

    assertThat(resultMap).isEqualTo(expectedMap);
  }
}
