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
import io.grpc.stub.StreamObserver;

/**
 * Implements the Tsunami Testbed GRPC service.
 */
public final class TsunamiTestbedService extends TsunamiTestbedGrpc.TsunamiTestbedImplBase {
  private final TsunamiTestbedUtil util;

  public TsunamiTestbedService(TsunamiTestbedUtil util) {
    this.util = util;
  }

  @Override
  public void createDeployment(
      CreateDeploymentRequest request, StreamObserver<CreateDeploymentResponse> responseObserver) {
    CreateDeploymentResponse response;
    try {
      String curResponse =
          util.createDeployment(
              request.getApplication(),
              request.getConfigPath(),
              request.getTemplateData(),
              request.getDeployerJobPath());
      response = CreateDeploymentResponse.newBuilder().setJobId(curResponse).build();
    } catch (Throwable t) {
      responseObserver.onError(t);
      return;
    }

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void listApplications(
      Empty request, StreamObserver<ListApplicationsResponse> responseObserver) {
    ListApplicationsResponse response;
    try {
      response =
          ListApplicationsResponse.newBuilder().addAllApplications(util.listApplications()).build();
    } catch (Throwable t) {
      responseObserver.onError(t);
      return;
    }

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void getApplication(
      GetApplicationRequest request, StreamObserver<GetApplicationResponse> responseObserver) {
    GetApplicationResponse response;
    try {
      response =
          GetApplicationResponse.newBuilder()
              .setServiceEndpoint(util.getServiceEndpoint(request.getApplication()))
              .build();
    } catch (Throwable t) {
      responseObserver.onError(t);
      return;
    }

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }
}
