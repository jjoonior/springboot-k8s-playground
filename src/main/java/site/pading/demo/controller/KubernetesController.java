package site.pading.demo.controller;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1NodeList;
import io.kubernetes.client.openapi.models.V1PodList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KubernetesController {

  @Autowired
  private ApiClient apiClient;

  @GetMapping("/nodes")
  public String listNodes() {
    try {
      // CoreV1Api를 통해 Kubernetes API 호출
      CoreV1Api api = new CoreV1Api(apiClient);
      V1NodeList nodeList = api.listNode(null, null, null, null, null, null, null, null, null, null);
      // Node 이름 목록 출력
      StringBuilder response = new StringBuilder("Nodes in the cluster:\n");
      nodeList.getItems().forEach(node -> response.append(node.getMetadata().getName()).append("\n"));
      return response.toString();
    } catch (Exception e) {
      return "Error fetching node list: " + e.getMessage();
    }
  }

  @GetMapping("/pods")
  public String listPods(@RequestParam String namespace) {
    try {
      // CoreV1Api 인스턴스 생성
      CoreV1Api api = new CoreV1Api(apiClient);

      // 특정 네임스페이스의 파드 목록 가져오기
      V1PodList podList = api.listNamespacedPod(
          namespace,            // 네임스페이스
          null,                 // continue
          null,                 // fieldSelector
          null,                 // labelSelector
          null,                 // limit
          null,                 // pretty
          null,                 // resourceVersion
          null,                 // timeoutSeconds
          null,                 // watch
          null,                  // allowWatchBookmarks
          null
      );

      // 결과를 반환하기 위한 문자열 생성
      StringBuilder response = new StringBuilder("Pods in namespace [" + namespace + "]:\n");
      podList.getItems().forEach(pod -> {
        response.append("- ").append(pod.getMetadata().getName()).append("\n");
        System.out.println(pod.getMetadata().getName());
      });

      return response.toString();
    } catch (Exception e) {
      e.printStackTrace();
      return "Error fetching pods from namespace [" + namespace + "]: " + e.getMessage();
    }
  }
}

