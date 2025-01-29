package site.pading.demo.controller;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import site.pading.demo.service.TerminalService;

@Controller
@RequiredArgsConstructor
public class TerminalController {

  private final TerminalService terminalService;

  @MessageMapping("/project/{projectName}/terminal/{terminalId}/connect")
  public void connectToPod(@DestinationVariable String projectName,
      @DestinationVariable String terminalId) throws Exception {
    String destination = "/sub/project/" + projectName + "/terminal/" + terminalId;
    terminalService.connectToPod(projectName, terminalId, destination);
  }

  @MessageMapping("/project/{projectName}/terminal/{terminalId}/input")
  public void handleInput(@DestinationVariable String terminalId, String input) {
    terminalService.handleInput(terminalId, input);
  }

  @MessageMapping("/project/{projectName}/terminal/{terminalId}/resize")
  public void handleResize(@DestinationVariable String terminalId, Map<?, ?> resize) {
    terminalService.handleResize(terminalId, resize);
  }
}
