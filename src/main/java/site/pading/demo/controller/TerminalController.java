package site.pading.demo.controller;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ExecListener;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class TerminalController {

  private final Map<String, TerminalBridge> bridges = new ConcurrentHashMap<>();

  @Autowired
  private SimpMessagingTemplate template;

  @Autowired
  private KubernetesClient kubernetesClient;


  @MessageMapping("/terminal/connect")
  public void connectToPod(String sessionId) throws Exception {
    String[] parts = sessionId.split(":");
    String namespace = parts[0];
    String podName = parts[1];
    String containerName = parts.length > 2 ? parts[2] : null;

    ExecWatch execWatch = kubernetesClient.pods()
        .inNamespace(namespace)
        .withName(podName)
        .inContainer(containerName)
        .redirectingInput()
        .redirectingOutput()
        .redirectingError()
        .withTTY()
        .usingListener(new ExecListener() {
          @Override
          public void onOpen() {
            System.out.println("Connection opened");
          }

          @Override
          public void onFailure(Throwable t, Response failureResponse) {
            template.convertAndSend("/sub/terminal/" + sessionId,
                "Connection failed: " + t.getMessage());
            bridges.remove(sessionId);
          }

          @Override
          public void onClose(int code, String reason) {
            template.convertAndSend("/sub/terminal/" + sessionId, "\nConnection closed");
            bridges.remove(sessionId);
          }
        })
        .exec("sh", "-c",
            "TERM=xterm-256color; export TERM; [ -x /bin/bash ] && /bin/bash || /bin/sh");

    TerminalBridge bridge = new TerminalBridge(execWatch, template, sessionId);
    bridges.put(sessionId, bridge);
    bridge.start();
  }

  @MessageMapping("/terminal/input")
  public void handleInput(TerminalInput input) {
    TerminalBridge bridge = bridges.get(input.getSessionId());
    if (bridge != null) {
      bridge.sendInput(input.getInput());
    }
  }

  private static class TerminalBridge {

    private final ExecWatch execWatch;
    private final SimpMessagingTemplate template;
    private final String sessionId;
    private final OutputStream inputStream;

    public TerminalBridge(ExecWatch execWatch,
        SimpMessagingTemplate template,
        String sessionId) {
      this.execWatch = execWatch;
      this.template = template;
      this.sessionId = sessionId;
      this.inputStream = execWatch.getInput();
    }

    public void start() {
      startAsyncReader(execWatch.getOutput(), "OUTPUT");
      startAsyncReader(execWatch.getError(), "ERROR");
    }

    private void startAsyncReader(InputStream stream, String type) {
      new Thread(() -> {
        byte[] buffer = new byte[1024];
        try {
          int bytesRead;
          while ((bytesRead = stream.read(buffer)) != -1) {
            String content = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
            template.convertAndSend("/sub/terminal/" + sessionId, content);
          }
        } catch (IOException e) {
          template.convertAndSend("/sub/terminal/" + sessionId,
              "\n[" + type + " READ ERROR] " + e.getMessage());
        }
      }).start();
    }

    public synchronized void sendInput(String input) {
      try {
        inputStream.write(input.getBytes(StandardCharsets.UTF_8));
        inputStream.flush();
      } catch (IOException e) {
        template.convertAndSend("/sub/terminal/" + sessionId,
            "\n[INPUT WRITE ERROR] " + e.getMessage());
      }
    }
  }

  // DTO 클래스
  public static class TerminalInput {

    private String sessionId;
    private String input;

    // getters & setters
    public String getSessionId() {
      return sessionId;
    }

    public void setSessionId(String sessionId) {
      this.sessionId = sessionId;
    }

    public String getInput() {
      return input;
    }

    public void setInput(String input) {
      this.input = input;
    }
  }
}
