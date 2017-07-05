package com.maxxton.microdocs.crawler.spring.collector;

import java.util.ArrayList;
import java.util.List;

import com.maxxton.microdocs.core.builder.SchemaMappingsBuilder;
import com.maxxton.microdocs.core.collector.SchemaCollector;
import com.maxxton.microdocs.core.collector.SchemaParser;
import com.maxxton.microdocs.core.domain.schema.Schema;
import com.maxxton.microdocs.core.reflect.ReflectAnnotation;
import com.maxxton.microdocs.core.reflect.ReflectClass;
import com.maxxton.microdocs.core.reflect.ReflectDescription;
import com.maxxton.microdocs.core.reflect.ReflectDescriptionTag;
import com.maxxton.microdocs.core.reflect.ReflectGenericClass;
import com.maxxton.microdocs.crawler.spring.parser.PageParser;
import com.maxxton.microdocs.crawler.spring.parser.ResponseEntityParser;

/**
 * Specific schema collector for JPA and Jackson
 *
 * @author Steven Hermans
 */
public class SpringSchemaCollector extends SchemaCollector {

  private static final String[] SCHEMA_TYPES = new String[] { "javax.persistence.Entity" };
  private static final String[] TABLE_TYPES = new String[] { "javax.persistence.Table", "javax.persistence.SecondaryTable" };
  private static final String COLUMN_TYPE = "javax.persistence.Column";
  private static final String ID_TYPE = "javax.persistence.Id";
  private static final String TRANSIENT_TYPE = "javax.persistence.Transient";

  private static final String JSON_PROPERTY_TYPE = "com.fasterxml.jackson.annotation.JsonProperty";
  private static final String JSON_IGNORE_TYPE = "com.fasterxml.jackson.annotation.JsonIgnore";
  private static final String JSON_SUB_TYPES = "com.fasterxml.jackson.annotation.JsonSubTypes";

  private static final String FEIGN_PROPERTY = "com.maxxton.common.feign.FeignProperty";
  private static final String IGNORE_DOWNSTREAM_CHECK = "ignoreDownstreamCheck";

  public SpringSchemaCollector() {
    super(SCHEMA_TYPES, new SchemaParser[] { new ResponseEntityParser(), new PageParser() });
  }

  @Override
  protected Schema collectObjectSchema(ReflectClass<?> reflectClass, List<ReflectGenericClass> genericClasses) {
    Schema schema = super.collectObjectSchema(reflectClass, genericClasses);
    SchemaMappingsBuilder mappingsBuilder = new SchemaMappingsBuilder();

    // Check if it is an entity class
    boolean isEntity = reflectClass.getAnnotations().stream().filter(annotation -> {
      for (String entityType : SCHEMA_TYPES) {
        if (entityType.equals(annotation.getName())) {
          return true;
        }
      }
      return false;
    }).count() > 0;
    if (isEntity) {
      List<String> tables = new ArrayList();
      for (String type : TABLE_TYPES) {
        if (reflectClass.getAnnotation(type) != null && reflectClass.getAnnotation(type).get("name") != null && !reflectClass.getAnnotation(type).get("name").getString().isEmpty()) {
          tables.add(reflectClass.getAnnotation(type).get("name").getString().replace("\"", "").toUpperCase());
        }
      }
      if (tables.isEmpty()) {
        tables.add(reflectClass.getSimpleName().replaceAll("(.)([A-Z])", "$1_$2").replace("\"", "").toUpperCase());
      }
      mappingsBuilder.relationalTables(tables);
    }
    schema.setMappings(mappingsBuilder.build());
    return schema;
  }

  @Override
  protected Schema collectProperty(String name, ReflectGenericClass type, List<ReflectAnnotation> annotations, ReflectDescription docs) {
    Schema fieldSchema = this.collect(type);
    getDefaultValue(fieldSchema, docs);
    SchemaMappingsBuilder mappingsBuilder = new SchemaMappingsBuilder();

    // CLIENT
    // Ignore field
    List<ReflectDescriptionTag> downstreamCheckTags = docs.getTags(IGNORE_DOWNSTREAM_CHECK);
    if (downstreamCheckTags != null && !downstreamCheckTags.isEmpty()) {
      mappingsBuilder.clientIgnore(true);
    }
    // Property name
    annotations.stream().filter(annotation -> annotation.getName().equals(FEIGN_PROPERTY)).forEach(annotation -> {
      if (annotation.get("value").getString() != null && !annotation.get("value").getString().isEmpty() && !name.equals(annotation.get("value").getString())) {
        mappingsBuilder.clientName(annotation.get("value").getString().replace("\"", ""));
      }
    });

    // RELATIONAL
    // Column name
    annotations.stream().filter(annotation -> annotation.getName().equals(COLUMN_TYPE)).forEach(annotation -> {
      if (annotation.get("name").getString() != null && !annotation.get("name").getString().isEmpty() && !name.equals(annotation.get("name").getString())) {
        mappingsBuilder.relationalName(annotation.get("name").getString().replace("\"", ""));
      }
    });
    // Ignore
    annotations.stream().filter(annotation -> annotation.getName().equals(TRANSIENT_TYPE)).forEach(annotation -> {
      mappingsBuilder.relationalIgnore(true);
    });
    // Id
    annotations.stream().filter(annotation -> annotation.getName().equals(ID_TYPE)).forEach(annotation -> {
      mappingsBuilder.relationalPrimary(true);
    });

    // JSON
    // Property
    annotations.stream().filter(annotation -> annotation.getName().equals(JSON_PROPERTY_TYPE)).forEach(annotation -> {
      if (annotation.get("value").getString() != null && !annotation.get("value").getString().isEmpty() && !name.equals(annotation.get("value").getString())) {
        mappingsBuilder.jsonName(annotation.get("value").getString().replace("\"", ""));
      }
    });
    // Ignore
    annotations.stream().filter(annotation -> annotation.getName().equals(JSON_IGNORE_TYPE)).forEach(annotation -> {
      mappingsBuilder.jsonIgnore(true);
    });

    fieldSchema.setMappings(mappingsBuilder.build());

    return fieldSchema;
  }

}
