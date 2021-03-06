package com.maxxton.microdocs.core.reflect;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Steven Hermans
 */
public class ReflectGenericClass {

  @JsonIgnore
  private ReflectClass<?> classType;
  private List<ReflectGenericClass> genericTypes = new ArrayList<>();
  private boolean isArray = false;

  public ReflectClass<?> getClassType() {
    return classType;
  }

  public void setClassType(ReflectClass<?> classType) {
    this.classType = classType;
  }

  public List<ReflectGenericClass> getGenericTypes() {
    return genericTypes;
  }

  public void setGenericTypes(List<ReflectGenericClass> genericTypes) {
    this.genericTypes = genericTypes;
  }

  public boolean isArray() {
    return isArray;
  }

  public void setArray(boolean array) {
    isArray = array;
  }
}
