package com.maxxton.microdocs.core.domain.schema;

/**
 * @author Steven Hermans
 */
public final class SchemaPrimitive extends Schema {

  public SchemaPrimitive() { }

  public SchemaPrimitive(SchemaType type) {
    setType(type);
  }
}
