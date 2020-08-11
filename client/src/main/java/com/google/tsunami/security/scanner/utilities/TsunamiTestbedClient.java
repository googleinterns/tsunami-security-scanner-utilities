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

import com.google.protobuf.Empty;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ClientInterceptors;
import io.grpc.ForwardingClientCall;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/** A client application which calls the Testbed API over gRPC. */
public final class TsunamiTestbedClient {

  private static final String DEFAULT_ADDRESS = "localhost:8000";

  public static void main(String[] args) throws Exception {
    Options options = createOptions();
    CommandLineParser parser = new DefaultParser();
    CommandLine params;
    try {
      params = parser.parse(options, args);
    } catch (ParseException e) {
      System.err.println("Invalid command line: " + e.getMessage());
      printUsage(options);
      return;
    }

    String address = params.getOptionValue("testbed", DEFAULT_ADDRESS);
    String apiKey = params.getOptionValue("api_key");
    String authToken = params.getOptionValue("auth_token");
    String operation = params.getOptionValue("operation", "list");
    String appName = params.getOptionValue("application", "jupyter");
    String configPath = params.getOptionValue("config_path", "/application");
    String templateData =
        params.getOptionValue("template_data", "\"{'jupyter_version':'notebook-6.0.3'}\"");
    String deployerJobPath = params.getOptionValue("deployter_job_path", ".");

    // Create gRPC stub.
    TsunamiTestbedGrpc.TsunamiTestbedBlockingStub testbed =
        createTestbedStub(address, apiKey, authToken);

    // Run different requests based on operation input.
    if ("createDeployment".equals(operation)) {
      createDeployment(testbed, appName, configPath, templateData, deployerJobPath);
    } else if ("listApplications".equals(operation)) {
      listApplications(testbed);
    } else if ("getApplication".equals(operation)) {
      getApplication(testbed, appName);
    }
  }

  static void createDeployment(
      TsunamiTestbedGrpc.TsunamiTestbedBlockingStub tsunamiTestbed,
      String appName,
      String configPath,
      String templateData,
      String deployerJobPath) {
    CreateDeploymentRequest.Builder builder = CreateDeploymentRequest.newBuilder();
    builder
        .setApplication(appName)
        .setConfigPath(configPath)
        .setTemplateData(templateData)
        .setDeployerJobPath(deployerJobPath);
    CreateDeploymentResponse response = tsunamiTestbed.createDeployment(builder.build());
    System.out.println(response);
  }

  static void listApplications(TsunamiTestbedGrpc.TsunamiTestbedBlockingStub tsunamiTestbed) {
    ListApplicationsResponse response = tsunamiTestbed.listApplications(Empty.getDefaultInstance());
    System.out.println(response);
  }

  static void getApplication(
      TsunamiTestbedGrpc.TsunamiTestbedBlockingStub tsunamiTestbed, String appName) {
    GetApplicationRequest.Builder builder = GetApplicationRequest.newBuilder();
    builder.setApplication(appName);
    GetApplicationResponse response = tsunamiTestbed.getApplication(builder.build());
    System.out.println(response);
  }

  private static final class Interceptor implements ClientInterceptor {
    private final String apiKey;
    private final String authToken;

    private static Logger LOGGER = Logger.getLogger("InfoLogging");

    private static Metadata.Key<String> API_KEY_HEADER =
        Metadata.Key.of("x-api-key", Metadata.ASCII_STRING_MARSHALLER);
    private static Metadata.Key<String> AUTHORIZATION_HEADER =
        Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);

    public Interceptor(String apiKey, String authToken) {
      this.apiKey = apiKey;
      this.authToken = authToken;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
        MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
      LOGGER.info("Intercepted " + method.getFullMethodName());
      ClientCall<ReqT, RespT> call = next.newCall(method, callOptions);

      call =
          new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(call) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
              if (apiKey != null && !apiKey.isEmpty()) {
                LOGGER.info("Attaching API Key: " + apiKey);
                headers.put(API_KEY_HEADER, apiKey);
              }
              if (authToken != null && !authToken.isEmpty()) {
                System.out.println("Attaching auth token");
                headers.put(AUTHORIZATION_HEADER, "Bearer " + authToken);
              }
              super.start(responseListener, headers);
            }
          };
      return call;
    }
  }

  static TsunamiTestbedGrpc.TsunamiTestbedBlockingStub createTestbedStub(
      String address, String apiKey, String authToken) {
    Channel channel = ManagedChannelBuilder.forTarget(address).usePlaintext(true).build();

    channel = ClientInterceptors.intercept(channel, new Interceptor(apiKey, authToken));

    return TsunamiTestbedGrpc.newBlockingStub(channel);
  }

  private static Options createOptions() {
    Options options = new Options();

    // tsunamiTestbed
    options.addOption(
        Option.builder()
            .longOpt("tsunami_testbed")
            .desc("The address of the Tsunami Testbed server")
            .hasArg()
            .argName("address")
            .type(String.class)
            .build());

    // api_key
    options.addOption(
        Option.builder()
            .longOpt("api_key")
            .desc("The API key to use for RPC calls")
            .hasArg()
            .argName("key")
            .type(String.class)
            .build());

    // auth_token
    options.addOption(
        Option.builder()
            .longOpt("auth_token")
            .desc("The auth token to use for RPC calls")
            .hasArg()
            .argName("token")
            .type(String.class)
            .build());

    // operation
    options.addOption(
        Option.builder()
            .longOpt("operation")
            .desc(
                "The tsunamiTestbed operation to perform: createDeployment|listApplications|getApplication")
            .hasArg()
            .argName("op")
            .type(String.class)
            .build());

    options.addOption(
        Option.builder()
            .longOpt("application")
            .desc("App name")
            .hasArg()
            .argName("ap")
            .type(String.class)
            .build());

    options.addOption(
        Option.builder()
            .longOpt("config_path")
            .desc("Path of config files.")
            .hasArg()
            .argName("cp")
            .type(String.class)
            .build());

    options.addOption(
        Option.builder()
            .longOpt("template_data")
            .desc("Template Data in json format")
            .hasArg()
            .argName("td")
            .type(String.class)
            .build());

    options.addOption(
        Option.builder()
            .longOpt("deployer_job_path")
            .desc("Path of deployer job's yaml file. Example: ${project_dir}/deployer_job.yaml .")
            .hasArg()
            .argName("jp")
            .type(String.class)
            .build());

    return options;
  }

  private static void printUsage(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(
        "client",
        "A simple Tsunami Testbed gRPC client for use with Endpoints.",
        options,
        "",
        true);
  }
}
