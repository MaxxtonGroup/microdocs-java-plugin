package com.maxxton.microdocs.crawler.gradle

import com.sun.source.util.DocTreePath
import jdk.javadoc.doclet.Reporter
import org.gradle.api.logging.Logger

import javax.lang.model.element.Element
import javax.tools.Diagnostic

/**
 * @author Steven Hermans
 */
class GradleErrorReporter implements Reporter {

    private final Logger logger;

    GradleErrorReporter(Logger logger) {
        this.logger = logger
    }

    @Override
    void printError(String msg) {
        logger.error(msg);
    }

    @Override
    void printError(String msg, Throwable e) {
        logger.error(msg, e);
    }

    @Override
    void printNotice(String msg) {
        logger.info(msg);
    }

    @Override
    void printWarning(String msg) {
        logger.warn(msg);
    }

    @Override
    void print(Diagnostic.Kind kind, String msg) {
        logger.log()
    }

    @Override
    void print(Diagnostic.Kind kind, DocTreePath path, String msg) {

    }

    @Override
    void print(Diagnostic.Kind kind, Element e, String msg) {

    }
}
