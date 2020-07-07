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

import freemarker.template.*;
import java.io.*;
import java.util.*;

/**
 * This class is used for replace ${app_version} and ${password} parameters in template config files.
 * Usage: FreeMarkerUtil.replaceTemplates(Map<String, String> templateDataMap, File configFile);
 * Input:
 *      templateDataMap: Map of template data needs to be substituted, such as {mysql_version=5.6, password=dkkeoij}
 *      configFile: original config file template Output: A new config file after replacement.
 * Deprecated usage: FreeMarkerUtil.replaceTemplates(String version, String password, File configFile);
 */
public final class FreeMarkerUtil {

  static Configuration cfg = new Configuration(Configuration.VERSION_2_3_29);

  private FreeMarkerUtil() throws IOException {
    cfg.setDefaultEncoding("UTF-8");
    cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    cfg.setLogTemplateExceptions(false);
    cfg.setWrapUncheckedExceptions(true);
    cfg.setFallbackOnNullLoopVariable(false);
  }

  public static String replaceTemplates(Map<String, String> templateDataMap, File configFile)
      throws IOException, TemplateException {

    // Split input configFile into path and file name.
    String file = configFile.getName();
    String filePath = configFile.getPath();
    String path = filePath.substring(0, filePath.lastIndexOf("/"));
    System.out.println("file: " + file);
    System.out.println("path: " + path);

    // Set path for config templates
    cfg.setDirectoryForTemplateLoading(new File(path));

    // Get the template file.
    Template temp = cfg.getTemplate(file);

    StringWriter stringWriter = new StringWriter();
    temp.process(templateDataMap, stringWriter);

    // get the config in String format from the StringWriter
    String config = stringWriter.toString();
    System.out.println(config);

    return config;
  }

  // Deprecated
  public static String replaceTemplates(String version, String password, File configFile)
      throws IOException, TemplateException {

    // Create a data-model.
    Map<String, String> templateDataMap = new HashMap();
    templateDataMap.put("password", password);
    templateDataMap.put("mysql_version", version);

    // Split input configFile into path and file name.
    String file = configFile.getName();
    String filePath = configFile.getPath();
    String path = filePath.substring(0, filePath.lastIndexOf("/"));

    // Set path for config templates
    cfg.setDirectoryForTemplateLoading(new File(path));

    // Get the template file.
    Template temp = cfg.getTemplate(file);

    StringWriter stringWriter = new StringWriter();
    temp.process(templateDataMap, stringWriter);

    // get the config in String format from the StringWriter
    String config = stringWriter.toString();

    return config;
  }
}
