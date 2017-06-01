package com.maxxton.microdocs.core.logging;

/**
 * @author Steven Hermans
 */
public enum LogLevel {

  DEBUG(1),
  INFO(2),
  WARNING(3),
  ERROR(4),
  OFF(0);

  private final int level;

  LogLevel(int level){
    this.level = level;
  }

  public int getLevel() {
    return level;
  }

  public boolean match(LogLevel logLevel){
    return match(logLevel.getLevel());
  }

  public boolean match(int logLevel){
    return logLevel >= level;
  }
}
