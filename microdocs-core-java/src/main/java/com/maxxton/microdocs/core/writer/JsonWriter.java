package com.maxxton.microdocs.core.writer;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxxton.microdocs.core.domain.Project;

/**
 * Writer project to json file
 *
 * @author Steven Hermans
 */
public class JsonWriter implements Writer {

  /**
   * Write project to json file
   *
   * @param project the service project
   * @param outputFile the json output
   * @throws Exception in case an unexpected error occurs
   */
  public void write(Project project, File outputFile) throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
    DefaultPrettyPrinter.Indenter indenter = new DefaultIndenter("   ", DefaultIndenter.SYS_LF);
    DefaultPrettyPrinter printer = new DefaultPrettyPrinter();
    printer.indentObjectsWith(indenter);
    printer.indentArraysWith(indenter);

    String json = objectMapper.writer(printer).writeValueAsString(project);

    FileOutputStream fileOut = new FileOutputStream(outputFile);
    fileOut.write(json.getBytes(StandardCharsets.UTF_8));
    fileOut.flush();
    fileOut.close();
  }
}
