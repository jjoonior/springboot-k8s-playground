package site.pading.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import site.pading.demo.service.DirectoryService;

@Controller
@RequiredArgsConstructor
public class DirectoryController {

  private final DirectoryService directoryService;

  @RequestMapping(value = "/chat")
  public String chat() {
    return "chat";
  }

  @MessageMapping("/test") // 메세지 전송
  public String sendMessage(String message) throws Exception {
    System.out.println(message);
    return message;
  }

  @MessageMapping("/project/{projectName}/directory/list")
  public void list(
      @DestinationVariable String projectName,
      @RequestBody FileCreateRequest request
  ) {
    directoryService.createFile(projectName, request.parent(), request.type());
  }

  @MessageMapping("/project/{projectName}/directory/create")
  public void create(
      @DestinationVariable String projectName,
      @RequestBody FileCreateRequest request
  ) {
    directoryService.createFile(projectName, request.parent(), request.type());
  }

  @MessageMapping("/project/{projectName}/file/delete")
  public void delete(
      @DestinationVariable String projectName,
      @RequestBody FileDeleteRequest request
  ) {
    directoryService.deleteFile(projectName, request.path());
  }

  @MessageMapping("/project/{projectName}/file/rename")
  public void rename(
      @DestinationVariable String projectName,
      @RequestBody FileDeleteRequest request
  ) {
    directoryService.deleteFile(projectName, request.path());
  }


  public enum Type {
    FILE, FOLDER
  }

  // DTO
  public record FileCreateRequest(String parent, Type type) {

  }

  public record FileDeleteRequest(String path) {

  }
}