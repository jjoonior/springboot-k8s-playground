package site.pading.demo.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import site.pading.demo.service.TerminalService;

@Controller
public class TerminalController {

  private final TerminalService terminalService;

  public TerminalController(TerminalService terminalService) {
    this.terminalService = terminalService;
  }

  @MessageMapping("/terminal/{terminalId}/connect")
  public void connectToPod(@DestinationVariable String terminalId) throws Exception {
    String destination = "/sub/terminal/" + terminalId;
    terminalService.connectToPod(terminalId, destination);
  }

  @MessageMapping("/terminal/{terminalId}/input")
  public void handleInput(@DestinationVariable String terminalId, String input) {
    terminalService.handleInput(terminalId, input);
  }
}
