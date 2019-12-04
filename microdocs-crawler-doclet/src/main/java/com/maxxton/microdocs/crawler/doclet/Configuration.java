package com.maxxton.microdocs.crawler.doclet;

/**
 * Command line configurations for JavaDoc
 *
 * @author Steven Hermans
 */
public class Configuration {

  static final String OPTION_DIRECTORY = "-d";
  static final String OPTION_FILENAME = "-f";
  static final String OPTION_CRAWLER = "-crawler";
  static final String OPTION_VERSION = "-version";
  static final String OPTION_GROUP = "-group";
  static final String OPTION_PROJECT_NAME = "-projectName";
  private static final String DEFAULT_DIRECTORY = ".";
  private static final String DEFAULT_FILENAME = "./microdocs.json";
  private static final String DEFAULT_CRAWLER = "spring";

  private String outputDirectory;
  private String outputFilename;
  private String crawler;
  private String version;
  private String group;
  private String projectName;

  void setOutputDirectory(String outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  void setOutputFilename(String outputFilename) {
    this.outputFilename = outputFilename;
  }

  void setCrawler(String crawler) {
    this.crawler = crawler;
  }

  void setVersion(String version) {
    this.version = version;
  }

  void setGroup(String group) {
    this.group = group;
  }

  void setProjectName(String projectName) {
    this.projectName = projectName;
  }

  String getOutputDirectory() {
    return outputDirectory != null ? outputDirectory : DEFAULT_DIRECTORY;
  }

  String getOutputFileName() {
    return outputFilename != null ? outputFilename : DEFAULT_FILENAME;
  }

  String getCrawler() {
    return crawler != null ? crawler : DEFAULT_CRAWLER;
  }

  String getVersion() {
    return version;
  }

  String getGroup() {
    return group;
  }

  String getProjectName() {
    return projectName;
  }

}
