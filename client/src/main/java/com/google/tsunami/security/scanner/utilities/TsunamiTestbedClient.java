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
import com.google.common.base.Strings;
import com.google.common.flogger.GoogleLogger;
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
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  private static final String DEFAULT_ADDRESS = "localhost:8000";

  public static void main(String[] args) {
    TsunamiTestbedClientArgs clientArgs = new TsunamiTestbedClientArgs();
    JCommander jCommander = JCommander.newBuilder().addObject(clientArgs).build();
    jCommander.parse(args);
    if (clientArgs.help) {
      jCommander.usage();
      return;
    }

    String address = clientArgs.address;
    String apiKey = clientArgs.apiKey;
    String authToken = clientArgs.authToken;
    String operation = clientArgs.operation;
    String appName = clientArgs.app;
    String templateData = Strings.nullToEmpty(clientArgs.templateData);

    // Create gRPC stub.
    TsunamiTestbedGrpc.TsunamiTestbedBlockingStub testbed =
        createTestbedStub(address, apiKey, authToken);

    switch (operation) {
      case "createDeployment":
        createDeployment(testbed, appName, templateData);
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
      String templateData) {
    CreateDeploymentRequest createDeploymentRequest =
        CreateDeploymentRequest.newBuilder()
            .setApplication(appName)
            .setTemplateData(templateData)
            .build();
    CreateDeploymentResponse response = tsunamiTestbed.createDeployment(createDeploymentRequest);
    logger.atInfo().log("CreateDeployment response = \n%s", response);
  }

  static void listApplications(TsunamiTestbedGrpc.TsunamiTestbedBlockingStub tsunamiTestbed) {
    ListApplicationsResponse response = tsunamiTestbed.listApplications(Empty.getDefaultInstance());
    logger.atInfo().log("ListApplications response = \n%s", response);
  }

  static void getApplication(
      TsunamiTestbedGrpc.TsunamiTestbedBlockingStub tsunamiTestbed, String appName) {
    GetApplicationRequest getApplicationRequest =
        GetApplicationRequest.newBuilder().setApplication(appName).build();
    GetApplicationResponse response = tsunamiTestbed.getApplication(getApplicationRequest);
    logger.atInfo().log("GetApplication response = \n%s", response);
  }

  private static final class CredentialInterceptor implements ClientInterceptor {
    private static Metadata.Key<String> API_KEY_HEADER =
        Metadata.Key.of("x-api-key", Metadata.ASCII_STRING_MARSHALLER);
    private static Metadata.Key<String> AUTHORIZATION_HEADER =
        Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);

    private final String apiKey;
    private final String authToken;

    public CredentialInterceptor(String apiKey, String authToken) {
      this.apiKey = apiKey;
      this.authToken = authToken;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
        MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
      logger.atInfo().log("Intercepted %s for credentials.", method.getFullMethodName());
      ClientCall<ReqT, RespT> call = next.newCall(method, callOptions);

      call =
          new ForwardingClientCall.SimpleForwardingClientCall<>(call) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
              if (!Strings.isNullOrEmpty(apiKey)) {
                logger.atInfo().log("Attaching API Key: %s.", apiKey);
                headers.put(API_KEY_HEADER, apiKey);
              }
              if (!Strings.isNullOrEmpty(authToken)) {
                logger.atInfo().log("Attaching auth token: %s.", authToken);
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
    channel = ClientInterceptors.intercept(channel, new CredentialInterceptor(apiKey, authToken));
    return TsunamiTestbedGrpc.newBlockingStub(channel);
  }

  @Parameters(separators = "=")
  private static class TsunamiTestbedClientArgs {
    @Parameter(
        names = "--tsunami_testbed",
        description = "The address of the Tsunami Testbed server")
    public String address = DEFAULT_ADDRESS;

    @Parameter(names = "--api_key", description = "The API key to use for RPC calls.")
    public String apiKey;

    @Parameter(names = "--auth_token", description = "The auth token to use for RPC calls.")
    public String authToken;

    @Parameter(
        names = "--operation",
        description =
            "The tsunamiTestbed operation to perform: createDeployment|listApplications|getApplication")
    public String operation = "listApplications";

    @Parameter(names = "--app_name", description = "Application's name.")
    public String app;

    @Parameter(
        names = "--template_data",
        description = "Template Data needs to be substituted in Json String type.")
    public String templateData;

    @Parameter(
        names = {"--help", "-h"},
        description = "Print parameters and description.",
        help = true)
    public boolean help = false;
  }
}
