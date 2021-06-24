package com.maxxton.microdocs.crawler;

import java.util.List;

import com.maxxton.microdocs.core.builder.ProjectBuilder;
import com.maxxton.microdocs.core.domain.Project;
import com.maxxton.microdocs.core.domain.dependency.Dependency;
import com.maxxton.microdocs.core.domain.dependency.DependencyType;
import com.maxxton.microdocs.core.reflect.ReflectClass;

/**
 * Crawls classes and extract the project information
 * @author Steven Hermans
 */
public abstract class Crawler {

    /**
     * Extract project information from the classes
     * @param classes list of ReflectClasses
     * @param customLibraries simple list of references to libraries to make microdocs aware
     * @return extracted project
     */
    public Project crawl(List<ReflectClass<?>> classes, List<String> customLibraries){
        //start builder
        ProjectBuilder projectBuilder = new ProjectBuilder();
        classes.forEach(clazz -> projectBuilder.projectClass(clazz.getName()));

        //extract project information
      Project project = extractProject(projectBuilder, classes);
      // add external dependencies
      customLibraries.forEach(library -> {
        Dependency dependency = new Dependency();
        dependency.setType(DependencyType.INCLUDES);
        project.getDependencies().put(library.toLowerCase(), dependency);
      });

      return project;
    }

    /**
     * Handles the extraction of the project information
     * @param projectBuilder builder of the project
     * @param classes list of ReflectClasses
     * @return extracted project
     */
    protected abstract Project extractProject(ProjectBuilder projectBuilder, List<ReflectClass<?>> classes);

}
