package com.google.cloud.healthcare.fdamystudies.common;

import com.google.cloud.storage.Storage;
import org.springframework.cloud.gcp.storage.GoogleStorageProtocolResolver;
import org.springframework.cloud.gcp.storage.GoogleStorageProtocolResolverSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import static org.mockito.Mockito.mock;

@Profile("mockit")
@Configuration
@Import(GoogleStorageProtocolResolver.class)
public class AppConfigTest {

  @Bean
  @Primary
  public static Storage mockStorage() throws Exception {
    return mock(Storage.class);
  }

  @Bean
  public static GoogleStorageProtocolResolverSettings googleStorageProtocolResolverSettings() {
    return new GoogleStorageProtocolResolverSettings();
  }
}
