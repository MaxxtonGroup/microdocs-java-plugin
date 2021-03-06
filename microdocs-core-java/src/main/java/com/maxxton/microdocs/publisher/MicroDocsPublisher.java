package com.maxxton.microdocs.publisher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.http.utils.URLParamEncoder;
import com.mashape.unirest.request.HttpRequestWithBody;
import com.maxxton.microdocs.core.domain.check.CheckProblem;
import com.maxxton.microdocs.core.domain.check.CheckResponse;
import com.maxxton.microdocs.core.logging.Logger;

/**
 * Publish or check reports to the MicroDocs server
 *
 * @author Steven Hermans
 */
public class MicroDocsPublisher {

  /**
   * Publish report on the MicroDocs server
   *
   * @param configuration the config object
   * @param microDocsReport the report
   * @param projectName the current project name
   * @param groupName the group name
   * @param version the version of the api
   * @param failOnProblems fail in case of compatibility problems
   * @param env current environment
   *
   * @return check response
   * @throws IOException possible IO exception when file is not found
   */
  public static CheckResponse publishProject(ServerConfiguration configuration, File microDocsReport, String projectName, String groupName, String version, boolean failOnProblems, String env)
      throws IOException {
    String report = loadReport(microDocsReport);

    String url = configuration.getUrl() + "/api/v1/projects/" + URLParamEncoder.encode(projectName);
    Logger.get().debug("PUT " + url);
    initObjectMapper();
    HttpResponse<CheckResponse> response = null;
    try {
      HttpRequestWithBody request = Unirest.put(configuration.getUrl() + "/api/v1/projects/" + URLParamEncoder.encode(projectName)).queryString("failOnProblems", failOnProblems)
          .header("content-type", "application/json").header("accept", "application/json");
      if (groupName != null && !groupName.trim().isEmpty()) {
        request = request.queryString("group", groupName);
      }
      if (version != null && !version.trim().isEmpty()) {
        request = request.queryString("version", version);
      }
      if (configuration.getUsername() != null && configuration.getPassword() != null) {
        request = request.basicAuth(configuration.getUsername(), configuration.getPassword());
      }
      if (env != null && !env.trim().isEmpty()) {
        request = request.queryString("env", env);
      }

      response = request.body(report).asObject(CheckResponse.class);
      if (response.getStatus() != 200) {
        throw new IOException("Wrong response status " + response.getStatus() + ", expected 200");
      }
    }
    catch (UnirestException e) {
      throw new IOException("Failed to send http request: POST " + url, e);
    }
    return response.getBody();
  }

  /**
   * Check report at the MicroDocs server for problems
   *
   * @param microDocsReport the report
   * @param configuration configuration server
   * @param projectName the current project name
   * @param env current environment
   *
   * @return check response
   * @throws IOException possible IO exception when file is not found
   */
  public static CheckResponse checkProject(ServerConfiguration configuration, File microDocsReport, String projectName, String env) throws IOException {
    String report = loadReport(microDocsReport);

    String url = configuration.getUrl() + "/api/v1/check";
    Logger.get().debug("POST " + url);
    initObjectMapper();
    HttpResponse<CheckResponse> response = null;
    try {
      HttpRequestWithBody request = Unirest.post(configuration.getUrl() + "/api/v1/check").queryString("project", projectName).header("content-type", "application/json")
          .header("accept", "application/json");
      if (env != null && !env.trim().isEmpty()) {
        request = request.queryString("env", env);
      }
      response = request.body(report).asObject(CheckResponse.class);
      if (response.getStatus() != 200) {
        throw new IOException("Wrong response status " + response.getStatus() + ", expected 200");
      }
    }
    catch (UnirestException e) {
      throw new IOException("Failed to send http request: POST " + url, e);
    }
    return response.getBody();
  }

  /**
   * Load MicroDocs report as string
   *
   * @param microDocsReport microdocs.json
   * @return content of the report as string
   */
  private static String loadReport(File microDocsReport) throws IOException {
    Logger.get().debug("Load " + microDocsReport.getAbsolutePath());
    byte[] encoded = Files.readAllBytes(Paths.get(microDocsReport.toURI()));
    return new String(encoded);
  }

  private static void initObjectMapper() {
    com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
    jacksonObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    Unirest.setObjectMapper(new ObjectMapper() {

      public <T> T readValue(String value, Class<T> valueType) {
        try {
          return jacksonObjectMapper.readValue(value, valueType);
        }
        catch (IOException e) {
          throw new RuntimeException(e);
        }
      }

      public String writeValue(Object value) {
        try {
          return jacksonObjectMapper.writeValueAsString(value);
        }
        catch (JsonProcessingException e) {
          throw new RuntimeException(e);
        }
      }
    });
  }

  /**
   * Print check response to console
   *
   * @param response the response
   * @param rootDir the rootdir
   * @return succeed or failed
   */
  public static boolean printCheckResponse(CheckResponse response, File rootDir) {
    boolean hasProblems = !"ok".equalsIgnoreCase(response.getStatus());
    int errorCount = 0;
    int warningCount = 0;
    int noticeCount = 0;
    if (response.getProblems() != null) {
      for (CheckProblem problem : response.getProblems()) {
        switch (problem.getLevel().toLowerCase()) {
          case "error":
            errorCount++;
            break;
          case "warning":
            warningCount++;
            break;
          case "notice":
            noticeCount++;
            break;
        }
      }
    }
    String message = "\n";
    if (errorCount + warningCount + noticeCount > 0) {
      message += "Project contains problems: ";
      if (errorCount > 0) {
        message += errorCount + " error" + (errorCount > 1 ? "s" : "") + ",";
      }
      if (warningCount > 0) {
        message += warningCount + " warning" + (warningCount > 1 ? "s" : "") + ",";
      }
      if (noticeCount > 0) {
        message += noticeCount + " notice" + (noticeCount > 1 ? "s" : "") + ",";
      }
      if (message.endsWith(",")) {
        message = message.substring(0, message.length() - 1);
      }
    }
    else {
      message += "No problems found";
    }
    if (hasProblems) {
      Logger.get().error(message);
    }
    else {
      Logger.get().debug(message);
    }

    if (response.getProblems() != null) {
      for (CheckProblem problem : response.getProblems()) {
        String msg = "\n";
        String lineNumber = problem.getLineNumber() > 0 ? ":" + problem.getLineNumber() : "";
        String sourceFile = new File(rootDir, "src/main/java/" + problem.getPath() + lineNumber).getPath();
        msg += sourceFile + ": " + problem.getLevel() + ": " + problem.getMessage();
        if (problem.getClient() != null) {
          msg += problem.getClient().getSourceLink();
        }
        if (hasProblems) {
          Logger.get().error(msg);
        }
        else {
          Logger.get().debug(msg);
        }
      }
    }
    Logger.get().error("");

    return !hasProblems;
  }

}
