package site.pading.demo.config;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KubernetesConfig {

  @Value("${kubernetes.master-url}")
  private String masterUrl;

  @Value("${kubernetes.token}")
  private String token;

  @Value("${kubernetes.trust-cert}")
  private boolean trustCert;

  @Bean
  public KubernetesClient createClient() {
    Config config = new ConfigBuilder()
        .withMasterUrl(masterUrl)
        .withOauthToken(token)
        .withTrustCerts(trustCert)
        .build();

    return new DefaultKubernetesClient(config);
  }
}

