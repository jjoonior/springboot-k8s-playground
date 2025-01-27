package site.pading.demo.controller;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.Status;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.ExecListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
  }

  @GetMapping("/create-pod")
  public String createPod(@RequestParam String namespace, @RequestParam String name) {

    // Pod 객체 생성
    Pod pod = new PodBuilder()
        .withNewMetadata()
        .withName(namespace)  // Pod 이름 설정
//        .withNamespace("pading")  // 네임스페이스 설정
        .endMetadata()
        .withNewSpec()
        .addNewContainer()
        .withName("my-containersss")  // 컨테이너 이름 설정
        .withImage("nginx")  // 컨테이너 이미지 설정 (여기서는 nginx 사용)
//        .withNewPort()
//        .withContainerPort(80)
//        .endPort()
        .endContainer()
        .endSpec()
        .build();

//     Pod 생성
    kubernetesClient.pods().inNamespace(namespace).create(pod);

    return "Pod 생성 완료";
  }

  @GetMapping("/execute")
  public String executeCommand(
      @RequestParam String namespace,
      @RequestParam String podName,
      @RequestParam String command
  ) throws TimeoutException, IOException {
    return executeCommandInPod(namespace, podName, command, 30, TimeUnit.SECONDS);
  }

  public String executeCommandInPod(String namespace, String podName, String command, long timeout,
      TimeUnit timeUnit)
      throws TimeoutException {
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream()) {

      // Synchronization flag to wait for command execution to complete
      final CountDownLatch execLatch = new CountDownLatch(1);

      // ExecListener to handle command execution events
      ExecListener execListener = new ExecListener() {
        @Override
        public void onOpen() {
          System.out.println("Connection to Pod opened.");
        }

        @Override
        public void onFailure(Throwable t, Response failureResponse) {
          System.err.println("Error occurred during command execution: " + t.getMessage());
          if (failureResponse != null) {
            try {
              System.err.println("Failure Response Code: " + failureResponse.code());
              System.err.println("Failure Response Body: " + failureResponse.body());
            } catch (IOException e) {
              System.err.println("Error reading failure response body: " + e.getMessage());
            }
          }
          t.printStackTrace();
          execLatch.countDown(); // Ensure the latch releases
        }

        @Override
        public void onClose(int code, String reason) {
          System.out.println("Connection to Pod closed. Code: " + code + ", Reason: " + reason);
          execLatch.countDown(); // Notify when command execution is done
        }

        @Override
        public void onExit(int code, Status status) {
          System.out.println("Command execution completed with exit code: " + code);
          if (status != null) {
            System.out.println("Command exit status: " + status);
          }
        }
      };

      // Execute the command in the Pod
      kubernetesClient.pods()
          .inNamespace(namespace)
          .withName(podName)
          .writingOutput(outputStream) // Capture stdout
          .writingError(errorStream)  // Capture stderr
          .usingListener(execListener) // Attach the listener
          .exec("sh", "-c", command); // Execute the command

      // Wait for the command execution to complete with a timeout
      boolean completed = execLatch.await(timeout, timeUnit);
      if (!completed) {
        throw new TimeoutException("Command execution timed out after " + timeout + " " + timeUnit);
      }

      // Get the output and error streams
      String output = outputStream.toString();
      String error = errorStream.toString();

      // Log the output and error
      System.out.println("Command Output: " + output);
      System.err.println("Command Error: " + error);

      // Return the output or error
      return output.isEmpty() ? error : output;

    } catch (Exception e) {
      e.printStackTrace();
      return "Error executing command: " + e.getMessage();
    }
  }
}
