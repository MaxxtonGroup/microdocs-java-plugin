package com.maxxton.microdocs.crawler.doclet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Steven Hermans
 */
public class DocletDebugger {

  public static void main(String[] args) {
    if(args.length != 1){
      System.err.println("usage: <javadoc_options_file>");
      System.exit(1);
    }
    File file = new File(args[0]);
    if(!file.isFile()){
      System.err.println("Could not find file: " + file.getAbsolutePath());
      System.exit(1);
    }

    String classPath = null;
    String text = "";
    List<String> arguments = new ArrayList<>();
    arguments.add("-docletpath");
    // TODO
    arguments.add("C:/Users/steve/projects/microdocs/microdocs-java-plugin/microdocs-crawler-doclet/build/classes/main");
    arguments.add("-doclet");
    arguments.add(DocletRunner.class.getName());
    arguments.add("@" + file.getAbsolutePath());

    System.out.print("args:");
    arguments.forEach(arg -> System.out.print(" " + arg));
    System.out.println();

    com.sun.tools.javadoc.Main.execute(arguments.toArray(new String[0]));
  }

}
