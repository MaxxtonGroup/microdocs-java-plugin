package com.maxxton.microdocs.core.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.maxxton.microdocs.core.domain.component.Annotation;
import com.maxxton.microdocs.core.domain.component.Component;
import com.maxxton.microdocs.core.domain.component.ComponentType;
import com.maxxton.microdocs.core.domain.component.Method;
import com.maxxton.microdocs.core.logging.Logger;

/**
 * Build component
 *
 * @author Steven Hermans
 */
public class ComponentBuilder implements Builder<Component> {

  private Component component = new Component();
  private String simpleName;

  public String simpleName() {
    return simpleName;
  }

  public ComponentBuilder simpleName(String name) {
    this.simpleName = name;
    return this;
  }

  public ComponentBuilder name(String name) {
    component.setName(name);
    return this;
  }

  public ComponentBuilder file(String file) {
    component.setFile(file);
    return this;
  }

  public ComponentBuilder type(ComponentType type) {
    component.setType(type);
    return this;
  }

  public ComponentBuilder description(String description) {
    if (description == null) {
      component.setDescription("");
    }
    else {
      component.setDescription(description);
    }
    return this;
  }

  public ComponentBuilder authors(String... authors) {
      List<String> authorList = new ArrayList<>(Arrays.asList(authors));
    return authors(authorList);
  }

  public ComponentBuilder authors(List<String> authors) {
    component.setAuthors(authors);
    return this;
  }

  public ComponentBuilder annotation(AnnotationBuilder annotationBuilder) {
    return annotation(annotationBuilder.name(), annotationBuilder.build());
  }

  public ComponentBuilder annotation(String name, Annotation annotation) {
    if (component.getAnnotations() == null) {
      component.setAnnotations(new HashMap<>());
    }
    if (name != null) {
      component.getAnnotations().put(name, annotation);
    }
    else {
      Logger.get().warning("Name annotation on " + component.getName() + " is empty");
    }
    return this;
  }

  public ComponentBuilder method(MethodBuilder methodBuilder) {
    method(methodBuilder.build());
    return this;
  }

  public ComponentBuilder method(Method method) {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(method.getName()).append("(");
    if (method.getParameters() != null) {
      for (String param : method.getParameters()) {
        stringBuilder.append(param).append(",");
      }
      stringBuilder.deleteCharAt(stringBuilder.length() - 1);
    }
    stringBuilder.append(")");
    if (component.getMethods() == null) {
      component.setMethods(new HashMap<>());
    }
    component.getMethods().put(stringBuilder.toString(), method);
    return this;
  }

  public ComponentBuilder dependencies(ComponentBuilder component) {
    Component ref = new Component();
    ref.setReference("#/components/" + component.simpleName());
    this.component.getDependencies().add(ref);
    return this;
  }

  @Override
  public Component build() {
    return component;
  }
}
