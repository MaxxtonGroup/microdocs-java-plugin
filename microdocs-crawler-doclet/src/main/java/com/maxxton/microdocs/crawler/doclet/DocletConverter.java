package com.maxxton.microdocs.crawler.doclet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.JavaFileObject;

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
import com.sun.source.doctree.BlockTagTree;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.DocTrees;
import com.sun.source.util.TreePath;

import jdk.javadoc.doclet.DocletEnvironment;

/**
 * Convert Doclet classes to Reflect classes
 *
 * @author Steven Hermans
 * @author Rob Sonke
 */
public class DocletConverter {

  /**
   * Converts com.sun.javadoc.ClassDoc to ReflectClasses
   *
   * @param classDocs list of ClassDocs
   * @return List of ReflectClasses
   */
  public static List<ReflectClass<?>> convert(DocletEnvironment docletEnvironment, TypeElement... classDocs) {
    List<TypeElement> classes = new ArrayList<>();
    Collections.addAll(classes, classDocs);
    return convert(docletEnvironment, classes);
  }

  /**
   * Converts javax.lang.model.element.TypeElement to ReflectClasses
   *
   * @param typeElements list of TypeElements
   * @return List of ReflectClasses
   */
  public static List<ReflectClass<?>> convert(DocletEnvironment docletEnvironment, List<TypeElement> typeElements) {
    List<ReflectClass<TypeElement>> reflectClasses = new ArrayList<>();
    typeElements.forEach(typeElement -> reflectClasses.add(convertClass(docletEnvironment, typeElement)));
    reflectClasses.forEach(reflectClass -> updateClass(docletEnvironment, reflectClass, reflectClasses));
    return new ArrayList<>(reflectClasses);
  }

  private static void updateClass(DocletEnvironment docletEnvironment, ReflectClass<TypeElement> reflectClass, List<ReflectClass<TypeElement>> reflectClasses) {
    TypeElement originalTypeElement = reflectClass.getOriginal();
    // find super class
    if (originalTypeElement.getSuperclass() != null) {
      reflectClass.setSuperClass(convertGenericClass(docletEnvironment, originalTypeElement.getSuperclass(), reflectClasses));
    }
    // find interfaces
    for (TypeMirror interfaceType : originalTypeElement.getInterfaces()) {
      reflectClass.getInterfaces().add(convertGenericClass(docletEnvironment, interfaceType, reflectClasses));
    }
    //find annotations
    for (AnnotationMirror annotationDesc : originalTypeElement.getAnnotationMirrors()) {
      reflectClass.getAnnotations().add(convertAnnotation(docletEnvironment, annotationDesc));
    }
    //find fields
    for (VariableElement fieldDoc : ElementFilter.fieldsIn(originalTypeElement.getEnclosedElements())) {
        ReflectField field = convertField(docletEnvironment, fieldDoc, reflectClasses);
        if (fieldDoc.getKind().equals(ElementKind.ENUM_CONSTANT)) {
            reflectClass.getEnumFields().add(field);
        }
        else {
            // normal field
            if (field.isStatic()) {
              reflectClass.getClassFields().add(field);
            }
            else {
              reflectClass.getDeclaredFields().add(field);
            }
        }
    }
    //find methods
    for (ExecutableElement methodDoc : ElementFilter.methodsIn(originalTypeElement.getEnclosedElements())) {
      ReflectMethod method = convertMethod(docletEnvironment, methodDoc, reflectClasses);
      if (method.isStatic()) {
        reflectClass.getClassMethods().add(method);
      } else {
        reflectClass.getDeclaredMethods().add(method);
      }
    }
  }

  private static ReflectMethod convertMethod(DocletEnvironment docletEnvironment, ExecutableElement executableMethod, List<ReflectClass<TypeElement>> reflectClasses) {
    ReflectMethod method = new ReflectMethod();
    method.setSimpleName(executableMethod.getSimpleName().toString());
    //method.setName(executableMethod. qualifiedName());
    method.setPublic(executableMethod.getModifiers().contains(Modifier.PUBLIC));
    method.setStatic(executableMethod.getModifiers().contains(Modifier.STATIC));
    method.setDescription(convertDoc(docletEnvironment, executableMethod));
    method.setReturnType(convertGenericClass(docletEnvironment, executableMethod.getReturnType(), reflectClasses));

    // TODO check if correct
    MethodTree tree = docletEnvironment.getDocTrees().getTree(executableMethod);
    DocTrees docTrees = docletEnvironment.getDocTrees();
    TreePath dct = docTrees.getPath(executableMethod);
    long lineNumber = docletEnvironment.getDocTrees().getSourcePositions().getStartPosition(dct.getCompilationUnit(), tree);
    method.setLineNumber((int) lineNumber);

    //find parameters
    for (VariableElement parameter : executableMethod.getParameters()) {
      method.getParameters().add(convertParameter(docletEnvironment, parameter, reflectClasses));
    }

    //find annotations
    for (AnnotationMirror annotationDesc : executableMethod.getAnnotationMirrors()) {
      method.getAnnotations().add(convertAnnotation(docletEnvironment, annotationDesc));
    }
    return method;
  }

