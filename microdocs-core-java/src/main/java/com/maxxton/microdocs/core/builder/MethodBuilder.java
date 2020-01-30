package com.maxxton.microdocs.core.builder;

import java.util.ArrayList;

import com.maxxton.microdocs.core.domain.component.Method;

/**
 * Build method
 *
 * @author Steven Hermans
 */
public class MethodBuilder implements Builder<Method> {

  private Method method = new Method();

  @Override
  public Method build() {
    return method;
  }

  public MethodBuilder name(String name) {
    this.method.setName(name);
    return this;
  }

  public MethodBuilder description(String description) {
    if (description == null) {
      this.method.setDescription("");
    }
    else {
      this.method.setDescription(description);
    }
    return this;
  }

  public MethodBuilder parameter(String paramName) {
    if (this.method.getParameters() == null) {
      this.method.setParameters(new ArrayList<>());
    }
    this.method.getParameters().add(paramName);
    return this;
  }

  public MethodBuilder lineNumber(int lineNumber) {
    this.method.setLineNumber(lineNumber);
    return this;
  }
}
