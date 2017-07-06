package com.maxxton.microdocs.core.reflect;

import java.util.List;

/**
 * @author Steven Hermans
 */
public class ReflectAnnotationValue {

  private ReflectClass clazz;
  private ReflectAnnotation annotation;
  private List<ReflectAnnotationValue> list;
  private ReflectAnnotationValue child;
  private Object object;
  private String rawString;

  public ReflectAnnotationValue(String rawString, List<ReflectAnnotationValue> list){
    this.rawString = rawString;
    this.list = list;
  }

  public ReflectAnnotationValue(String rawString, ReflectAnnotationValue child){
    this.rawString = rawString;
    this.child = child;
  }

  public ReflectAnnotationValue(String rawString, ReflectAnnotation annotation){
    this.rawString = rawString;
    this.annotation = annotation;
  }

  public ReflectAnnotationValue(String rawString, ReflectClass clazz){
    this.rawString = rawString;
    this.clazz = clazz;
  }

  public ReflectAnnotationValue(String rawString, Object object){
    this.rawString = rawString;
    this.object = object;
  }

  public List<ReflectAnnotationValue> getList() {
    return list;
  }

  public ReflectAnnotationValue getChild() {
    return child;
  }

  public ReflectAnnotation getAnnotation() {
    return annotation;
  }

  public Object getObject() {
    return object;
  }

  public String getString() {
    if(this.object instanceof String){
      return this.object.toString();
    }
    return rawString;
  }

  public Integer getInt() {
    if(this.object instanceof Integer){
      return (Integer) this.object;
    }
    return null;
  }

  public Double getDouble() {
    if(this.object instanceof Double){
      return (Double) this.object;
    }
    return null;
  }

  public Boolean getBoolean() {
    if(this.object instanceof Boolean){
      return (Boolean) this.object;
    }
    return false;
  }

  public ReflectClass getClazz() {
    return clazz;
  }
}
