package com.maxxton.microdocs.core.writer;

import java.io.File;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.maxxton.microdocs.core.domain.Project;

/**
 * Write project as json to the console
 *
 * @author Steven Hermans
 */
public class ConsoleWriter implements Writer {

  /**
   * Write project to console as json
   *
   * @param project    the service project
   * @param outputFile the json output
   * @throws Exception in case an unexpected error occurs
   */
  @Override
  public void write(Project project, File outputFile) throws Exception {
    ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);;
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

    String json = objectMapper.writeValueAsString(project);
    System.out.println(json);
  }

}
