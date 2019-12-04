package com.maxxton.microdocs.crawler.doclet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.tools.DocumentationTool;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * Test class for running the doclet
 *
 * @author Steven Hermans
 */
public class DocletDebugger {

    public static void main(String[] args) throws Exception {
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
        arguments.add("-docletpath");
        // TODO
        arguments.add("C:/Users/steve/projects/microdocs/microdocs-java-plugin/microdocs-crawler-doclet/build/classes/main");
        arguments.add("-doclet");
        arguments.add(DocletRunner.class.getName());
        arguments.add("@" + file.getAbsolutePath());

        System.out.print("args:");
        arguments.forEach(arg -> System.out.print(" " + arg));
        System.out.println();
        // TODO
        DocumentationTool tool = ToolProvider.getSystemDocumentationTool();
        try (StandardJavaFileManager fm = tool.getStandardFileManager(null, null, null)) {
            DocumentationTool.DocumentationTask t = tool.getTask(null, fm, null, DocletRunner.class, null, null);
            if (t.call()) {
                System.out.println("task succeeded");
            } else {
                throw new Exception("task failed");
            }

        }
    }

}
