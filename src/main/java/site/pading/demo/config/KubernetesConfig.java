package site.pading.demo.config;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import java.io.FileReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KubernetesConfig {
  @Bean
  public ApiClient apiClient() throws Exception {
    // 기본 경로 사용 시 (~/.kube/config)
    String kubeConfigPath = System.getProperty("user.home") + "/.kube/config";
    ApiClient client = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();

    // resources에서 config 파일 로드 시
//    InputStreamReader kubeConfigReader = new InputStreamReader(
//        getClass().getClassLoader().getResourceAsStream("kube/config")
//    );
//    ApiClient client = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(kubeConfigReader)).build();

    // ApiClient를 기본값으로 설정
    io.kubernetes.client.openapi.Configuration.setDefaultApiClient(client);
    return client;

  }
}

