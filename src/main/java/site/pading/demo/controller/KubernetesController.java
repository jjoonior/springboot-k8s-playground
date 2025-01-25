package site.pading.demo.controller;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KubernetesController {
  @Autowired
  private KubernetesClient kubernetesClient;

  @GetMapping("/pods")
  public String listPods(@RequestParam String namespace) {
    try {
      // 해당 네임스페이스에서 파드 목록 가져오기
      List<Pod> podList = kubernetesClient.pods().inNamespace(namespace).list().getItems();

      StringBuilder response = new StringBuilder("Pods in namespace [" + namespace + "]:\n");
      podList.forEach(pod -> {
        response.append("- ").append(pod.getMetadata().getName()).append("\n");
      });

      return response.toString();
    } catch (KubernetesClientException e) {
      return "Error fetching pods from namespace [" + namespace + "]: " + e.getMessage();
    }
  }}

