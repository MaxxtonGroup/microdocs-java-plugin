package com.maxxton.microdocs.core.logging;

/**
 * @author Steven Hermans
 */
public class Logger {

  public static final String ANSI_RESET = "\u001B[0m";
  public static final String ANSI_BLACK = "\u001B[30m";
  public static final String ANSI_RED = "\u001B[31m";
  public static final String ANSI_GREEN = "\u001B[32m";
  public static final String ANSI_YELLOW = "\u001B[33m";
  public static final String ANSI_BLUE = "\u001B[34m";
  public static final String ANSI_PURPLE = "\u001B[35m";
  public static final String ANSI_CYAN = "\u001B[36m";
  public static final String ANSI_WHITE = "\u001B[37m";

  private static Logger reporter = new Logger();

  public static void set(Logger reporter) {
    Logger.reporter = reporter;
  }

  public static Logger get() {
    return reporter;
  }

  private LogLevel level = LogLevel.WARNING;

  public LogLevel getLevel() {
    return level;
  }

  public void setLevel(LogLevel level) {
    this.level = level;
  }

  public void error(String msg) {
    if(level.match(LogLevel.ERROR)) {
      System.err.println(ANSI_RED + "[error] " + ANSI_RESET + msg);
    }
  }

  public void error(String msg, Throwable e) {
    if(level.match(LogLevel.ERROR)) {
      System.err.println(ANSI_RED + "[error] " + ANSI_RESET + msg);
      e.printStackTrace();
    }
  }

  public void debug(String msg) {
    if(level.match(LogLevel.DEBUG)) {
      System.err.println(ANSI_CYAN + "[debug] " + ANSI_RESET + msg);
    }
  }

  public void info(String msg) {
    if(level.match(LogLevel.INFO)) {
      System.out.println(ANSI_GREEN + msg + ANSI_RESET);
    }
  }

  public void warning(String msg) {
    if(level.match(LogLevel.WARNING)) {
      System.out.println(ANSI_YELLOW + "[warning] " + ANSI_RESET + msg);
    }
  }

  public void logEndpoint(String method, String path) {
    if(level.match(LogLevel.INFO)) {
      method = "[" + method + "]";
      while (method.length() < 8) {
        method += " ";
      }
      System.out.println(ANSI_BLUE + method + " " + path + ANSI_RESET);
    }
  }

}
