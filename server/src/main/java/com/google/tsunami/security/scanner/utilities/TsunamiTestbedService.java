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

/** Implements the Tsunami Testbed GRPC service. */
public final class TsunamiTestbedService extends TsunamiTestbedGrpc.TsunamiTestbedImplBase {
  private final TsunamiTestbedUtil util;

  public TsunamiTestbedService(TsunamiTestbedUtil util) {
    this.util = util;
  }

  @Override
  public void createDeployment(
      CreateDeploymentRequest request, StreamObserver<CreateDeploymentResponse> responseObserver) {
    try {
      CreateDeploymentResponse response =
          CreateDeploymentResponse.newBuilder()
              .setJobId(
                  util.createDeployment(
                      request.getApplication(),
                      request.getConfigPath(),
                      request.getTemplateData(),
                      request.getDeployerJobPath()))
              .build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Throwable t) {
      responseObserver.onError(t);
    }
  }

  @Override
  public void listApplications(
      Empty request, StreamObserver<ListApplicationsResponse> responseObserver) {
    try {
      ListApplicationsResponse response =
          ListApplicationsResponse.newBuilder().addAllApplications(util.listApplications()).build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Throwable t) {
      responseObserver.onError(t);
    }
  }

  @Override
  public void getApplication(
      GetApplicationRequest request, StreamObserver<GetApplicationResponse> responseObserver) {
    try {
      GetApplicationResponse response =
          GetApplicationResponse.newBuilder()
              .setServiceEndpoint(util.getApplication(request.getApplication()))
              .build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (Throwable t) {
      responseObserver.onError(t);
    }
  }
}
