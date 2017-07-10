package com.maxxton.microdocs.core.collector;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.maxxton.microdocs.core.domain.schema.Schema;
import com.maxxton.microdocs.core.domain.schema.SchemaArray;
import com.maxxton.microdocs.core.domain.schema.SchemaDummy;
import com.maxxton.microdocs.core.domain.schema.SchemaEnum;
import com.maxxton.microdocs.core.domain.schema.SchemaGenericObject;
import com.maxxton.microdocs.core.domain.schema.SchemaObject;
import com.maxxton.microdocs.core.domain.schema.SchemaPrimitive;
import com.maxxton.microdocs.core.domain.schema.SchemaType;
import com.maxxton.microdocs.core.logging.Logger;
import com.maxxton.microdocs.core.reflect.ClassType;
import com.maxxton.microdocs.core.reflect.ReflectAnnotation;
import com.maxxton.microdocs.core.reflect.ReflectClass;
import com.maxxton.microdocs.core.reflect.ReflectDescription;
import com.maxxton.microdocs.core.reflect.ReflectDescriptionTag;
import com.maxxton.microdocs.core.reflect.ReflectField;
import com.maxxton.microdocs.core.reflect.ReflectGenericClass;

/**
 * Collect Schemas
 *
 * @author Steven Hermans
 */
public class SchemaCollector {

  private Map<String, Schema> schemas = new HashMap();
  protected Map<String, String> postViews = new HashMap();

  private final String[] annotations;
  private final SchemaParser[] schemaParsers;

  public SchemaCollector(String[] annotations, SchemaParser[] schemaParsers) {
    this.annotations = annotations;
    this.schemaParsers = schemaParsers;
  }

  public Map<String, Schema> collect(List<ReflectClass<?>> classes) {
    // add models by their annotations
    Map<String, ReflectClass> models = new HashMap();
    classes.stream().filter(clazz -> clazz.hasAnnotation(annotations) && !schemas.containsKey(getSchemaName(clazz, null))).forEach(clazz -> models.put(clazz.getName(), clazz));

    // collect schemas of their models
    models.entrySet().forEach(entry -> schemas.put(getSchemaName(entry.getValue(), null), collectSchema(entry.getValue(), new ArrayList(), null)));

    // collect postViews
    postViews.forEach((className, view) -> {
      ReflectClass matchClass = classes.stream().filter(clazz -> clazz.getName().equals(className)).findFirst().orElse(null);
      collect(matchClass, view);
    });

    return schemas.entrySet().stream().filter(entry -> !entry.getValue().getType().equals(SchemaType.DUMMY)).collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
  }

  public Schema collect(ReflectClass reflectClass) {
    return collect(reflectClass, null);
  }

  public Schema collect(ReflectClass reflectClass, String view) {
    if (!schemas.containsKey(getSchemaName(reflectClass, view))) {
      Schema schema = collectSchema(reflectClass, new ArrayList(), view);
      if (schema.getType() != SchemaType.OBJECT || (schema instanceof SchemaObject && ((SchemaObject) schema).isIgnore())) {
        return schema;
      }
      schemas.put(getSchemaName(reflectClass, view), schema);
    }
    Schema schema = new SchemaPrimitive();
    schema.setReference("#/definitions/" + reflectClass.getName() + (view != null ? "#" + view : ""));
    return schema;
  }

  public Schema collect(ReflectGenericClass reflectGenericClass) {
    return collect(reflectGenericClass, null);
  }

  public Schema collect(ReflectGenericClass reflectGenericClass, String view) {
    if (reflectGenericClass.getGenericTypes().isEmpty()) {
      return collect(reflectGenericClass.getClassType(), view);
    }
    else {
      // don't define schemas globally when they have generic types
      return collectSchema(reflectGenericClass.getClassType(), reflectGenericClass.getGenericTypes(), view);
    }
  }

