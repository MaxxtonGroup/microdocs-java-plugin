package com.maxxton.microdocs.crawler.doclet;

import javax.tools.Diagnostic;

import com.maxxton.microdocs.core.logging.LogLevel;
import com.maxxton.microdocs.core.logging.Logger;

import jdk.javadoc.doclet.Reporter;

/**
 * @author Steven Hermans
 */
public class DocletErrorReporter extends Logger {

  private final Reporter delegate;

  public DocletErrorReporter(Reporter errorReporter, LogLevel logLevel) {
    this.delegate = errorReporter;
    this.setLevel(logLevel);
  }

  public void error(String msg) {
    super.error(msg);
    if (getLevel().match(LogLevel.ERROR)) {
      delegate.print(Diagnostic.Kind.ERROR, msg);
    }
  }

  public void error(String msg, Throwable e) {
    super.error(msg, e);
    if (getLevel().match(LogLevel.ERROR)) {
      delegate.print(Diagnostic.Kind.ERROR, msg);
    }
  }

  public void info(String msg) {
    super.info(msg);
    if (getLevel().match(LogLevel.INFO)) {
      delegate.print(Diagnostic.Kind.ERROR, msg);
    }
  }

  public void warning(String msg) {
    super.warning(msg);
    if (getLevel().match(LogLevel.WARNING)) {
      delegate.print(Diagnostic.Kind.ERROR, msg);
    }
  }

}
