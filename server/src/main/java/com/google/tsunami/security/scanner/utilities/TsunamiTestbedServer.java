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

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.util.Config;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Builds and starts a GRPC-based Tsunami Testbed server.
 */
public final class TsunamiTestbedServer {

  private static final int DEFAULT_PORT = 8000;

  public static void main(String[] args) throws Exception {
    Options options = createOptions();
    CommandLineParser parser = new DefaultParser();
    CommandLine line;
    try {
      line = parser.parse(options, args);
    } catch (ParseException e) {
      System.err.println("Invalid command line: " + e.getMessage());
      printUsage(options);
      return;
    }

    //////////////////////////////////////////

    ApiClient client = Config.defaultClient();
    Configuration.setDefaultApiClient(client);
    System.out.println("Initialized Kubernetes Api Client.");

    int port = DEFAULT_PORT;

    if (line.hasOption("port")) {
      String portOption = line.getOptionValue("port");
      try {
        port = Integer.parseInt(portOption);
      } catch (java.lang.NumberFormatException e) {
        System.err.println("Invalid port number: " + portOption);
        printUsage(options);
        return;
      }
    }

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

  private static Options createOptions() {
    Options options = new Options();

    // port
    options.addOption(
        Option.builder()
            .longOpt("port")
            .desc("The port on which the server listens.")
            .hasArg()
            .argName("port")
            .type(Integer.class)
            .build());

    return options;
  }

  private static void printUsage(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(
        "client",
        "A simple Tsunami Testbed gRPC server for use with Endpoints.",
        options,
        "",
        true);
  }
}
