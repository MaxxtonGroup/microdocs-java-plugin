package com.maxxton.microdocs.crawler.spring;

import com.maxxton.microdocs.core.builder.ComponentBuilder;
import com.maxxton.microdocs.core.collector.ComponentCollector;
import com.maxxton.microdocs.core.collector.SchemaCollector;
import com.maxxton.microdocs.core.domain.component.ComponentType;
import com.maxxton.microdocs.crawler.Crawler;
import com.maxxton.microdocs.crawler.spring.collector.PathCollector;
import com.maxxton.microdocs.crawler.spring.collector.SpringSchemaCollector;
import com.maxxton.microdocs.core.builder.ProjectBuilder;
import com.maxxton.microdocs.core.domain.Project;
import com.maxxton.microdocs.core.domain.schema.Schema;
import com.maxxton.microdocs.core.reflect.ReflectClass;
import com.maxxton.microdocs.crawler.spring.collector.DependencyCollector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Steven Hermans
 */
public class SpringCrawler extends Crawler {

    private final ComponentCollector componentCollector;
    private final SchemaCollector schemaCollector;
    private final PathCollector pathCollector;
    private final DependencyCollector dependencyCollector;

    public SpringCrawler() {
        Map<String, ComponentType> componentsMap = new HashMap();
        componentsMap.put(Types.SERVICE.getClassName(), ComponentType.SERVICE);
        componentsMap.put(Types.COMPONENT.getClassName(), ComponentType.COMPONENT);
        componentsMap.put(Types.CONTROLLER.getClassName(), ComponentType.CONTROLLER);
        componentsMap.put(Types.REST_CONTROLLER.getClassName(), ComponentType.CONTROLLER);
        componentsMap.put(Types.REPOSITORY.getClassName(), ComponentType.REPOSITORY);
        componentsMap.put(Types.SPRING_BOOT_APPLICATION.getClassName(), ComponentType.APPLICATION);
        componentsMap.put(Types.CONFIGURATION.getClassName(), ComponentType.CONFIGURATION);
        componentsMap.put(Types.FEIGN_CLIENT.getClassName(), ComponentType.CLIENT);
        componentCollector = new ComponentCollector(componentsMap);

        schemaCollector = new SpringSchemaCollector();
        pathCollector = new PathCollector(schemaCollector, Types.REST_CONTROLLER);
        dependencyCollector = new DependencyCollector(schemaCollector);
    }

    @Override
    protected Project extractProject(ProjectBuilder project, List<ReflectClass<?>> classes) {
        // extract components
        List<ComponentBuilder> components = componentCollector.collect(classes);
        components.forEach(component -> project.component(component.simpleName(), component.build()));

        // extract endpoint
        pathCollector.collect(classes).forEach(pathBuilder -> project.path(pathBuilder));

        // extract dependencies
        dependencyCollector.collect(classes).forEach(dependencyBuilder -> project.dependency(dependencyBuilder));

        // extract schemas
        Map<String, Schema> schemas = schemaCollector.collect(classes);
        schemas.entrySet().forEach(entry -> project.definition(entry.getKey(), entry.getValue()));

        return project.build();
    }
}