  private static ReflectParameter convertParameter(DocletEnvironment docletEnvironment, VariableElement parameter, List<ReflectClass<TypeElement>> reflectClasses) {
    ReflectParameter reflectParameter = new ReflectParameter();
    reflectParameter.setName(parameter.getSimpleName().toString());
    reflectParameter.setType(convertGenericClass(docletEnvironment, parameter.asType(), reflectClasses));
    //find annotations
    for (AnnotationMirror annotationMirror : parameter.getAnnotationMirrors()) {
      reflectParameter.getAnnotations().add(convertAnnotation(docletEnvironment, annotationMirror));
    }
    return reflectParameter;
  }

  /**
   * Convert a java field (a class member) into a ReflectField object
   * @return a reflectfield instance
   */
  private static ReflectField convertField(DocletEnvironment docletEnvironment, VariableElement fieldDoc, List<ReflectClass<TypeElement>> reflectClasses) {
      ReflectField field = new ReflectField();
      field.setSimpleName(fieldDoc.getSimpleName().toString());
      // use the same, variable element does not have a qualified name
      field.setName(fieldDoc.getSimpleName().toString());
      field.setStatic(fieldDoc.getModifiers().contains(Modifier.STATIC));
      field.setPublic(fieldDoc.getModifiers().contains(Modifier.PUBLIC));
      field.setDescription(convertDoc(docletEnvironment, fieldDoc));
      field.setDefaultValue(fieldDoc.getConstantValue() != null ? fieldDoc.getConstantValue().toString() : null);
      field.setType(convertGenericClass(docletEnvironment, fieldDoc.asType(), reflectClasses));
      //find annotations
      for (AnnotationMirror annotationDesc : fieldDoc.getAnnotationMirrors()) {
          field.getAnnotations().add(convertAnnotation(docletEnvironment, annotationDesc));
      }
      return field;
  }

  private static ReflectAnnotation convertAnnotation(DocletEnvironment docletEnvironment, AnnotationMirror annotationDesc) {
    ReflectAnnotation annotation = new ReflectAnnotation();
    // TODO check if this really gives the right names of the annotation
    TypeElement annotationElement = (TypeElement) annotationDesc.getAnnotationType().asElement();
    annotation.setSimpleName(annotationElement.getSimpleName().toString());
    annotation.setName(annotationElement.getQualifiedName().toString());

    PackageElement packageElement = docletEnvironment.getElementUtils().getPackageOf(annotationElement);
    Name packageName = packageElement.getSimpleName();
    annotation.setPackageName(packageName != null ? packageName.toString() : null);

    // check the annotation values
    for (ExecutableElement executableElement : annotationDesc.getElementValues().keySet()) {
      AnnotationValue annotationValue = annotationDesc.getElementValues().get(executableElement);
      Object value = annotationValue != null ? annotationValue.getValue() : null;
      if (value != null) {
        ReflectAnnotationValue reflectAnnotationValue = convertAnnotationValue(docletEnvironment, annotationValue, annotationValue.getValue(), value.toString());
        // TODO proper name?
        annotation.getProperties().put(executableElement.getSimpleName().toString(), reflectAnnotationValue);
      }
    }

    return annotation;
  }

  private static ReflectAnnotationValue convertAnnotationValue(DocletEnvironment docletEnvironment, AnnotationValue annotationValue, Object value, String raw) {
    // TODO check if this would be needed
//    if(value instanceof AnnotationValueImpl){
//      AnnotationValueImpl valueImpl = (AnnotationValueImpl) value;
//      return convertAnnotationValue(valueImpl.value(), valueImpl.toString());
//    }else if(value instanceof AnnotationDescImpl){
//      AnnotationDescImpl annotationDesc = (AnnotationDescImpl) value;
//      ReflectAnnotation annotation = convertAnnotation(docletEnvironment, annotationDesc);
//      return new ReflectAnnotationValue(raw, annotation);
//    }else if(value instanceof TypeElement){
//      ReflectClass<TypeElement> clazz = convertClass(docletEnvironment, (TypeElement)value);
//      return new ReflectAnnotationValue(raw, clazz);
//    }
    if (value.getClass().isArray()) {
      Object[] array = (Object[]) value;
      List<ReflectAnnotationValue> list = new ArrayList<>();
      for(Object item : array){
        list.add(convertAnnotationValue(docletEnvironment, annotationValue, item, ""));
      }
      return new ReflectAnnotationValue(raw, list);
    }
    return new ReflectAnnotationValue(raw, value);
  }

