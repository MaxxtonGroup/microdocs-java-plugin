package com.maxxton.microdocs.crawler.spring.parser;

import java.util.ArrayList;
import java.util.List;

import com.maxxton.microdocs.core.collector.SchemaCollector;
import com.maxxton.microdocs.core.domain.path.Parameter;
import com.maxxton.microdocs.core.domain.path.ParameterPlacing;
import com.maxxton.microdocs.core.domain.path.ParameterVariable;
import com.maxxton.microdocs.core.domain.schema.SchemaType;
import com.maxxton.microdocs.core.reflect.ReflectClass;
import com.maxxton.microdocs.core.reflect.ReflectMethod;
import com.maxxton.microdocs.core.reflect.ReflectParameter;
import com.maxxton.microdocs.crawler.spring.collector.RequestParser;

/**
 * @author Steven Hermans
 */
public class SpecificationsParser implements RequestParser {

  @Override
  public String getClassName() {
    return "org.springframework.data.jpa.domain.Specifications";
  }

  @Override
  public List<Parameter> parse(ReflectParameter reflectParameter, ReflectClass controller, ReflectMethod method, SchemaCollector schemaCollector) {
    List<Parameter> parameters = new ArrayList<>();

    ParameterVariable filterParameter = new ParameterVariable();
    filterParameter.setName("filter");
    filterParameter.setIn(ParameterPlacing.QUERY);
    filterParameter.setType(SchemaType.STRING);
    filterParameter.setDescription("Filter query");
    parameters.add(filterParameter);

    return parameters;
  }
}
