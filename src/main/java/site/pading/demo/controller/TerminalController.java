package site.pading.demo.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import site.pading.demo.service.TerminalService;

@Controller
public class TerminalController {

  private final TerminalService terminalService;

  public TerminalController(TerminalService terminalService) {
    this.terminalService = terminalService;
  }

  @MessageMapping("/terminal/connect")
  public void connectToPod(String sessionId) throws Exception {
    terminalService.connectToPod(sessionId);
  }

  @MessageMapping("/terminal/input")
  public void handleInput(TerminalInput input) {
    terminalService.handleInput(input);
  }
}
