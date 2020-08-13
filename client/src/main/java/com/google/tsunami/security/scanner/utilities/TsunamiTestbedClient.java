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

/** A client application which calls the Testbed API over gRPC. */
public final class TsunamiTestbedClient {

  private static final String DEFAULT_ADDRESS = "localhost:8000";

  private static class TsunamiTestbedClientArgs {
    @Parameter(
        names = "--tsunami_testbed",
        description = "The address of the Tsunami Testbed server")
    private String address = DEFAULT_ADDRESS;

    @Parameter(names = "--api_key", description = "The API key to use for RPC calls.")
    private String apiKey;

    @Parameter(names = "--auth_token", description = "The auth token to use for RPC calls.")
    private String authToken;

    @Parameter(
        names = "--operation",
        description =
            "The tsunamiTestbed operation to perform: createDeployment|listApplications|getApplication")
    private String operation = "listApplications";

    @Parameter(names = "--app", description = "Application's name.")
    private String app = "jupyter";

    @Parameter(names = "--config_path", description = "Path of application config files.")
    private String configPath = "/application";

    @Parameter(
        names = "--template_data",
        description = "Template Data needs to be substituted in Json String type.")
    private String templateData = "\"{'jupyter_version':'notebook-6.0.3'}\"";

    @Parameter(names = "--deployer_job_path", description = "Path for deployer job yaml file.")
    private String deployerJobPath = ".";

    @Parameter(
        names = {"--help", "-h"},
        description = "Print parameters and description.",
        help = true)
    private boolean help = false;

    public String getAddress() {
      return address;
    }

    public String getApiKey() {
      return apiKey;
    }

    public String getAuthToken() {
      return authToken;
    }

    public String getOperation() {
      return operation;
    }

    public String getApp() {
      return app;
    }

    public String getConfigPath() {
      return configPath;
    }

    public String getTemplateData() {
      return templateData;
    }

    public String getDeployerJobPath() {
      return deployerJobPath;
    }

    public boolean isHelp() {
      return help;
    }
  }

  public static void main(String[] args) throws Exception {
    TsunamiTestbedClientArgs clientArgs = new TsunamiTestbedClientArgs();
    JCommander jCommander = JCommander.newBuilder().addObject(clientArgs).build();
    try {
      jCommander.parse(args);
      if (clientArgs.isHelp()) {
        jCommander.usage();
        return;
      }
    } catch (ParameterException e) {
      System.err.println("Invalid command line: " + e.getMessage());
      return;
    }

    String address = clientArgs.getAddress();
    String apiKey = clientArgs.getApiKey();
    String authToken = clientArgs.getAuthToken();
    String operation = clientArgs.getOperation();
    String appName = clientArgs.getApp();
    String configPath = clientArgs.getConfigPath();
    String templateData = clientArgs.getTemplateData();
    String deployerJobPath = clientArgs.getDeployerJobPath();

    // Create gRPC stub.
    TsunamiTestbedGrpc.TsunamiTestbedBlockingStub testbed =
        createTestbedStub(address, apiKey, authToken);

    switch (operation) {
      case "createDevelopment":
        createDeployment(testbed, appName, configPath, templateData, deployerJobPath);
        break;
      case "listApplications":
        listApplications(testbed);
        break;
      case "getApplication":
        getApplication(testbed, appName);
        break;
    }
  }

  static void createDeployment(
      TsunamiTestbedGrpc.TsunamiTestbedBlockingStub tsunamiTestbed,
      String appName,
      String configPath,
      String templateData,
      String deployerJobPath) {
    CreateDeploymentRequest createDeploymentRequest =
        CreateDeploymentRequest.newBuilder()
            .setApplication(appName)
            .setConfigPath(configPath)
            .setTemplateData(templateData)
            .setDeployerJobPath(deployerJobPath)
            .build();
    CreateDeploymentResponse response = tsunamiTestbed.createDeployment(createDeploymentRequest);
    System.out.println(response);
  }

  static void listApplications(TsunamiTestbedGrpc.TsunamiTestbedBlockingStub tsunamiTestbed) {
    ListApplicationsResponse response = tsunamiTestbed.listApplications(Empty.getDefaultInstance());
    System.out.println(response);
  }

  static void getApplication(
      TsunamiTestbedGrpc.TsunamiTestbedBlockingStub tsunamiTestbed, String appName) {
    GetApplicationRequest getApplicationRequest =
        GetApplicationRequest.newBuilder().setApplication(appName).build();
    GetApplicationResponse response = tsunamiTestbed.getApplication(getApplicationRequest);
    System.out.println(response);
  }

  private static final class Interceptor implements ClientInterceptor {
    private static Logger LOGGER = Logger.getLogger("InfoLogging");

    private static Metadata.Key<String> API_KEY_HEADER =
        Metadata.Key.of("x-api-key", Metadata.ASCII_STRING_MARSHALLER);
    private static Metadata.Key<String> AUTHORIZATION_HEADER =
        Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);

    private final String apiKey;
    private final String authToken;

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
}
