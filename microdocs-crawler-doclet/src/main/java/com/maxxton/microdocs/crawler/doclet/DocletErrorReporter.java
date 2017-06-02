package com.maxxton.microdocs.crawler.doclet;

import com.maxxton.microdocs.core.logging.LogLevel;
import com.maxxton.microdocs.core.logging.Logger;
import com.sun.javadoc.DocErrorReporter;

/**
 * @author Steven Hermans
 */
public class DocletErrorReporter extends Logger {

  private final DocErrorReporter delegate;

  public DocletErrorReporter(DocErrorReporter errorReporter, LogLevel logLevel) {
    this.delegate = errorReporter;
    this.setLevel(logLevel);
  }

  public void error(String msg) {
    super.error(msg);
    if (getLevel().match(LogLevel.ERROR)) {
      delegate.printError(msg);
    }
  }

  public void error(String msg, Throwable e) {
    super.error(msg, e);
    if (getLevel().match(LogLevel.ERROR)) {
      delegate.printError(msg);
    }
  }

  public void info(String msg) {
    super.info(msg);
    if (getLevel().match(LogLevel.INFO)) {
    delegate.printNotice(msg);
    }
  }

  public void warning(String msg) {
    super.warning(msg);
    if (getLevel().match(LogLevel.WARNING)) {
    delegate.printWarning(msg);
    }
  }

}
