/*
 * Copyright 2020 Google LLC
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */

package com.fdahpstudydesigner.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fdahpstudydesigner.bean.AuditLogEventRequest;
import com.fdahpstudydesigner.config.HibernateTestConfig;
import com.fdahpstudydesigner.config.WebAppTestConfig;
import com.fdahpstudydesigner.service.AuditEventService;
import com.fdahpstudydesigner.util.FdahpStudyDesignerConstants;
import com.fdahpstudydesigner.util.SessionObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Resource;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@PropertySource(value = {"classpath:application-mockit.properties"})
@WebAppConfiguration("src/main/webapp")
@ContextConfiguration(classes = {WebAppTestConfig.class, HibernateTestConfig.class})
@TestExecutionListeners({
  DependencyInjectionTestExecutionListener.class,
  DirtiesContextTestExecutionListener.class,
  TransactionalTestExecutionListener.class
})
public class BaseMockIT {

  private static Logger logger = Logger.getLogger(BaseMockIT.class);

  @Rule public TestName testName = new TestName();

  @Resource private WebApplicationContext webAppContext;

  @Autowired private AuditEventService mockAuditService;

  protected final String CONTEXT_PATH = "/studybuilder";

  protected final String SESSION_USER_EMAIL = "mystudies_mockit@grr.la";

  protected MockMvc mockMvc;

  @Value("${applicationVersion}")
  protected String applicationVersion;

  protected List<AuditLogEventRequest> auditRequests = new ArrayList<>();

  @Before
  public void setUp() {

    logger.debug(String.format("BEGIN TEST: %s", testName.getMethodName()));

    mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext).build();
    initSecurityContext();

    doAnswer(
            new Answer<AuditLogEventRequest>() {

              @Override
              public AuditLogEventRequest answer(final InvocationOnMock invocation)
                  throws Throwable {
                AuditLogEventRequest auditRequest =
                    (AuditLogEventRequest) (invocation.getArguments())[0];
                auditRequests.add(SerializationUtils.clone(auditRequest));
                return auditRequest;
              }
            })
        .when(mockAuditService)
        .postAuditLogEvent(Mockito.any(AuditLogEventRequest.class));
  }

  @After
  public void cleanUp() {
    auditRequests.clear();

    logger.debug(String.format("END TEST: %s", testName.getMethodName()));
  }

  @Test
  public void testMockMvcAndWebAppContext() {
    assertNotNull(webAppContext);
    assertNotNull(mockMvc);
  }

  protected void clearAuditRequests() {
    auditRequests.clear();
  }

  protected SessionObject getSessionObject() {
    SessionObject session = new SessionObject();
    session.setSessionId(UUID.randomUUID().toString());
    session.setEmail(SESSION_USER_EMAIL);
    session.setStudySession(new ArrayList<>(Arrays.asList(0)));
    return session;
  }

  protected HashMap<String, Object> getSessionAttributes() {
    HashMap<String, Object> sessionAttributes = new HashMap<String, Object>();
    sessionAttributes.put(FdahpStudyDesignerConstants.SESSION_OBJECT, getSessionObject());
    return sessionAttributes;
  }

  protected void initSecurityContext() {
    Authentication authentication = Mockito.mock(Authentication.class);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);
  }

  protected HttpHeaders getCommonHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.add("correlationId", UUID.randomUUID().toString());

    headers.add("appVersion", applicationVersion);
    headers.add("appId", "STUDY BUILDER");
    headers.add("source", "STUDY BUILDER");
    headers.add("mobilePlatform", "UNKNOWN");
    return headers;
  }

  /**
   * @param assertOptionalFieldsForEvent is a {@link Map} collection that contains {@link eventCode}
   *     as key and {@link AuditLogEventRequest} with optional field values as value.
   * @param auditEvents audit event enums
   */
  protected void verifyAuditEventCall(
      Map<String, AuditLogEventRequest> assertOptionalFieldsForEvent,
      StudyBuilderAuditEvent... auditEvents) {

    verifyAuditEventCall(auditEvents);

    Map<String, AuditLogEventRequest> auditRequestByEventCode = new HashMap<>();
    for (AuditLogEventRequest auditRequest : auditRequests) {
      auditRequestByEventCode.put(auditRequest.getEventCode(), auditRequest);
    }

    for (Map.Entry<String, AuditLogEventRequest> entry : assertOptionalFieldsForEvent.entrySet()) {
      String eventCode = entry.getKey();
      AuditLogEventRequest expectedAuditRequest = entry.getValue();
      AuditLogEventRequest auditRequest = auditRequestByEventCode.get(eventCode);
      assertEquals(expectedAuditRequest.getUserId(), auditRequest.getUserId());
      assertEquals(expectedAuditRequest.getParticipantId(), auditRequest.getParticipantId());
      assertEquals(expectedAuditRequest.getStudyId(), auditRequest.getStudyId());
      assertEquals(expectedAuditRequest.getStudyVersion(), auditRequest.getStudyVersion());
    }
  }

  protected void verifyAuditEventCall(StudyBuilderAuditEvent... auditEvents) {
    ArgumentCaptor<AuditLogEventRequest> argument =
        ArgumentCaptor.forClass(AuditLogEventRequest.class);
    verify(mockAuditService, atLeastOnce()).postAuditLogEvent(argument.capture());

    Map<String, AuditLogEventRequest> auditRequestByEventCode = new HashMap<>();
    for (AuditLogEventRequest auditRequest : auditRequests) {
      auditRequestByEventCode.put(auditRequest.getEventCode(), auditRequest);
    }

    for (StudyBuilderAuditEvent auditEvent : auditEvents) {
      AuditLogEventRequest auditRequest = auditRequestByEventCode.get(auditEvent.getEventCode());

      assertEquals(auditEvent.getEventCode(), auditRequest.getEventCode());
      if (auditEvent.getDestination() != null) {
        assertEquals(auditEvent.getDestination().getValue(), auditRequest.getDestination());
      }

      if (auditEvent.getSource() != null) {
        assertEquals(auditEvent.getSource().getValue(), auditRequest.getSource());
      }

      if (auditEvent.getResourceServer() != null) {
        assertEquals(auditEvent.getResourceServer().getValue(), auditRequest.getResourceServer());
      }

      assertFalse(
          StringUtils.contains(auditRequest.getDescription(), "{")
              && StringUtils.contains(auditRequest.getDescription(), "}"));
      assertNotNull(auditRequest.getCorrelationId());
      assertNotNull(auditRequest.getOccured());
      assertNotNull(auditRequest.getPlatformVersion());
      assertNotNull(auditRequest.getAppId());
      assertNotNull(auditRequest.getAppVersion());
      assertNotNull(auditRequest.getMobilePlatform());
    }
  }

  protected void addParams(MockHttpServletRequestBuilder requestBuilder, Object formModel)
      throws Exception {
    ObjectMapper objectMapper = JsonUtils.getObjectMapper();
    objectMapper.setSerializationInclusion(Include.NON_NULL);
    objectMapper.setSerializationInclusion(Include.NON_EMPTY);
    objectMapper.setSerializationInclusion(Include.NON_DEFAULT);

    ObjectReader reader = objectMapper.reader(Map.class);
    Map<String, String> map = reader.readValue(objectMapper.writeValueAsString(formModel));
    for (Map.Entry<String, String> entry : map.entrySet()) {
      String value = String.valueOf(entry.getValue());
      if (StringUtils.isNotEmpty(value) && !StringUtils.equals(value, "null")) {
        requestBuilder.param(entry.getKey(), value);
      }
    }
  }
}