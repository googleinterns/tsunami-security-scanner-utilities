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

import com.google.common.flogger.GoogleLogger;
import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1Job;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import java.util.Optional;

/**
 * Implements the Tsunami Testbed GRPC service.
 */
public final class TsunamiTestbedService extends TsunamiTestbedGrpc.TsunamiTestbedImplBase {

  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  private final TsunamiTestbedUtil util;

  public TsunamiTestbedService(TsunamiTestbedUtil util) {
    this.util = util;
  }

  @Override
  public void createDeployment(
      CreateDeploymentRequest request, StreamObserver<CreateDeploymentResponse> responseObserver) {
    try {
      logger.atInfo().log("[CreateDeployment] Received request '%s'.", request);
      V1Job createdJob = util.createDeployment(request.getApplication(), request.getTemplateData());
      CreateDeploymentResponse response =
          CreateDeploymentResponse.newBuilder()
              .setJobId(
                  Optional.ofNullable(createdJob.getMetadata())
                      .map(V1ObjectMeta::getUid)
                      .orElse("UNKNOWN_JOB_ID"))
              .setJobName(
                  Optional.ofNullable(createdJob.getMetadata())
                      .map(V1ObjectMeta::getName)
                      .orElse("UNKNOWN_JOB_NAME"))
              .build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (StatusException e) {
      logger.atSevere().withCause(e).log(
          "Failed creating new deployment for '%s'.", request.getApplication());
      responseObserver.onError(e);
    }
  }

  @Override
  public void listApplications(
      Empty request, StreamObserver<ListApplicationsResponse> responseObserver) {
    try {
      logger.atInfo().log("[ListApplications] Received request '%s'.", request);
      ListApplicationsResponse response =
          ListApplicationsResponse.newBuilder().addAllApplications(util.listApplications()).build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (StatusException e) {
      logger.atSevere().withCause(e).log("Failed listing applications.");
      responseObserver.onError(e);
    }
  }

  @Override
  public void getApplication(
      GetApplicationRequest request, StreamObserver<GetApplicationResponse> responseObserver) {
    try {
      logger.atInfo().log("[GetApplication] Received request '%s'.", request);
      GetApplicationResponse response =
          GetApplicationResponse.newBuilder()
              .setServiceEndpoint(util.getApplication(request.getApplication()))
              .build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (StatusException e) {
      logger.atSevere().withCause(e)
          .log("Failed fetching application info for '%s'.", request.getApplication());
      responseObserver.onError(e);
    }
  }
}
