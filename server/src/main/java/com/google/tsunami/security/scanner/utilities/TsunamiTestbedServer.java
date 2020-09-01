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
import com.beust.jcommander.Parameters;
import com.google.common.flogger.GoogleLogger;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.util.Config;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Builds and starts a GRPC-based Tsunami Testbed server.
 */
public final class TsunamiTestbedServer {

  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();
  private static final int DEFAULT_PORT = 8000;

  private final Server server;
  private final int port;

  TsunamiTestbedServer(int port, String deployerImage) {
    this.port = port;
    this.server = ServerBuilder.forPort(port)
        .addService(new TsunamiTestbedService(new TsunamiTestbedUtil(deployerImage))).build();
  }

  public static void main(String[] args) throws Exception {
    TsunamiTestbedServerArgs serverArgs = new TsunamiTestbedServerArgs();
    JCommander jCommander = JCommander.newBuilder().addObject(serverArgs).build();
    jCommander.parse(args);
    if (serverArgs.help) {
      jCommander.usage();
      return;
    }

    ApiClient client = Config.defaultClient();
    Configuration.setDefaultApiClient(client);
    logger.atInfo().log("Initialized Kubernetes Api Client.");

    TsunamiTestbedServer server =
        new TsunamiTestbedServer(serverArgs.port, serverArgs.deployerImage);
    server.start();
    server.blockUntilShutdown();
  }

  private void start() throws IOException {
    server.start();
    logger.atInfo().log("Testbed service listening on %d.", port);
    Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
  }

  private void stop() {
    try {
      logger.atInfo().log("Server shutting down.");
      server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      logger.atSevere().withCause(e).log("Server interrrupted while shutting down.");
    }
  }

  private void blockUntilShutdown() throws InterruptedException {
    if (server != null) {
      server.awaitTermination();
    }
  }

  @Parameters(separators = "=")
  private static final class TsunamiTestbedServerArgs {

    @Parameter(
        names = "--port",
        description = "The port on which the server listens.",
        validateWith = ValidPort.class)
    public int port = DEFAULT_PORT;

    @Parameter(
        names = "--deployer_image",
        description = "The name of the appliation deployer image", required = true)
    public String deployerImage;

    @Parameter(
        names = {"--help", "-h"},
        description = "Print parameters and description.",
        help = true)
    public boolean help = false;
  }
}
