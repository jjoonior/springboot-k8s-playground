package site.pading.demo.service;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import site.pading.demo.controller.DirectoryController.Type;

@Service
@RequiredArgsConstructor
public class DirectoryService {

  private final KubernetesClient kubernetesClient;
  private final SimpMessagingTemplate messagingTemplate;
  private static final String NAMESPACE = "pading";
  private static final String POD_NAME = "my-pod";

  // 디렉토리 트리 조회
  public List<TreeNode> getTreeNodes(String projectName, String path)
      throws IOException, ExecutionException, InterruptedException {
    ByteArrayOutputStream output = new ByteArrayOutputStream();

    try (ExecWatch watch = kubernetesClient.pods()
        .inNamespace(NAMESPACE)
        .withName(POD_NAME)
        .writingOutput(output)
        .exec("sh", "-c", "ls -alF " + path + " | awk '{print $1,$9}'")) {

      CompletableFuture<Void> future = new CompletableFuture<>();
      watch.exitCode().thenAccept(code -> future.complete(null));
      future.get(); // 명령어 실행 완료 대기
    }

    return parseLsOutput(output.toString(StandardCharsets.UTF_8.name()), path);
  }

  // ls 출력 파싱
  private List<TreeNode> parseLsOutput(String output, String basePath) {
    List<TreeNode> nodes = new ArrayList<>();
    for (String line : output.split("\n")) {
      if (line.trim().isEmpty() || line.contains("total ")) {
        continue;
      }

      String[] parts = line.split("\\s+", 2);
      if (parts.length < 2) {
        continue;
      }

      String permissions = parts[0];
      String name = parts[1].replace("/", ""); // 디렉토리 표시 제거
      boolean isDirectory = permissions.startsWith("d");
      String fullPath = basePath.endsWith("/")
          ? basePath + name
          : basePath + "/" + name;

      nodes.add(new TreeNode(
          fullPath,
          name,
          isDirectory ? "FOLDER" : "FILE",
          isDirectory,
          new ArrayList<>()
      ));
    }
    return nodes;
  }

  // 파일/디렉토리 생성
  public void createFile(String projectName, String parentPath, Type type) {
    String suffix = type == Type.FILE
        ? "file_" + System.currentTimeMillis()
        : "dir_" + System.currentTimeMillis();

    String command = type == Type.FILE
        ? "touch " + parentPath + "/" + suffix
        : "mkdir -p " + parentPath + "/" + suffix;

    executeCommand(command);
    notifyClients(projectName, parentPath);
  }

  // 파일/디렉토리 삭제
  public void deleteFile(String projectName, String path) {
    executeCommand("rm -rf " + path);
    String parentPath = path.contains("/")
        ? path.substring(0, path.lastIndexOf('/'))
        : "/";
    notifyClients(projectName, parentPath);
  }

  // 명령어 실행 공통 함수
  private void executeCommand(String command) {
    try (ExecWatch ignore = kubernetesClient.pods()
        .inNamespace(NAMESPACE)
        .withName(POD_NAME)
        .writingOutput(System.out)
        .writingError(System.err)
        .exec("sh", "-c", command)) {
    }
  }

  // 클라이언트 알림
  private void notifyClients(String projectName, String parentPath) {
    messagingTemplate.convertAndSend(
        "/topic/project/" + projectName + "/file",
        Map.of("parent", parentPath)
    );
  }

  // TreeNode 레코드
  public record TreeNode(
      String id,
      String text,
      String type,
      boolean directory,
      List<TreeNode> children
  ) {

  }
}