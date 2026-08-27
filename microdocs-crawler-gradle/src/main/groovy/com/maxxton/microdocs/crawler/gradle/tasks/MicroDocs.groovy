package com.maxxton.microdocs.crawler.gradle.tasks

import com.maxxton.microdocs.crawler.gradle.MicroDocsUtils
import groovy.transform.CompileStatic
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.CoreJavadocOptions

/**
 * @author Steven Hermans
 */
abstract class MicroDocs extends Javadoc {

  @Override
  @CompileStatic
  protected void generate() {
    String version = MicroDocsUtils.getVersion(project)
    CoreJavadocOptions opts = (CoreJavadocOptions) options

    if (project.name != null)
      opts.addStringOption("projectName", project.name)
    if (version != null && version.length() > 0)
      opts.addStringOption("apiVersion", version)
    else
      opts.addStringOption("apiVersion", "develop")

    super.generate()
  }
}
