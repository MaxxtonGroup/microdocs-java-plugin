package com.maxxton.microdocs.crawler.doclet.scanner;

import java.util.List;

import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.SimpleTypeVisitor9;

/**
 * Scans for primitive types and gives the kind
 *
 * Maxxton Group 2019
 *
 * @author R. Sonke (r.sonke@maxxton.com)
 */
public class PrimitiveScanner extends SimpleTypeVisitor9<Void, Void> {

  private final List<TypeKind> kinds;

  public PrimitiveScanner(List<TypeKind> kinds) {
    this.kinds = kinds;
  }

  @Override
  public Void visitPrimitive(PrimitiveType t, Void aVoid) {
    kinds.add(t.getKind());
    return super.visitPrimitive(t, aVoid);
  }
}
