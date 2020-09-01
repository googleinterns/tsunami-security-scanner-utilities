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

import com.google.common.collect.ImmutableMap;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

/**
 * This class is used for replace template arguments in template config files.
 */
public final class FreeMarkerUtil {

  private FreeMarkerUtil() {
  }

  /**
   * Replace template parameters in {@code configFile} with the {@code templateData}.
   */
  public static String replaceTemplates(ImmutableMap<String, String> templateData,
      String resourcePath)
      throws IOException, TemplateException {

    Configuration config = new Configuration(Configuration.VERSION_2_3_30);
    config.setDefaultEncoding("UTF-8");
    config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    config.setLogTemplateExceptions(false);
    config.setWrapUncheckedExceptions(true);
    config.setFallbackOnNullLoopVariable(false);
    config.setClassForTemplateLoading(FreeMarkerUtil.class, "/");

    StringWriter outputWriter = new StringWriter();
    config.getTemplate(resourcePath).process(templateData, outputWriter);
    return outputWriter.toString();
  }
}
