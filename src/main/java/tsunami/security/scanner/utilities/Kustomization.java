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

import org.graalvm.compiler.lir.LIR;

import java.util.List;

public class Kustomization {
    private List<String> name;
    private List<String> literals;
    private List<String> resources;

    public List<String> getName(){
        return name;
    }

    public void setName(List<String> name){
        this.name = name;
    }

    public List<String> getLiterals(){
        return literals;
    }

    public void setLiterals(List<String> literals){
        this.literals = literals;
    }

    public List<String> getResources(){
        return resources;
    }

    public void setResources(List<String> resources){
        this.resources = resources;
    }
} 