  private static ReflectGenericClass convertGenericClass(DocletEnvironment docletEnvironment, TypeMirror type, List<ReflectClass<TypeElement>> reflectClasses) {
    ReflectGenericClass genericClass = new ReflectGenericClass();
    TypeElement typeElement = (TypeElement) docletEnvironment.getTypeUtils().asElement(type);

    Optional<ReflectClass<TypeElement>> optional = reflectClasses.stream().filter(clazz -> clazz.getName().equals(typeElement.getQualifiedName().toString())).findFirst();
    ReflectClass<TypeElement> reflectClass;
    reflectClass = optional.orElseGet(() -> convertClass(docletEnvironment, typeElement));
    genericClass.setClassType(reflectClass);

    List<? extends TypeParameterElement> typeParameters = ((TypeElement) docletEnvironment.getTypeUtils().asElement(type)).getTypeParameters();
    if (!typeParameters.isEmpty()) {
        for (TypeParameterElement parameterElement : typeParameters) {
            ReflectGenericClass genericType = convertGenericClass(docletEnvironment, parameterElement.asType(), reflectClasses);
            genericClass.getGenericTypes().add(genericType);
        }
    }
    return genericClass;
  }

  /**
   * Convert one TypeElement into a ReflectClass
   * @param typeElement the to be converted element
   * @return the reflect class instance
   */
  private static ReflectClass<TypeElement> convertClass(DocletEnvironment docletEnvironment, TypeElement typeElement) {
    ReflectClass<TypeElement> reflectClass = new ReflectClass<>();
    reflectClass.setOriginal(typeElement);
    reflectClass.setName(typeElement.getQualifiedName().toString());
    reflectClass.setSimpleName(typeElement.getSimpleName().toString());

    PackageElement packageElement = docletEnvironment.getElementUtils().getPackageOf(typeElement);
    Name simpleName = packageElement.getSimpleName();
    reflectClass.setPackageName(simpleName != null ? simpleName.toString() : null);

    // use the doctrees to get to the original filename of the typeelement
    DocTrees docTrees = docletEnvironment.getDocTrees();
    TreePath dct = docTrees.getPath(typeElement);
    JavaFileObject fileObject = dct.getCompilationUnit().getSourceFile();

    if (reflectClass.getPackageName() != null) {
        reflectClass.setFile(reflectClass.getPackageName().replaceAll("\\.", "/") + "/" + fileObject.getName());
    } else {
        reflectClass.setFile(fileObject.getName());
    }

    reflectClass.setAbstract(typeElement.getModifiers().contains(Modifier.ABSTRACT));
    if (typeElement.getKind().equals(ElementKind.ENUM)) {
        reflectClass.setType(ClassType.ENUM);
    } else if (typeElement.getKind().isInterface()) {
        reflectClass.setType(ClassType.INTERFACE);
    } else if (typeElement.getKind().isClass()) {
        reflectClass.setType(ClassType.CLASS);
    } else {
        reflectClass.setType(ClassType.OTHER);
    }
    reflectClass.setDescription(convertDoc(docletEnvironment, typeElement));
    return reflectClass;
  }


  private static ReflectDescription convertDoc(DocletEnvironment docletEnvironment, Element element) {
      ReflectDescription reflectDescription = new ReflectDescription();
      DocTrees docTrees = docletEnvironment.getDocTrees();
      String comment = docTrees.getDocComment(docTrees.getPath(element));
      DocCommentTree docCommentTree = docTrees.getDocCommentTree(element);

      reflectDescription.setText(comment);
      for (DocTree tag : docCommentTree.getBlockTags()) {
          BlockTagTree blockTag = (BlockTagTree) tag;
          // TODO check if getTagName returns the tag value, otherwise we need subinterfaces of BlockTagTree
          reflectDescription.getTags().add(new ReflectDescriptionTag(blockTag.getKind().tagName, blockTag.getTagName()));
      }
      return reflectDescription;
  }
}
