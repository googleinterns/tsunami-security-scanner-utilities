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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Map;

/**
 * This class is used for transforming input template data Json String to Map.
 * Usage: TemplateDataUtil.parseTemplateDataJson(String templateDataJson);
 * Input:
 *      templateDataJson: input template data in Json String type.
 * Output:
 *      A map which reflects a correspondence of input json keys and values.
 * Deprecated usage: FreeMarkerUtil.replaceTemplates(String version, String password, File configFile);
 */
public final class TemplateDataUtil {

  private TemplateDataUtil() {}

  public static Map<String, String> parseTemplateDataJson(String templateDataJson) {

    GsonBuilder builder = new GsonBuilder();
    builder.setPrettyPrinting();

    Gson gson = builder.create();
    Map<String, String> templateDataMap = gson.fromJson(templateDataJson, Map.class);

    return templateDataMap;
  }
}
