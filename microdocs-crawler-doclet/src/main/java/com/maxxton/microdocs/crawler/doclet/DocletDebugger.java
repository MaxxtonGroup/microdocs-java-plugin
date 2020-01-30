package com.maxxton.microdocs.crawler.doclet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.tools.DocumentationTool;
import javax.tools.ToolProvider;

/**
 * Test class for running the doclet
 *
 * @author Steven Hermans
 */
public class DocletDebugger {

  public static void main(String[] args) {
    if (args.length != 1) {
      System.err.println("usage: <javadoc_options_file>");
      System.exit(1);
    }
    File file = new File(args[0]);
    if (!file.isFile()) {
      System.err.println("Could not find file: " + file.getAbsolutePath());
      System.exit(1);
    }

    List<String> arguments = new ArrayList<>();
    //        arguments.add("-docletpath");
    //        arguments.add("/Users/robsonke/maxxton/github/microdocs-java-plugin/microdocs-crawler-doclet/build/classes/java/main");
    //        arguments.add("-doclet");
    //        arguments.add(DocletRunner.class.getName());
    //arguments.add("/Users/robsonke/maxxton/sources/mxt2/services/config-server/src/main/java/com/maxxton/config/config/WebSecurityConfig.java");
    arguments.add("@" + file.getAbsolutePath());
    //arguments.add("@/Users/robsonke/maxxton/sources/mxt2/services/config-server/src/main/java/com/maxxton/config/config/WebSecurityConfig.java");

    System.out.print("args:");
    arguments.forEach(arg -> System.out.print(" " + arg));
    System.out.println();

    DocumentationTool javadoc = ToolProvider.getSystemDocumentationTool();
    String argsString = String.join(" ", arguments);
    System.out.println("command: javadoc " + argsString);
    int result = javadoc.run(System.in, System.out, System.err, arguments.toArray(String[]::new));
    System.out.println("task result: " + result);
  }

}
