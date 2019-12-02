package com.maxxton.microdocs.crawler.doclet;

/**
 * Command line configurations for JavaDoc
 *
 * @author Steven Hermans
 */
public class Configuration {

  public static final String OPTION_DIRECTORY = "-d";
  public static final String OPTION_FILENAME = "-f";
  public static final String OPTION_CRAWLER = "-crawler";
  public static final String OPTION_VERSION = "-version";
  public static final String OPTION_GROUP = "-group";
  public static final String OPTION_PROJECT_NAME = "-projectName";
  private static final String DEFAULT_DIRECTORY = ".";
  private static final String DEFAULT_FILENAME = "./microdocs.json";
  private static final String DEFAULT_CRAWLER = "spring";

  private String outputDirectory;
  private String outputFilename;
  private String crawler;
  private String version;
  private String group;
  private String projectName;

  public void setOutputDirectory(String outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  public void setOutputFilename(String outputFilename) {
    this.outputFilename = outputFilename;
  }

  public void setCrawler(String crawler) {
    this.crawler = crawler;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public void setGroup(String group) {
    this.group = group;
  }

  public void setProjectName(String projectName) {
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
