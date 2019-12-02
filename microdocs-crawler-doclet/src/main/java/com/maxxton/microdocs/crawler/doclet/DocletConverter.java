package com.maxxton.microdocs.crawler.doclet;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import com.maxxton.microdocs.core.reflect.ClassType;
import com.maxxton.microdocs.core.reflect.ReflectAnnotation;
import com.maxxton.microdocs.core.reflect.ReflectAnnotationValue;
import com.maxxton.microdocs.core.reflect.ReflectClass;
import com.maxxton.microdocs.core.reflect.ReflectDescription;
import com.maxxton.microdocs.core.reflect.ReflectDescriptionTag;
import com.maxxton.microdocs.core.reflect.ReflectField;
import com.maxxton.microdocs.core.reflect.ReflectGenericClass;
import com.maxxton.microdocs.core.reflect.ReflectMethod;
import com.maxxton.microdocs.core.reflect.ReflectParameter;
import com.sun.source.doctree.DocTree;

/**
 * Convert Doclet classes to Reflect classes
 *
 * @author Steven Hermans
 */
public class DocletConverter {

  /**
   * Converts com.sun.javadoc.ClassDoc to ReflectClasses
   *
   * @param classDocs list of ClassDocs
   * @return List of ReflectClasses
   */
  public static List<ReflectClass<?>> convert(TypeElement... classDocs) {
    List<TypeElement> classes = new ArrayList<>();
    Collections.addAll(classes, classDocs);
    return convert(classes);
  }

  /**
   * Converts com.sun.javadoc.ClassDoc to ReflectClasses
   *
   * @param typeElements list of ClassDocs
   * @return List of ReflectClasses
   */
  public static List<ReflectClass<?>> convert(List<TypeElement> typeElements) {
    List<ReflectClass<TypeElement>> reflectClasses = new ArrayList<>();
    typeElements.forEach(typeElement -> reflectClasses.add(convertClass(typeElement)));
    reflectClasses.forEach(reflectClass -> updateClass(reflectClass, reflectClasses));
    return new ArrayList<>(reflectClasses);
  }

  private static void updateClass(ReflectClass<TypeElement> reflectClass, List<ReflectClass<TypeElement>> reflectClasses) {
    TypeElement originalTypeElement = reflectClass.getOriginal();
    // find super class
    if (originalTypeElement.getSuperclass() != null) {
      reflectClass.setSuperClass(convertGenericClass(originalTypeElement.getSuperclass(), reflectClasses));
    }
    // find interfaces
    for (TypeMirror interfaceType : originalTypeElement.getInterfaces()) {
      reflectClass.getInterfaces().add(convertGenericClass(interfaceType, reflectClasses));
    }
    //find annotations
    for (AnnotationMirror annotationDesc : originalTypeElement.getAnnotationMirrors()) {
      reflectClass.getAnnotations().add(convertAnnotation(annotationDesc));
    }
    //find fields
    for (VariableElement fieldDoc : originalTypeElement.fields(false)) {
      ReflectField field = convertField(fieldDoc, reflectClasses);
      if (field.isStatic()) {
        reflectClass.getClassFields().add(field);
      } else {
        reflectClass.getDeclaredFields().add(field);
      }
    }
    //find enum
    for (VariableElement fieldDoc : originalTypeElement.enumConstants()) {
      ReflectField constant = convertField(fieldDoc, reflectClasses);
      reflectClass.getEnumFields().add(constant);
    }
    //find methods
    for (ExecutableElement methodDoc : originalTypeElement.methods(false)) {
      ReflectMethod method = convertMethod(methodDoc, reflectClasses);
      if (method.isStatic()) {
        reflectClass.getClassMethods().add(method);
      } else {
        reflectClass.getDeclaredMethods().add(method);
      }
    }
  }

  private static ReflectMethod convertMethod(Element methodDoc, List<ReflectClass<TypeElement>> reflectClasses) {
    ReflectMethod method = new ReflectMethod();
    method.setSimpleName(methodDoc.name());
    method.setName(methodDoc.qualifiedName());
    method.setPublic(methodDoc.isPublic());
    method.setStatic(methodDoc.isStatic());
    method.setDescription(convertDoc(methodDoc));
    method.setReturnType(convertGenericClass(methodDoc.returnType(), reflectClasses));
    method.setLineNumber(methodDoc.position().line());
    //find parameters
    for (VariableElement parameter : methodDoc.parameters()) {
      method.getParameters().add(convertParameter(parameter, reflectClasses));
    }
    //find annotations
    for (AnnotationMirror annotationDesc : methodDoc.annotations()) {
      method.getAnnotations().add(convertAnnotation(annotationDesc));
    }
    return method;
  }

  private static ReflectParameter convertParameter(VariableElement parameter, List<ReflectClass<TypeElement>> reflectClasses) {
    ReflectParameter reflectParameter = new ReflectParameter();
    reflectParameter.setName(parameter.name());
    reflectParameter.setType(convertGenericClass(parameter.type(), reflectClasses));
    //find annotations
    for (AnnotationMirror annotationDesc : parameter.annotations()) {
      reflectParameter.getAnnotations().add(convertAnnotation(annotationDesc));
    }
    return reflectParameter;
  }

