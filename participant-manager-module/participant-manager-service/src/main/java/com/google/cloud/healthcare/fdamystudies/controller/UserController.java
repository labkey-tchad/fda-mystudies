/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.google.cloud.healthcare.fdamystudies.controller;

import com.google.cloud.healthcare.fdamystudies.beans.AdminUserResponse;
import com.google.cloud.healthcare.fdamystudies.beans.ManageUsersResponse;
import com.google.cloud.healthcare.fdamystudies.beans.UserRequest;
import com.google.cloud.healthcare.fdamystudies.service.ManageUserService;
import com.google.cloud.healthcare.fdamystudies.service.UserProfileService;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

  private XLogger logger = XLoggerFactory.getXLogger(UserController.class.getName());

  private static final String BEGIN_REQUEST_LOG = "%s request";

  private static final String EXIT_STATUS_LOG = "status=%d";

  @Autowired private ManageUserService manageUserService;

  @Autowired private UserProfileService userProfileService;

  @PostMapping(
      value = "/users",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<AdminUserResponse> addNewUserSiteCoordinator(
      @Valid @RequestBody UserRequest user,
      @RequestHeader(name = "userId") String superAdminUserId,
      HttpServletRequest request) {
    logger.entry(String.format(BEGIN_REQUEST_LOG, request.getRequestURI()));
    user.setSuperAdminUserId(superAdminUserId);
    AdminUserResponse userResponse = manageUserService.createUser(user);
    logger.exit(String.format(EXIT_STATUS_LOG, userResponse.getHttpStatusCode()));
    return ResponseEntity.status(userResponse.getHttpStatusCode()).body(userResponse);
  }

  @PutMapping(
      value = "/users/{superAdminUserId}/",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<AdminUserResponse> updateUserSiteCoordinator(
      @Valid @RequestBody UserRequest user,
      @PathVariable String superAdminUserId,
      HttpServletRequest request) {
    logger.entry(String.format(BEGIN_REQUEST_LOG, request.getRequestURI()));
    AdminUserResponse userResponse = manageUserService.updateUser(user, superAdminUserId);
    logger.exit(String.format(EXIT_STATUS_LOG, userResponse.getHttpStatusCode()));
    return ResponseEntity.status(userResponse.getHttpStatusCode()).body(userResponse);
  }

  /* @PostMapping(
      value = "/users/",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<SetUpAccountResponse> setUpAccount(
      @Valid @RequestBody SetUpAccountRequest setUpAccountRequest, HttpServletRequest request) {
    logger.entry(String.format(BEGIN_REQUEST_LOG, request.getRequestURI()));

    SetUpAccountResponse setUpAccountResponse = userProfileService.saveUser(setUpAccountRequest);

    logger.exit(String.format(EXIT_STATUS_LOG, setUpAccountResponse.getHttpStatusCode()));
    return ResponseEntity.status(setUpAccountResponse.getHttpStatusCode())
        .body(setUpAccountResponse);
  }*/

  @GetMapping(
      value = {"/users", "/users/{adminId}"},
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> manageUsers(
      @RequestHeader("userId") String userId,
      @PathVariable(value = "adminId", required = false) String adminId,
      HttpServletRequest request) {
    logger.entry(String.format(BEGIN_REQUEST_LOG, request.getRequestURI()));
    ManageUsersResponse userResponse = manageUserService.getUsers(userId, adminId);
    logger.exit(String.format(EXIT_STATUS_LOG, userResponse.getHttpStatusCode()));
    return ResponseEntity.status(userResponse.getHttpStatusCode()).body(userResponse);
  }
}
