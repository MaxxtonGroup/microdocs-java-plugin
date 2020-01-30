package com.maxxton.microdocs.crawler.spring.collector;

import java.util.ArrayList;
import java.util.List;

import com.maxxton.microdocs.core.builder.DependencyBuilder;
import com.maxxton.microdocs.core.builder.PathBuilder;
import com.maxxton.microdocs.core.collector.Collector;
import com.maxxton.microdocs.core.collector.SchemaCollector;
import com.maxxton.microdocs.core.domain.dependency.DependencyType;
import com.maxxton.microdocs.core.logging.Logger;
import com.maxxton.microdocs.core.reflect.ReflectAnnotation;
import com.maxxton.microdocs.core.reflect.ReflectClass;
import com.maxxton.microdocs.crawler.spring.Types;

/**
 * @author Steven Hermans
 */
public class DependencyCollector implements Collector<DependencyBuilder> {

  private final PathCollector pathCollector;

  public DependencyCollector(SchemaCollector schemaCollector) {
    pathCollector = new PathCollector(schemaCollector, Types.FEIGN_CLIENT);
  }

  @Override
  public List<DependencyBuilder> collect(List<ReflectClass<?>> classes) {
    List<DependencyBuilder> dependencyBuilders = new ArrayList<>();
    classes.stream().filter(reflectClass -> reflectClass.hasAnnotation(Types.FEIGN_CLIENT.getClassName())).forEach(client -> {
      Logger.get().debug("Crawl client: " + client.getSimpleName());
      dependencyBuilders.add(collect(client));
    });
    return dependencyBuilders;
  }

  private DependencyBuilder collect(ReflectClass<?> client) {
    DependencyBuilder dependencyBuilder = new DependencyBuilder();
    // collect dependency information
    ReflectAnnotation annotation = client.getAnnotation(Types.FEIGN_CLIENT.getClassName());
    if (annotation != null) {
      if (annotation.get("value") != null && annotation.get("value").getString() != null) {
        dependencyBuilder.title(annotation.get("value").getString());
      }
      else if (annotation.get("name") != null && annotation.get("name").getString() != null) {
        dependencyBuilder.title(annotation.get("name").getString());
      }
      else if (annotation.get("serviceId") != null && annotation.get("serviceId").getString() != null) {
        dependencyBuilder.title(annotation.get("serviceId").getString());
      }
      else {
        throw new RuntimeException("Missing serviceId in @FeignClient on " + client.getName());
      }
    }
    dependencyBuilder.description(client.getDescription().getText());
    dependencyBuilder.component(client);
    dependencyBuilder.type(DependencyType.REST);

    // collect paths
    List<ReflectClass<?>> clients = new ArrayList<>();
    clients.add(client);
    List<PathBuilder> pathBuilders = pathCollector.collect(clients);
    pathBuilders.forEach(dependencyBuilder::path);

    return dependencyBuilder;
  }

}
