package com.maxxton.microdocs.core.collector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.maxxton.microdocs.core.builder.AnnotationBuilder;
import com.maxxton.microdocs.core.builder.ComponentBuilder;
import com.maxxton.microdocs.core.builder.MethodBuilder;
import com.maxxton.microdocs.core.domain.component.ComponentType;
import com.maxxton.microdocs.core.logging.Logger;
import com.maxxton.microdocs.core.reflect.ReflectAnnotation;
import com.maxxton.microdocs.core.reflect.ReflectClass;

/**
 * Collect components
 *
 * @author Steven Hermans
 */
public class ComponentCollector implements Collector<ComponentBuilder> {

  private final Map<String, ComponentType> types;

  public ComponentCollector(Map<String, ComponentType> types) {
    this.types = types;
  }

  @Override
  public List<ComponentBuilder> collect(List<ReflectClass<?>> classes) {
    List<ComponentBuilder> componentBuilders = new ArrayList<>();
    for (ReflectClass<?> reflectClass : classes) {
      for (ReflectAnnotation annotation : reflectClass.getAnnotations()) {
        ComponentType type = null;
        for (Map.Entry<String, ComponentType> entry : types.entrySet()) {
          if (annotation.getName().equals(entry.getKey()) || annotation.getSimpleName().equals(entry.getKey())) {
            type = entry.getValue();
            break;
          }
        }
        if (type == null) {
          break;
        }
        List<String> authors = new ArrayList<>();
        reflectClass.getDescription().getTags("author").forEach(tag -> authors.add(tag.getDescription()));
        ComponentBuilder componentBuilder = new ComponentBuilder().name(reflectClass.getName()).file(reflectClass.getFile()).simpleName(reflectClass.getSimpleName()).authors(authors).type(type)
            .description(reflectClass.getDescription().getText());

        reflectClass.getDeclaredMethods().forEach(method -> {
          MethodBuilder methodBuilder = new MethodBuilder();
          methodBuilder.name(method.getSimpleName()).description(method.getDescription().getText()).lineNumber(method.getLineNumber());
          method.getParameters().stream().filter(parameter -> parameter.getType() != null).forEach(parameter -> {
            String paramName = parameter.getType().getClassType().getSimpleName();
            methodBuilder.parameter(paramName);
          });
          componentBuilder.method(methodBuilder);
        });
        reflectClass.getAnnotations().forEach(compAnnotation -> {
          AnnotationBuilder annotationBuilder = new AnnotationBuilder();
          annotationBuilder.name(compAnnotation.getSimpleName());
          compAnnotation.getProperties().forEach((key, value) -> annotationBuilder.property(key, value.toString()));
          componentBuilder.annotation(annotationBuilder);
        });
        // TODO check dependencies
        componentBuilders.add(componentBuilder);
      }
    }
    Logger.get().debug("Collected " + componentBuilders.size() + " component(s)");
    return componentBuilders;
  }

}
