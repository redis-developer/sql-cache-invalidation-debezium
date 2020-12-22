package io.redis.demo.cdc;

import javax.inject.Inject;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping({"/api/1.0/", "/"})
@CrossOrigin("*")
@org.springframework.context.annotation.Configuration
public class CachingRestController {


  @Inject
  DebeziumToRedisService service;

  @GetMapping("/start")
  public Map<String,String> start() throws IOException, URISyntaxException {
    Map<String,String> result = new HashMap<>();
    service.startDebezium();
    result.put("service", "CachingRestController.start");
    result.put("action", "OK");
    return result;
  }


  @GetMapping("/stop")
  public Map<String,String> stop() throws IOException {
    Map<String,String> result = new HashMap<>();
    service.stopDebezium();
    result.put("service", "CachingRestController.stop");
    result.put("action", "OK");
    return result;
  }

  @GetMapping("/reset")
  public Map<String,String> reset() throws IOException, URISyntaxException {
    Map<String,String> result = new HashMap<>();
    service.resetDebezium();
    result.put("service", "CachingRestController.restarted");
    result.put("action", "OK");
    return result;
  }

}
