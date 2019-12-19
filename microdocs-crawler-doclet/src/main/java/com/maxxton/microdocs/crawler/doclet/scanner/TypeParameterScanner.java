package com.maxxton.microdocs.crawler.doclet.scanner;

import java.util.List;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleTypeVisitor9;

/**
 * Collect all TypeParameterElements, ignore the rest (for now)
 *
 * Maxxton Group 2019
 *
 * @author R. Sonke (r.sonke@maxxton.com)
 */
public class TypeParameterScanner extends SimpleTypeVisitor9<Void, Void> {

  private List<TypeMirror> typeParameters;

  public TypeParameterScanner(List<TypeMirror> typeParameters) {
    this.typeParameters = typeParameters;
  }

  @Override
  public Void visitDeclared(DeclaredType t, Void aVoid) {
    List<? extends TypeMirror> typeArguments = t.getTypeArguments();
    typeParameters.addAll(typeArguments);
    return super.visitDeclared(t, aVoid);
  }

  @Override
  public Void visitUnknown(TypeMirror t, Void aVoid) {
    return super.visitUnknown(t, aVoid);
  }
}
