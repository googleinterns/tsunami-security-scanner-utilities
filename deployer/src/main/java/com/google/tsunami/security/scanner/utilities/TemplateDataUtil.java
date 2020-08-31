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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;

/**
 * This class is used for transforming input template data Json String to Map. Usage:
 * TemplateDataUtil.parseTemplateDataJson(String templateDataJson); Input: templateDataJson: input
 * template data in Json String type. Output: A map which reflects a correspondence of input json
 * keys and values. Deprecated usage: FreeMarkerUtil.replaceTemplates(String version, String
 * password, File configFile);
 */
public final class TemplateDataUtil {

  private TemplateDataUtil() {}

  public static ImmutableMap<String, String> parseTemplateDataJson(String templateDataJson) {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    Type type = new TypeToken<Map<String, String>>() {}.getType();
    return Optional.ofNullable(
            gson.<Map<String, String>>fromJson(Strings.nullToEmpty(templateDataJson), type))
        .map(ImmutableMap::copyOf)
        .orElse(ImmutableMap.of());
  }
}
