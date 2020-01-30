package com.maxxton.microdocs.core.domain.schema;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Steven Hermans
 */
public class SchemaEnum extends Schema {

  @JsonProperty("enum")
  private List<String> enums;
  private String name;
  private String simpleName;

  public SchemaEnum() {

  }

  public List<String> getEnums() {
    return enums;
  }

  public void setEnums(List<String> enums) {
    this.enums = enums;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSimpleName() {
    return simpleName;
  }

  public void setSimpleName(String simpleName) {
    this.simpleName = simpleName;
  }
}
