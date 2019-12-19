package com.maxxton.microdocs.crawler.doclet.scanner;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor9;

import com.maxxton.microdocs.core.reflect.ReflectAnnotation;
import com.maxxton.microdocs.core.reflect.ReflectAnnotationValue;
import com.maxxton.microdocs.core.reflect.ReflectClass;
import com.maxxton.microdocs.crawler.doclet.DocletConverter;

import jdk.javadoc.doclet.DocletEnvironment;

/**
 * A Scanner to find the values of annotations
 *
 * Maxxton Group 2019
 *
 * @author R. Sonke (r.sonke@maxxton.com)
 */
public class AnnotationValueScanner extends SimpleAnnotationValueVisitor9<ReflectAnnotationValue, Void> {

  private final AnnotationValue annotationValue;
  private final DocletEnvironment environment;

  public AnnotationValueScanner(AnnotationValue annotationValue, DocletEnvironment environment) {
    this.annotationValue = annotationValue;
    this.environment = environment;
  }

  @Override
  public ReflectAnnotationValue visitBoolean(boolean b, Void aVoid) {
    return new ReflectAnnotationValue(annotationValue.toString(), b);
  }

  @Override
  public ReflectAnnotationValue visitByte(byte b, Void aVoid) {
    return new ReflectAnnotationValue(annotationValue.toString(), b);
  }

  @Override
  public ReflectAnnotationValue visitChar(char c, Void aVoid) {
    return new ReflectAnnotationValue(annotationValue.toString(), c);
  }

  @Override
  public ReflectAnnotationValue visitDouble(double d, Void aVoid) {
    return new ReflectAnnotationValue(annotationValue.toString(), d);
  }

  @Override
  public ReflectAnnotationValue visitFloat(float f, Void aVoid) {
    return new ReflectAnnotationValue(annotationValue.toString(), f);
  }

  @Override
  public ReflectAnnotationValue visitInt(int i, Void aVoid) {
    return new ReflectAnnotationValue(annotationValue.toString(), i);
  }

  @Override
  public ReflectAnnotationValue visitLong(long i, Void aVoid) {
    return new ReflectAnnotationValue(annotationValue.toString(), i);
  }

  @Override
  public ReflectAnnotationValue visitShort(short s, Void aVoid) {
    return new ReflectAnnotationValue(annotationValue.toString(), s);
  }

  @Override
  public ReflectAnnotationValue visitString(String s, Void aVoid) {
    return new ReflectAnnotationValue(annotationValue.toString(), s);
  }

  @Override
  public ReflectAnnotationValue visitType(TypeMirror t, Void aVoid) {
    ReflectClass<TypeElement> reflectClass = DocletConverter.convertClass(environment, (TypeElement) environment.getTypeUtils().asElement(t));
    return new ReflectAnnotationValue(annotationValue.toString(), reflectClass);
  }

  @Override
  public ReflectAnnotationValue visitEnumConstant(VariableElement element, Void aVoid) {
    return new ReflectAnnotationValue(annotationValue.toString(), element);
  }

  @Override
  public ReflectAnnotationValue visitAnnotation(AnnotationMirror annotationMirror, Void aVoid) {
    ReflectAnnotation annotation = DocletConverter.convertAnnotation(environment, annotationMirror);
    return new ReflectAnnotationValue(annotationValue.toString(), annotation);
  }

  @Override
  public ReflectAnnotationValue visitArray(List<? extends AnnotationValue> values, Void aVoid) {

    List<ReflectAnnotationValue> list = new ArrayList<>();
    for(AnnotationValue item : values){
      // recursively go deeper
      AnnotationValueScanner scanner = new AnnotationValueScanner(item, environment);
      list.add(scanner.visit(item));
    }
    return new ReflectAnnotationValue(annotationValue.toString(), list);
  }
}