  private Schema collectSchema(ReflectClass reflectClass, List<ReflectGenericClass> genericClasses, String view) {
    Logger.get().debug("Collect Schema: " + reflectClass.getName());
    for (SchemaParser schemaParser : schemaParsers) {
      if (schemaParser.getClassName().equals(reflectClass.getName())) {
        return schemaParser.parse(reflectClass, genericClasses, this);
      }
    }

    if (reflectClass.getType() == ClassType.ENUM) {
      return collectEnumSchema(reflectClass);
    }
    else {
      if (reflectClass.getName().equals(Integer.class.getCanonicalName()) || reflectClass.getName().equals(Integer.TYPE.getCanonicalName()) || reflectClass.getName()
          .equals(Byte.class.getCanonicalName()) || reflectClass.getName().equals(Byte.TYPE.getCanonicalName()) || reflectClass.getName().equals(Short.class.getCanonicalName()) || reflectClass
          .getName().equals(Short.TYPE.getCanonicalName()) || reflectClass.getName().equals(Long.class.getCanonicalName()) || reflectClass.getName().equals(Long.TYPE.getCanonicalName())
          || reflectClass.getName().equals(Character.class.getCanonicalName()) || reflectClass.getName().equals(Character.TYPE.getCanonicalName())) {
        return collectIntegerSchema(reflectClass);
      }
      else if (reflectClass.getName().equals(Float.class.getCanonicalName()) || reflectClass.getName().equals(Float.TYPE.getCanonicalName()) || reflectClass.getName()
          .equals(Double.class.getCanonicalName()) || reflectClass.getName().equals(Double.TYPE.getCanonicalName())) {
        return collectNumberSchema(reflectClass);
      }
      else if (reflectClass.getName().equals(String.class.getCanonicalName())) {
        return collectStringSchema(reflectClass);
      }
      else if (reflectClass.getName().equals(Boolean.class.getCanonicalName()) || reflectClass.getName().equals(Boolean.TYPE.getCanonicalName())) {
        return collectBooleanSchema(reflectClass);
      }
      else if (reflectClass.getName().equals(Date.class.getCanonicalName()) || reflectClass.getName().equals(LocalDate.class.getCanonicalName()) || reflectClass.getName()
          .equals(LocalDateTime.class.getCanonicalName())) {
        return collectDateSchema(reflectClass);
      }
      else if (reflectClass.hasParent(List.class.getCanonicalName(), Iterator.class.getCanonicalName(), Set.class.getCanonicalName())) {
        return collectArraySchema(reflectClass, genericClasses, view);
      }
      else {
        return collectObjectSchema(reflectClass, genericClasses, view);
      }
    }
  }

  private Schema collectEnumSchema(ReflectClass<?> reflectClass) {
    SchemaEnum schema = new SchemaEnum();
    schema.setType(SchemaType.ENUM);
    schema.setName(reflectClass.getName());
    schema.setSimpleName(reflectClass.getSimpleName());
    schema.setDescription(reflectClass.getDescription().getText());
    List enums = new ArrayList();
    reflectClass.getEnumFields().forEach(field -> enums.add(field.getSimpleName()));
    schema.setEnums(enums);
    return schema;
  }

  private Schema collectIntegerSchema(ReflectClass reflectClass) {
    return new SchemaPrimitive(SchemaType.INTEGER);
  }

  private Schema collectNumberSchema(ReflectClass reflectClass) {
    return new SchemaPrimitive(SchemaType.NUMBER);
  }

  private Schema collectStringSchema(ReflectClass reflectClass) {
    return new SchemaPrimitive(SchemaType.STRING);
  }

  private Schema collectBooleanSchema(ReflectClass reflectClass) {
    return new SchemaPrimitive(SchemaType.BOOLEAN);
  }

  private Schema collectDateSchema(ReflectClass reflectClass) {
    return new SchemaPrimitive(SchemaType.DATE);
  }

  private Schema collectArraySchema(ReflectClass reflectClass, List<ReflectGenericClass> genericClasses, String view) {
    SchemaArray schema = new SchemaArray();
    schema.setType(SchemaType.ARRAY);
    if (!genericClasses.isEmpty()) {
      schema.setItems(collect(genericClasses.get(0), view));
    }
    return schema;
  }

  protected Schema collectObjectSchema(ReflectClass<?> reflectClass, List<ReflectGenericClass> genericClasses, String view) {
    // collect Map as empty object
    if (reflectClass.hasParent(Map.class.getCanonicalName()) || reflectClass.getName().equals(Object.class.getCanonicalName())) {
      SchemaObject schema = new SchemaObject();
      schema.setType(SchemaType.OBJECT);
      schema.setName(reflectClass.getSimpleName());
      schema.setIgnore(true);
      return schema;
    }

    // Set placeholder to prevent circular references
    Schema dummy = new SchemaDummy();
    dummy.setType(SchemaType.DUMMY);
    schemas.put(getSchemaName(reflectClass, view), dummy);

    SchemaObject schema = new SchemaObject();
    schema.setDescription(reflectClass.getDescription().getText());
    schema.setType(SchemaType.OBJECT);
    schema.setName(reflectClass.getSimpleName());
    schema.setGeneric(collectGeneric(genericClasses));
    Map<String, Schema> properties = new HashMap();

    // Collect getter and setters
    Map<String, PropertyBucket> propertyBuckets = new HashMap();
    reflectClass.getDeclaredMethods().stream()
        .filter(method -> method.getSimpleName().startsWith("is") || method.getSimpleName().startsWith("has") || method.getSimpleName().startsWith("get") || method.getSimpleName().startsWith("set"))
        .forEach(method -> {
          String propertyName = method.getSimpleName().startsWith("is") ? method.getSimpleName().substring(2) : method.getSimpleName().substring(3);
          propertyName = propertyName.substring(0, 1).toLowerCase() + propertyName.substring(1);
          ReflectGenericClass type = null;
          if (method.getSimpleName().startsWith("set")) {
            if (!method.getParameters().isEmpty()) {
              type = method.getParameters().get(0).getType();
            }
          }
          else {
            type = method.getReturnType();
          }

          PropertyBucket bucket;
          if (propertyBuckets.containsKey(propertyName)) {
            bucket = propertyBuckets.get(propertyName);
          }
          else {
            bucket = new PropertyBucket();
            propertyBuckets.put(propertyName, bucket);
          }

          bucket.setType(type);
          bucket.addAnnotations(method.getAnnotations());
          bucket.addDescription(method.getDescription());
        });

    // Collect properties
    for (ReflectField field : reflectClass.getDeclaredFields()) {
      String propertyName = field.getSimpleName();
      if (propertyBuckets.containsKey(propertyName)) {
        PropertyBucket bucket = propertyBuckets.get(propertyName);
        bucket.setType(field.getType());
        bucket.addAnnotations(field.getAnnotations());
        bucket.addDescription(field.getDescription());
      }
    }

    // Empty property buckets
    Logger.get().debug(propertyBuckets.size() + " buckets in " + reflectClass.getSimpleName());
    for (Map.Entry<String, PropertyBucket> entry : propertyBuckets.entrySet()) {
      Schema propertySchema = collectProperty(entry.getKey(), entry.getValue().getType(), entry.getValue().getAnnotations(), entry.getValue().getDescription(), view);
      if (matchView(propertySchema, view)) {
        properties.put(entry.getKey(), propertySchema);
      }
    }

    schema.setProperties(properties);

    if (reflectClass.getSuperClass() != null && reflectClass.getSuperClass().getClassType() != null && !Object.class.getName().equals(reflectClass.getSuperClass().getClassType().getName())) {
      Schema superSchema = collect(reflectClass.getSuperClass(), view);
      List<Schema> superList = new ArrayList();
      superList.add(superSchema);
      schema.setAllOf(superList);
    }

    return schema;
  }

