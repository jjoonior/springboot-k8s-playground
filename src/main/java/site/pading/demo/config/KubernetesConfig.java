package site.pading.demo.config;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KubernetesConfig {
  @Bean
  public KubernetesClient kubernetesClient() {
    // 기본적으로 .kube/config 파일을 자동으로 읽음
    return new DefaultKubernetesClient();
  }
}

