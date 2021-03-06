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

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

public class ValidPort implements IParameterValidator {
  public void validate(String name, String value) throws ParameterException {
    try {
      int port = Integer.parseInt(value);
      if (port < 0 || port > 65535) {
        throw new NumberFormatException();
      }
    } catch (NumberFormatException e) {
      throw new ParameterException(
          "Parameter " + name + " is not valid, should be from 0 to 65535 (found " + value + ")");
    }
  }
}
