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

import java.io.File;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class App {
    public String getGreeting() {
        return "Hello world.";
    }

    public static void main(String[] args) {
        /*System.out.println(new App().getGreeting());
         */

        /* TODO:change to system path + relative path */
        File file = new File("/usr/local/google/home/yuxinwu/workspace/tsunami-security-scanner-utilities/application/wordpress/kustomization.yaml");

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Kustomization kusto = mapper.readValue(file, Kustomization.class);

        System.out.println(kusto.getName());
    }
}
