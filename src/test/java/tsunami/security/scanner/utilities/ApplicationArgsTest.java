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
import static org.junit.Assert.assertThrows;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ApplicationArgsTest {

  @Test
  public void testApplicationArgs() {
    String[] args = setArgs();

    ApplicationArgs jArgs = new ApplicationArgs();
    JCommander cmd = JCommander.newBuilder().addObject(jArgs).build();
    cmd.parse(args);

    String appName = jArgs.getName();
    String appConfigPath = jArgs.getConfigPath();
    String templateData = jArgs.getTemplateData();

    assertThat(appName).isEqualTo(args[1]);
    assertThat(appConfigPath).isEqualTo(args[3]);
    assertThat(templateData).isEqualTo(args[5]);
  }

  @Test
  public void testApplicationArgs_whenInputFlagIsInvalid() {
    String[] args = setArgs();
    args[2] = "--config";

    ApplicationArgs jArgs = new ApplicationArgs();
    JCommander cmd = JCommander.newBuilder().addObject(jArgs).build();

    assertThrows(ParameterException.class, () -> cmd.parse(args));
  }

  @Test
  public void testApplicationArgs_whenExtraArgsPassedIn() {
    String[] args = setArgs();
    String[] newArgs = new String[7];
    for (int idx = 0; idx < 6; idx++) {
      newArgs[idx] = args[idx];
    }
    newArgs[6] = "extra";

    ApplicationArgs jArgs = new ApplicationArgs();
    JCommander cmd = JCommander.newBuilder().addObject(jArgs).build();

    assertThrows(ParameterException.class, () -> cmd.parse(newArgs));
  }

  @Test
  public void testApplicationArgs_whenArgsContentMissing() {
    String[] args = setArgs();
    String[] newArgs = new String[5];
    for (int idx = 0; idx < 5; idx++) {
      newArgs[idx] = args[idx];
    }

    ApplicationArgs jArgs = new ApplicationArgs();
    JCommander cmd = JCommander.newBuilder().addObject(jArgs).build();

    assertThrows(ParameterException.class, () -> cmd.parse(newArgs));
  }

  private String[] setArgs() {
    String[] args = new String[6];
    args[0] = "--app";
    args[1] = "jupyter";
    args[2] = "--configPath";
    args[3] = System.getProperty("user.dir") + "/application";
    args[4] = "--templateData";
    args[5] = "{'jupyter_version':'0.0.3'}";
    return args;
  }
}
