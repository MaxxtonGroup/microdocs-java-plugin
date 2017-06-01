package com.maxxton.microdocs.crawler.doclet;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Steven Hermans
 */
public class DocletDebugger {

  public static void main(String[] args) throws IOException {
    if(args.length != 2){
      System.err.println("usage: <javadoc_options_file> <source_folder>");
      System.exit(1);
    }
    File file = new File(args[0]);
    if(!file.isFile()){
      System.err.println("Could not find file: " + file.getAbsolutePath());
      System.exit(1);
    }
    String sourceFolder = args[1];

    String classPath = null;
    String text = "";
    List<String> arguments = new ArrayList();
    arguments.add("-docletpath");
    arguments.add("C:/Users/steve/projects/microdocs/microdocs-java-plugin/microdocs-crawler-doclet/build/classes/main");
    arguments.add("-doclet");
    arguments.add(DocletRunner.class.getName());
    arguments.add("-sourcepath");
    arguments.add(sourceFolder);
    BufferedReader reader = null;
    try{
      reader = new BufferedReader(new FileReader(file));
      String line;
      while((line = reader.readLine()) != null){
        if(!line.startsWith("-docletpath") && !line.startsWith("-doclet")){
          if(!line.startsWith("-")){
            line = line.replace(sourceFolder, "").replaceAll("\\\\\\\\", ".").replaceAll(".java", "").replaceAll("\'", "");
//            arguments.add(line);
          }else{
            for(String item : line.split(" ")){
              arguments.add(item);
            }
          }
          text += " " + line;
        }
      }
    }finally {
      if(reader != null){
        try {
          reader.close();
        } catch (IOException e) {}
      }
    }

    com.sun.tools.javadoc.Main.execute(arguments.toArray(new String[arguments.size()]));
//    com.sun.tools.javadoc.Main.execute(text, new String[]{});
  }

}
