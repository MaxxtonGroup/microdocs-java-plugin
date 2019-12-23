package com.maxxton.microdocs.crawler.doclet;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

import com.maxxton.microdocs.core.domain.Project;
import com.maxxton.microdocs.core.domain.common.ProjectInfo;
import com.maxxton.microdocs.core.logging.LogLevel;
import com.maxxton.microdocs.core.logging.Logger;
import com.maxxton.microdocs.core.reflect.ReflectClass;
import com.maxxton.microdocs.core.writer.JsonWriter;
import com.maxxton.microdocs.core.writer.Writer;
import com.maxxton.microdocs.crawler.Crawler;
import com.maxxton.microdocs.crawler.spring.SpringCrawler;

import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;
import jdk.javadoc.doclet.StandardDoclet;

import static com.maxxton.microdocs.crawler.doclet.Configuration.OPTION_CRAWLER;
import static com.maxxton.microdocs.crawler.doclet.Configuration.OPTION_DIRECTORY;
import static com.maxxton.microdocs.crawler.doclet.Configuration.OPTION_FILENAME;
import static com.maxxton.microdocs.crawler.doclet.Configuration.OPTION_GROUP;
import static com.maxxton.microdocs.crawler.doclet.Configuration.OPTION_PROJECT_NAME;
import static com.maxxton.microdocs.crawler.doclet.Configuration.OPTION_VERSION;

/**
 * Start of the Doclet runner
 *
 * @author Steven Hermans
 */
public class DocletRunner extends StandardDoclet {

  private static Configuration config = new Configuration();

  @Override
  public void init(Locale locale, Reporter reporter) {
     Logger.set(new DocletErrorReporter(reporter, LogLevel.INFO));
  }

  @Override
  public boolean run(DocletEnvironment docletEnvironment) {
    Logger.get().info("Collect MicroDocs definitions for " + config.getGroup() + "/" + config.getProjectName() + ":" + config.getVersion());

    // get crawler
    Crawler crawler = null;
    if ("spring".equals(config.getCrawler().toLowerCase())) {
      crawler = new SpringCrawler();
    }

    if (crawler == null) {
      throw new IllegalArgumentException("Unknown crawler: " + config.getCrawler());
    }

    // convert Doclet classes to reflect classes
    Set<? extends Element> specifiedElements = docletEnvironment.getSpecifiedElements();
    Set<TypeElement> typeElements = ElementFilter.typesIn(specifiedElements);
    List<ReflectClass<?>> classes = DocletConverter.convert(docletEnvironment, new ArrayList<>(typeElements));

    // run crawler
    Project project = crawler.crawl(classes);

    // add projectName if present
    if (config.getProjectName() != null) {
      if (project.getInfo() == null) {
        project.setInfo(new ProjectInfo());
      }
      project.getInfo().setTitle(config.getProjectName());
    }
    // add version if present
    if (config.getVersion() != null) {
      if (project.getInfo() == null) {
        project.setInfo(new ProjectInfo());
      }
      project.getInfo().setVersion(config.getVersion());
    }
    // add group if present
    if (config.getGroup() != null) {
      if (project.getInfo() == null) {
        project.setInfo(new ProjectInfo());
      }
      project.getInfo().setGroup(config.getGroup());
    }

    // save result
    try {
      Writer writer = new JsonWriter();
      Logger.get().info("Output json: " + config.getOutputDirectory() + "/" + config.getOutputFileName());
      writer.write(project, new File(config.getOutputDirectory(), config.getOutputFileName()));
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }

    return true;
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.RELEASE_11;
  }

  @Override
  public Set<Option> getSupportedOptions() {
    // include the default javadoc parameters as well
    Set<Option> options = new HashSet<>(super.getSupportedOptions());

    Optional<Option> option = options.stream().filter(o -> o.getNames().get(0).equals(OPTION_DIRECTORY)).findFirst();
    option.ifPresent(options::remove);

    options.add(new StandardOption(OPTION_DIRECTORY) {
      @Override
      public boolean process(String option, List<String> args) {
        config.setOutputDirectory(stripParameterValue(args.get(0)));
        return true;
      }
    });
    options.add(new StandardOption(OPTION_FILENAME) {
      @Override
      public boolean process(String option, List<String> args) {
        config.setOutputFilename(stripParameterValue(args.get(0)));
        return true;
      }
    });
    options.add(new StandardOption(OPTION_CRAWLER) {
      @Override
      public boolean process(String option, List<String> args) {
        config.setCrawler(stripParameterValue(args.get(0)));
        return true;
      }
    });
    options.add(new StandardOption(OPTION_VERSION) {
      @Override
      public boolean process(String option, List<String> args) {
        config.setVersion(stripParameterValue(args.get(0)));
        return true;
      }
    });
    options.add(new StandardOption(OPTION_GROUP) {
      @Override
      public boolean process(String option, List<String> args) {
        config.setGroup(stripParameterValue(args.get(0)));
        return true;
      }
    });
    options.add(new StandardOption(OPTION_PROJECT_NAME) {
      @Override
      public boolean process(String option, List<String> args) {
        config.setProjectName(stripParameterValue(args.get(0)));
        return true;
      }
    });

    return options;
  }

  /**
   * Simple version of an Option
   */
  private abstract static class StandardOption implements Option {
    private final String name;

    StandardOption(String name) {
      this.name = name;
    }

    /**
     * Strings quotes from a string
     *
     * @param input the to be stripped string
     * @return stripped string
     */
    String stripParameterValue(String input) {
      return input.replaceAll("^'|'$", "");
    }

    @Override
    public int getArgumentCount() {
      return 1;
    }

    @Override
    public String getDescription() {
      return "";
    }

    @Override
    public Kind getKind() {
      return Kind.STANDARD;
    }

    @Override
    public List<String> getNames() {
      return Collections.singletonList(name);
    }

    @Override
    public String getParameters() {
      return "";
    }

    @Override
    public abstract boolean process(String option, List<String> arguments);
  }

}