  private List<SchemaGenericObject> collectGeneric(List<ReflectGenericClass> genericClasses) {
    if (genericClasses.isEmpty()) {
      return null;
    }
    List<SchemaGenericObject> generics = new ArrayList();
    for (ReflectGenericClass clazz : genericClasses) {
      SchemaGenericObject generic = new SchemaGenericObject();
      generic.setName(clazz.getClassType().getName());
      generic.setSimpleName(clazz.getClassType().getSimpleName());
      generic.setGeneric(collectGeneric(clazz.getGenericTypes()));
      generics.add(generic);
    }
    return generics;
  }

  protected Schema collectProperty(String name, ReflectGenericClass type, List<ReflectAnnotation> annotations, ReflectDescription docs, String view) {
    Schema fieldSchema = collect(type, view);
    getDefaultValue(fieldSchema, docs);
    return fieldSchema;
  }

  protected void getDefaultValue(Schema fieldSchema, ReflectDescription docs) {
    if (docs.getTags("example") != null) {
      docs.getTags("example").forEach(tag -> {
        try {
          if (fieldSchema.getType() == SchemaType.BOOLEAN) {
            fieldSchema.setDefaultValue(Boolean.parseBoolean(tag.getContent()));
          }
          else if (fieldSchema.getType() == SchemaType.INTEGER) {
            fieldSchema.setDefaultValue(Integer.parseInt(tag.getContent()));
          }
          else if (fieldSchema.getType() == SchemaType.NUMBER) {
            fieldSchema.setDefaultValue(Float.parseFloat(tag.getContent()));
          }
          else if (fieldSchema.getType() == SchemaType.DATE) {
            fieldSchema.setDefaultValue(tag.getContent());
          }
          else if (fieldSchema.getType() == SchemaType.STRING) {
            fieldSchema.setDefaultValue(tag.getContent());
          }
        }
        catch (Exception e) {
        }
      });
    }
  }

  protected boolean matchView(Schema schema, String view) {
    if(view == null){
      return true;
    }
    if(schema.getMappings() != null && schema.getMappings().getJson() != null && schema.getMappings().getJson().getViews() != null){
      return schema.getMappings().getJson().getViews().contains(view);
    }
    return true;
  }

  protected String getSchemaName(ReflectClass reflectClass, String view){
    return reflectClass.getName() + (view != null ? "#" + view : "");
  }

  private class PropertyBucket {

    private ReflectGenericClass type;
    private List<ReflectAnnotation> annotations = new ArrayList();
    private List<ReflectDescription> descriptions = new ArrayList();

    public ReflectGenericClass getType() {
      return type;
    }

    public List<ReflectAnnotation> getAnnotations() {
      return annotations;
    }

    public ReflectDescription getDescription() {
      ReflectDescription desc = new ReflectDescription();
      if (!descriptions.isEmpty()) {
        for (int i = descriptions.size() - 1; i >= 0; i--) {
          if (descriptions.get(i).getText() != null && !descriptions.get(i).getText().trim().isEmpty()) {
            desc.setText(descriptions.get(i).getText());
            break;
          }
        }
        List<ReflectDescriptionTag> tags = new ArrayList();
        descriptions.forEach(description -> {
          description.getTags().forEach(tag -> {
            tags.add(tag);
          });
        });
        desc.setTags(tags);
      }
      return desc;
    }

    public void addDescription(ReflectDescription description) {
      descriptions.add(description);
    }

    public void addAnnotations(List<ReflectAnnotation> annotations) {
      this.annotations.addAll(annotations);
    }

    public void setType(ReflectGenericClass type) {
      this.type = type;
    }
  }
}
