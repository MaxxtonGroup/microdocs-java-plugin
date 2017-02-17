package com.maxxton.microdocs.crawler.spring;

/**
 * @author Steven Hermans
 */
public enum Types {

  SERVICE("org.springframework.stereotype.Service"),
  COMPONENT("org.springframework.stereotype.Component"),
  CONTROLLER("org.springframework.stereotype.Controller"),
  REST_CONTROLLER("org.springframework.web.bind.annotation.RestController"),
  REPOSITORY("org.springframework.stereotype.Repository"),
  SPRING_BOOT_APPLICATION("org.springframework.boot.autoconfigure.SpringBootApplication"),
  CONFIGURATION("org.springframework.context.annotation.Configuration"),
  FEIGN_CLIENT("org.springframework.cloud.netflix.feign.FeignClient"),


  REQUEST_MAPPING("org.springframework.web.bind.annotation.RequestMapping"),
  GET_MAPPING("org.springframework.web.bind.annotation.GetMapping"),
  DELETE_MAPPING("org.springframework.web.bind.annotation.DeleteMapping"),
  PATCH_MAPPING("org.springframework.web.bind.annotation.PatchMapping"),
  POST_MAPPING("org.springframework.web.bind.annotation.PostMapping"),
  PUT_MAPPING("org.springframework.web.bind.annotation.PutMapping");

  private final String className;

  private Types(String className) {
    this.className = className;
  }

  public String getClassName() {
    return className;
  }
}