  private static ReflectField convertField(VariableElement fieldDoc, List<ReflectClass<TypeElement>> reflectClasses) {
    ReflectField field = new ReflectField();
    field.setSimpleName(fieldDoc.name());
    field.setName(fieldDoc.qualifiedName());
    field.setStatic(fieldDoc.isStatic());
    field.setPublic(fieldDoc.isPublic());
    field.setDescription(convertDoc(fieldDoc));
    field.setDefaultValue(fieldDoc.constantValue() != null ? fieldDoc.constantValue().toString() : null);
    field.setType(convertGenericClass(fieldDoc.type(), reflectClasses));
    //find annotations
    for (AnnotationMirror annotationDesc : fieldDoc.annotations()) {
      field.getAnnotations().add(convertAnnotation(annotationDesc));
    }
    return field;
  }

  private static ReflectAnnotation convertAnnotation(AnnotationMirror annotationDesc) {
    ReflectAnnotation annotation = new ReflectAnnotation();
    annotation.setSimpleName(annotationDesc.annotationType().simpleTypeName());
    annotation.setName(annotationDesc.annotationType().qualifiedName());
    annotation.setPackageName(annotationDesc.annotationType().containingPackage() != null ? annotationDesc.annotationType().containingPackage().name() : null);
    for (AnnotationValue pair : annotationDesc.elementValues()) {
      Object value = pair.value() != null ? pair.value().value() : null;
      if(value != null) {
        ReflectAnnotationValue annotationValue = convertAnnotationValue(value, value.toString());
        if(annotationValue != null) {
          annotation.getProperties().put(pair.element().name(), annotationValue);
        }
      }
    }
    return annotation;
  }

  private static ReflectAnnotationValue convertAnnotationValue(Object value, String raw) {
    if(value instanceof AnnotationValueImpl){
      AnnotationValueImpl valueImpl = (AnnotationValueImpl) value;
      return convertAnnotationValue(valueImpl.value(), valueImpl.toString());
    }else if(value instanceof AnnotationDescImpl){
      AnnotationDescImpl annotationDesc = (AnnotationDescImpl) value;
      ReflectAnnotation annotation = convertAnnotation(annotationDesc);
      return new ReflectAnnotationValue(raw, annotation);
    }else if(value instanceof TypeElement){
      ReflectClass<TypeElement> clazz = convertClass((TypeElement)value);
      return new ReflectAnnotationValue(raw, clazz);
    }else if (value.getClass().isArray()) {
      Object[] array = (Object[]) value;
      List<ReflectAnnotationValue> list = new ArrayList<>();
      for(Object item : array){
        list.add(convertAnnotationValue(item, ""));
      }
      return new ReflectAnnotationValue(raw, list);
    }
    return new ReflectAnnotationValue(raw, value);
  }

  private static ReflectGenericClass convertGenericClass(TypeMirror type, List<ReflectClass<TypeElement>> classes) {
    ReflectGenericClass genericClass = new ReflectGenericClass();
    Optional<ReflectClass<TypeElement>> optional = classes.stream().filter(clazz -> clazz.getName().equals(type.qualifiedTypeName())).findFirst();
    ReflectClass<TypeElement> reflectClass;
    if (optional.isPresent()) {
      reflectClass = optional.get();
    } else {
      if (type.asTypeElement() != null) {
        reflectClass = convertClass(type.asClassDoc());
      } else {
        reflectClass = new ReflectClass<>();
        reflectClass.setName(type.qualifiedTypeName());
        reflectClass.setSimpleName(type.simpleTypeName());
      }
    }
    genericClass.setClassType(reflectClass);

    if (type.asParameterizedType() != null) {
      ParameterizedType paramType = type.asParameterizedType();
      Type[] types = paramType.typeArguments();
      if (types != null) {
        for (Type t : types) {
          ReflectGenericClass genericType = convertGenericClass(t, classes);
          genericClass.getGenericTypes().add(genericType);
        }
      }
    }
    return genericClass;
  }

  private static ReflectClass<TypeElement> convertClass(TypeElement typeElement) {
    ReflectClass<TypeElement> reflectClass = new ReflectClass<>();
    reflectClass.setOriginal(typeElement);
    reflectClass.setName(typeElement.qualifiedName());
    reflectClass.setSimpleName(typeElement.simpleTypeName());
    reflectClass.setPackageName(typeElement.containingPackage() != null ? typeElement.containingPackage().name() : null);
    if (reflectClass.getPackageName() != null) {
      reflectClass.setFile(reflectClass.getPackageName().replaceAll("\\.", "/") + "/" + typeElement.position().file().getName());
    } else {
      reflectClass.setFile(typeElement.position().file().getName());
    }
    reflectClass.setAbstract(typeElement.isAbstract());
    if (typeElement.isEnum()) {
      reflectClass.setType(ClassType.ENUM);
    } else if (typeElement.isInterface()) {
      reflectClass.setType(ClassType.INTERFACE);
    } else if (typeElement.isClass()) {
      reflectClass.setType(ClassType.CLASS);
    } else {
      reflectClass.setType(ClassType.OTHER);
    }
    reflectClass.setDescription(convertDoc(typeElement));
    return reflectClass;
  }


  private static ReflectDescription convertDoc(Element element) {
    ReflectDescription reflectDescription = new ReflectDescription();
    reflectDescription.setText(element.commentText());
    for (DocTree tag : element.tags()) {
      reflectDescription.getTags().add(new ReflectDescriptionTag(tag.kind(), tag.text()));
    }
    return reflectDescription;
  }
}
