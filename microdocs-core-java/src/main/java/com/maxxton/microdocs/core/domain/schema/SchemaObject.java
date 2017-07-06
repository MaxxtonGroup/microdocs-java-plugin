package com.maxxton.microdocs.core.domain.schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Steven Hermans
 */
public class SchemaObject extends Schema {

  private Map<String, Schema> properties;
  private List<Schema> anyOf;
  private List<Schema> allOf;
  private String name;
  private List<SchemaGenericObject> generic;
  @JsonIgnore
  private boolean ignore;

  public SchemaObject() {

  }

  public Map<String, Schema> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, Schema> properties) {
    this.properties = properties;
  }

  public List<Schema> getAllOf() {
    return allOf;
  }

  public void setAllOf(List<Schema> allOf) {
    this.allOf = allOf;
  }

  public List<Schema> getAnyOf() {
    return anyOf;
  }

  public void setAnyOf(List<Schema> anyOf) {
    this.anyOf = anyOf;
  }

  public void addAnyOf(Schema schema) {
    if (this.anyOf == null) {
      this.anyOf = new ArrayList();
    }
    this.anyOf.add(schema);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<SchemaGenericObject> getGeneric() {
    return generic;
  }

  public void setGeneric(List<SchemaGenericObject> generic) {
    this.generic = generic;
  }

  public boolean isIgnore() {
    return ignore;
  }

  public void setIgnore(boolean ignore) {
    this.ignore = ignore;
  }
}
