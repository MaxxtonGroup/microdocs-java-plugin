package com.maxxton.microdocs.crawler.spring.collector;

import java.util.List;

import com.maxxton.microdocs.core.collector.SchemaCollector;
import com.maxxton.microdocs.core.domain.path.Parameter;
import com.maxxton.microdocs.core.reflect.ReflectClass;
import com.maxxton.microdocs.core.reflect.ReflectMethod;
import com.maxxton.microdocs.core.reflect.ReflectParameter;

/**
 * @author Steven Hermans
 */
public interface RequestParser {

  public String getClassName();

  public List<Parameter> parse(ReflectParameter reflectParameter, ReflectClass<?> controller, ReflectMethod method, SchemaCollector schemaCollector);

}
