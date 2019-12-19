package com.maxxton.microdocs.crawler.doclet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

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
import com.maxxton.microdocs.crawler.doclet.scanner.AnnotationValueScanner;
import com.maxxton.microdocs.crawler.doclet.scanner.PrimitiveScanner;
import com.maxxton.microdocs.crawler.doclet.scanner.TagScanner;
import com.maxxton.microdocs.crawler.doclet.scanner.TypeParameterScanner;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.DocTrees;
import com.sun.source.util.TreePath;

import jdk.javadoc.doclet.DocletEnvironment;

/**
 * Convert Doclet classes to Reflect classes
 * *
 * @author Steven Hermans
 * @author Rob Sonke
 */
public class DocletConverter {

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
      ReflectGenericClass genericClass = convertGenericClass(docletEnvironment, interfaceType, reflectClasses);
      if (genericClass != null) {
        reflectClass.getInterfaces().add(genericClass);
      }
    }
    // find annotations
    for (AnnotationMirror annotationDesc : originalTypeElement.getAnnotationMirrors()) {
      reflectClass.getAnnotations().add(convertAnnotation(docletEnvironment, annotationDesc));
    }
    // find fields
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
    // find methods
    for (ExecutableElement methodDoc : ElementFilter.methodsIn(originalTypeElement.getEnclosedElements())) {
      ReflectMethod method = convertMethod(docletEnvironment, methodDoc, reflectClasses);
      if (method.isStatic()) {
        reflectClass.getClassMethods().add(method);
      }
      else {
        reflectClass.getDeclaredMethods().add(method);
      }
    }
  }

  private static ReflectMethod convertMethod(DocletEnvironment docletEnvironment, ExecutableElement executableMethod, List<ReflectClass<TypeElement>> reflectClasses) {
    ReflectMethod method = new ReflectMethod();
    method.setSimpleName(executableMethod.getSimpleName().toString());
    method.setPublic(executableMethod.getModifiers().contains(Modifier.PUBLIC));
    method.setStatic(executableMethod.getModifiers().contains(Modifier.STATIC));
    method.setDescription(convertDoc(docletEnvironment, executableMethod));
    method.setReturnType(convertGenericClass(docletEnvironment, executableMethod.getReturnType(), reflectClasses));

    MethodTree tree = docletEnvironment.getDocTrees().getTree(executableMethod);
    if (tree != null) {
      DocTrees docTrees = docletEnvironment.getDocTrees();
      TreePath dct = docTrees.getPath(executableMethod);
      long position = docletEnvironment.getDocTrees().getSourcePositions().getStartPosition(dct.getCompilationUnit(), tree);
      long lineNumber = docletEnvironment.getDocTrees().getPath(executableMethod).getCompilationUnit().getLineMap().getLineNumber(position);
      method.setLineNumber((int) lineNumber);
    }

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
   *
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

  public static ReflectAnnotation convertAnnotation(DocletEnvironment docletEnvironment, AnnotationMirror annotationDesc) {
    ReflectAnnotation annotation = new ReflectAnnotation();

    TypeElement annotationElement = (TypeElement) annotationDesc.getAnnotationType().asElement();
    annotation.setSimpleName(annotationElement.getSimpleName().toString());
    annotation.setName(annotationElement.getQualifiedName().toString());

    PackageElement packageElement = docletEnvironment.getElementUtils().getPackageOf(annotationElement);
    Name packageName = packageElement.getQualifiedName();
    annotation.setPackageName(packageName != null ? packageName.toString() : null);

    // check the annotation values
    for (ExecutableElement executableElement : annotationDesc.getElementValues().keySet()) {
      AnnotationValue annotationValue = annotationDesc.getElementValues().get(executableElement);
      AnnotationValueScanner scanner = new AnnotationValueScanner(annotationValue, docletEnvironment);
      ReflectAnnotationValue reflectAnnotationValue = scanner.visit(annotationValue);
      annotation.getProperties().put(executableElement.getSimpleName().toString(), reflectAnnotationValue);
    }

    return annotation;
  }

  private static ReflectGenericClass convertGenericClass(DocletEnvironment docletEnvironment, TypeMirror type, List<ReflectClass<TypeElement>> reflectClasses) {
    // type could be void too
    if (type.toString().equals("void")) {
      return null;
    }

    ReflectGenericClass genericClass = new ReflectGenericClass();
    Element element = docletEnvironment.getTypeUtils().asElement(type);
    // this could be TypeVar variables too, such as T, K, E
    if (!type.getKind().isPrimitive() && element instanceof TypeElement) {
      TypeElement typeElement = (TypeElement) element;
      // search for the class in our own set, otherwise just call convertClass again
      Optional<ReflectClass<TypeElement>> optional = reflectClasses.stream().filter(clazz -> clazz.getName().equals(typeElement.getQualifiedName().toString())).findFirst();
      ReflectClass<TypeElement> reflectClass;
      reflectClass = optional.orElseGet(() -> convertClass(docletEnvironment, typeElement));
      genericClass.setClassType(reflectClass);

      List<TypeMirror> typeParameters = new ArrayList<>();
      TypeParameterScanner typeScanner = new TypeParameterScanner(typeParameters);
      typeScanner.visit(type);
      if (!typeParameters.isEmpty()) {
        for (TypeMirror parameterType : typeParameters) {
          ReflectGenericClass genericType = convertGenericClass(docletEnvironment, parameterType, reflectClasses);
          if (genericType != null) {
            genericClass.getGenericTypes().add(genericType);
          }
        }
      }
      return genericClass;
    }
    else if (type.getKind().isPrimitive()) {
      // use the scanner to figure out what primitive type
      List<TypeKind> result = new ArrayList<>();
      PrimitiveScanner scanner = new PrimitiveScanner(result);
      scanner.visit(type);
      ReflectClass<Void> reflectClass = new ReflectClass<>();
      reflectClass.setName(result.get(0).name().toLowerCase());
      reflectClass.setSimpleName(result.get(0).name().toLowerCase());
      genericClass.setClassType(reflectClass);
      return genericClass;
    }
    return null;
  }

  /**
   * Convert one TypeElement into a ReflectClass
   *
   * @param typeElement the to be converted element
   * @return the reflect class instance
   */
  public static ReflectClass<TypeElement> convertClass(DocletEnvironment docletEnvironment, TypeElement typeElement) {
    ReflectClass<TypeElement> reflectClass = new ReflectClass<>();
    reflectClass.setOriginal(typeElement);
    reflectClass.setName(typeElement.getQualifiedName().toString());
    reflectClass.setSimpleName(typeElement.getSimpleName().toString());

    PackageElement packageElement = docletEnvironment.getElementUtils().getPackageOf(typeElement);
    Name packageName = packageElement.getQualifiedName();
    reflectClass.setPackageName(packageName != null ? packageName.toString() : null);

    // use the doctrees to get to the original filename of the typeelement
    // this could remain empty in case of third party classes such as spring ones
    if (reflectClass.getPackageName() != null) {
      reflectClass.setFile(reflectClass.getPackageName().replaceAll("\\.", "/") + "/" + reflectClass.getSimpleName() + ".java");
    }
    else {
      reflectClass.setFile(reflectClass.getSimpleName() + ".java");
    }
    reflectClass.setAbstract(typeElement.getModifiers().contains(Modifier.ABSTRACT));
    if (typeElement.getKind().equals(ElementKind.ENUM)) {
      reflectClass.setType(ClassType.ENUM);
    }
    else if (typeElement.getKind().isInterface()) {
      reflectClass.setType(ClassType.INTERFACE);
    }
    else if (typeElement.getKind().isClass()) {
      reflectClass.setType(ClassType.CLASS);
    }
    else {
      reflectClass.setType(ClassType.OTHER);
    }
    reflectClass.setDescription(convertDoc(docletEnvironment, typeElement));
    return reflectClass;
  }

  private static ReflectDescription convertDoc(DocletEnvironment docletEnvironment, Element element) {
    ReflectDescription reflectDescription = new ReflectDescription();
    DocTrees docTrees = docletEnvironment.getDocTrees();
    TreePath path = docTrees.getPath(element);
    // if the path is null, there's no access to the source, so no comments either
    if (path != null && docTrees.getDocCommentTree(path) != null) {
      DocCommentTree docCommentTree = docTrees.getDocCommentTree(element);
      String comment = docCommentTree.getFullBody().stream().map(Object::toString).collect(Collectors.joining(" "));
      reflectDescription.setText(comment);
      Map<String, ReflectDescriptionTag> tags = new HashMap<>();
      TagScanner scanner = new TagScanner(tags);
      scanner.visitDocComment(docCommentTree, null);
      reflectDescription.getTags().addAll(tags.values());
    }
    return reflectDescription;
  }
}
