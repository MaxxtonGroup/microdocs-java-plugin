package com.maxxton.microdocs.core.collector;

import java.util.List;

import com.maxxton.microdocs.core.domain.schema.Schema;
import com.maxxton.microdocs.core.reflect.ReflectClass;
import com.maxxton.microdocs.core.reflect.ReflectGenericClass;

/**
 * @author Steven Hermans
 */
public interface SchemaParser {

  public String getClassName();

  public Schema parse(ReflectClass<?> reflectClass, List<ReflectGenericClass> genericClasses, SchemaCollector collector);

}
