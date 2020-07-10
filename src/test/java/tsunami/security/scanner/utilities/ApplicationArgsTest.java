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
import static org.junit.Assert.assertThrows;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class ApplicationArgsTest {

  @Test
  public void parse_whenValidArgs_parsesSuccessfully() {
    String[] args =
        new String[] {
          "--app",
          "jupyter",
          "--configPath",
          "application",
          "--templateData",
          "{'jupyter_version':'notebook-6.0.3'}"
        };

    ApplicationArgs jArgs = new ApplicationArgs();
    JCommander cmd = JCommander.newBuilder().addObject(jArgs).build();
    cmd.parse(args);

    assertThat(jArgs.getName()).isEqualTo("jupyter");
    assertThat(jArgs.getConfigPath()).isEqualTo("application");
    assertThat(jArgs.getTemplateData()).isEqualTo("{'jupyter_version':'notebook-6.0.3'}");
  }

  @Test
  public void parse_whenInvalidFlags_parseFailed() {
    String[] args =
        new String[] {
          "--app",
          "jupyter",
          "--config",
          "application",
          "--templateData",
          "{'jupyter_version':'notebook-6.0.3'}"
        };

    ApplicationArgs jArgs = new ApplicationArgs();
    JCommander cmd = JCommander.newBuilder().addObject(jArgs).build();

    assertThrows(ParameterException.class, () -> cmd.parse(args));
  }

  @Test
  public void parse_whenExtraArgsPassedIn_parseFailed() {
    String[] args =
        new String[] {
          "--app",
          "jupyter",
          "--configPath",
          "application",
          "--templateData",
          "{'jupyter_version':'notebook-6.0.3'}",
          "extra"
        };

    ApplicationArgs jArgs = new ApplicationArgs();
    JCommander cmd = JCommander.newBuilder().addObject(jArgs).build();

    assertThrows(ParameterException.class, () -> cmd.parse(args));
  }

  @Test
  public void parse_whenArgsContentMissing_parseFailed() {
    String[] args =
        new String[] {"--app", "jupyter", "--configPath", "application", "--templateData"};

    ApplicationArgs jArgs = new ApplicationArgs();
    JCommander cmd = JCommander.newBuilder().addObject(jArgs).build();

    assertThrows(ParameterException.class, () -> cmd.parse(args));
  }
}
