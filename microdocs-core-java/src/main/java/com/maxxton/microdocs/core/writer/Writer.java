package com.maxxton.microdocs.core.writer;

import java.io.File;

import com.maxxton.microdocs.core.domain.Project;

/**
 * @author Steven Hermans
 */
public interface Writer {

  /**
   * Write project
   *
   * @param project the service project
   * @param outputFile the json output
   * @throws Exception in case an unexpected error occurs
   */
  public void write(Project project, File outputFile) throws Exception;
}
