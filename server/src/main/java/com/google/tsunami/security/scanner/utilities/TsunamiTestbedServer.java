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

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.util.Config;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/** Builds and starts a GRPC-based Tsunami Testbed server. */
public final class TsunamiTestbedServer {

  private static final int DEFAULT_PORT = 8000;

  private static class TsunamiTestbedServerArgs {
    @Parameter(
        names = "--port",
        description = "The port on which the server listens.",
        validateWith = ValidPort.class)
    private int port = DEFAULT_PORT;

    @Parameter(
        names = {"--help", "-h"},
        description = "Print parameters and description.",
        help = true)
    private boolean help = false;

    public int getPort() {
      return port;
    }

    public boolean isHelp() {
      return help;
    }
  }

  public static void main(String[] args) throws Exception {
    TsunamiTestbedServerArgs serverArgs = new TsunamiTestbedServerArgs();
    JCommander jCommander = JCommander.newBuilder().addObject(serverArgs).build();
    try {
      jCommander.parse(args);
      if (serverArgs.isHelp()) {
        jCommander.usage();
        return;
      }
    } catch (ParameterException e) {
      System.err.println("Invalid command line: " + e.getMessage());
      return;
    }

    int port = serverArgs.getPort();

    ApiClient client = Config.defaultClient();
    Configuration.setDefaultApiClient(client);
    System.out.println("Initialized Kubernetes Api Client.");

    final TsunamiTestbedUtil util = new TsunamiTestbedUtil();
    final TsunamiTestbedServer server = new TsunamiTestbedServer();
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread() {
              @Override
              public void run() {
                try {
                  System.out.println("Shutting down");
                  server.stop();
                } catch (Exception e) {
                  e.printStackTrace();
                }
              }
            });
    server.start(port, util);
    System.out.format("Testbed service listening on %d\n", port);
    server.blockUntilShutdown();
  }

  private Server server;

  private void start(int port, TsunamiTestbedUtil util) throws IOException {
    server =
        ServerBuilder.forPort(port).addService(new TsunamiTestbedService(util)).build().start();
  }

  private void stop() throws Exception {
    server.shutdownNow();
    if (!server.awaitTermination(5, TimeUnit.SECONDS)) {
      System.err.println("Timed out waiting for server shutdown");
    }
  }

  private void blockUntilShutdown() throws InterruptedException {
    if (server != null) {
      server.awaitTermination();
    }
  }
}
