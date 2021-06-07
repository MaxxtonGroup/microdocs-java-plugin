package com.maxxton.microdocs.crawler.spring.collector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.maxxton.microdocs.core.builder.PathBuilder;
import com.maxxton.microdocs.core.collector.Collector;
import com.maxxton.microdocs.core.collector.SchemaCollector;
import com.maxxton.microdocs.core.domain.path.Parameter;
import com.maxxton.microdocs.core.domain.path.ParameterBody;
import com.maxxton.microdocs.core.domain.path.ParameterPlacing;
import com.maxxton.microdocs.core.domain.path.ParameterVariable;
import com.maxxton.microdocs.core.domain.path.Response;
import com.maxxton.microdocs.core.domain.schema.Schema;
import com.maxxton.microdocs.core.domain.schema.SchemaObject;
import com.maxxton.microdocs.core.domain.schema.SchemaType;
import com.maxxton.microdocs.core.logging.Logger;
import com.maxxton.microdocs.core.reflect.ReflectAnnotation;
import com.maxxton.microdocs.core.reflect.ReflectAnnotationValue;
import com.maxxton.microdocs.core.reflect.ReflectClass;
import com.maxxton.microdocs.core.reflect.ReflectDescriptionTag;
import com.maxxton.microdocs.core.reflect.ReflectMethod;
import com.maxxton.microdocs.core.reflect.ReflectParameter;
import com.maxxton.microdocs.crawler.spring.Types;
import com.maxxton.microdocs.crawler.spring.parser.PageableParser;
import com.maxxton.microdocs.crawler.spring.parser.SpecificationsParser;

/**
 * @author Steven Hermans
 */
public class PathCollector implements Collector<PathBuilder> {

  private final String[] defaultConsumes = new String[] { "application/json" };
  private final String[] defaultProduces = new String[] { "application/json" };

  private static final String TYPE_REQUEST_BODY = "org.springframework.web.bind.annotation.RequestBody";
  private static final String TYPE_REQUEST_PARAM = "org.springframework.web.bind.annotation.RequestParam";
  private static final String TYPE_PATH_VARIABLE = "org.springframework.web.bind.annotation.PathVariable";

  private static final String JSON_VIEW = "com.fasterxml.jackson.annotation.JsonView";

  private SchemaCollector schemaCollector;
  private final String[] controllers;
  private final List<String> requestMappers = Arrays
      .asList(Types.REQUEST_MAPPING.getClassName(), Types.GET_MAPPING.getClassName(), Types.DELETE_MAPPING.getClassName(), Types.PATCH_MAPPING.getClassName(), Types.POST_MAPPING.getClassName(),
          Types.PUT_MAPPING.getClassName());

  private final RequestParser[] requestParsers = new RequestParser[] { new PageableParser(), new SpecificationsParser() };

  public PathCollector(SchemaCollector schemaCollector, Types... controllers) {
    this.schemaCollector = schemaCollector;
    this.controllers = new String[controllers.length];
    for (int i = 0; i < controllers.length; i++) {
      this.controllers[i] = controllers[i].getClassName();
    }

  }

  @Override
  public List<PathBuilder> collect(List<ReflectClass<?>> classes) {
    List<PathBuilder> pathBuilders = new ArrayList<>();
    classes.stream().filter(reflectClass -> reflectClass.hasAnnotation(controllers)).forEach(controller -> {
      Logger.get().debug("Crawl controller: " + controller.getSimpleName());
      String[] mappers = requestMappers.toArray(new String[0]);
      controller.getDeclaredMethods().stream().filter(method -> method.hasAnnotation(mappers)).forEach(method -> {
        Logger.get().debug("Crawl controller method: " + method.getSimpleName());
        pathBuilders.addAll(collectPaths(controller, method));
      });
    });
    return pathBuilders;
  }

  private List<PathBuilder> collectPaths(ReflectClass<?> controller, ReflectMethod method) {
    // Get first mappings
    ReflectAnnotation controllerRequestMapping = controller.getAnnotations().stream().filter(annotation -> requestMappers.stream().anyMatch(mapper -> mapper.equals(annotation.getName()))).findFirst()
        .orElse(null);
    ReflectAnnotation methodRequestMapping = method.getAnnotations().stream().filter(annotation -> requestMappers.stream().anyMatch(mapper -> mapper.equals(annotation.getName()))).findFirst()
        .orElse(null);
    List<Parameter> parameters = new ArrayList<>();

    // find uri
    String controllerPath = getPath(controllerRequestMapping);
    String methodPath = getPath(methodRequestMapping);
    String fullPath = (controllerPath + "/" + methodPath).replace("\\\\", "/").replace("//", "/");
    if (!fullPath.startsWith("/")) {
      fullPath = "/" + fullPath;
    }
    if (fullPath.endsWith("/")) {
      fullPath = fullPath.substring(0, fullPath.length() - 1);
    }
    String path;
    if (fullPath.contains("?")) {
      String[] pathSplit = fullPath.split("\\?");
      path = pathSplit[0];
      if (pathSplit.length >= 2) {
        String paramsSplit = pathSplit[1];

        String[] params = paramsSplit.split("&");
        for (String param : params) {
          String[] paramSplit = param.split("=");
          ParameterVariable parameter = new ParameterVariable();
          parameter.setIn(ParameterPlacing.QUERY);
          parameter.setName(paramSplit[0]);
          parameter.setRequired(true);
          parameter.setType(SchemaType.ANY);
          if (paramSplit.length > 1) {
            parameter.setDefaultValue(paramSplit[1]);
            parameter.setType(SchemaType.ENUM);
            List<String> enums = new ArrayList<>();
            enums.add(paramSplit[1]);
            parameter.setEnums(enums);
          }
          parameters.add(parameter);
        }
      }
    }
    else {
      path = fullPath;
    }

    // Ignore regex in path parameters
    path = path.replaceAll("\\{(.+)(:.*?)}", "{$1}");
    String finalPath = path;

    // find methods
    Set<String> methods = new HashSet<>();
    methods.addAll(getMethods(controllerRequestMapping));
    methods.addAll(getMethods(methodRequestMapping));
    if (methods.isEmpty()) { //use default
      methods.add("get");
    }
    methods.forEach(requestMethod -> Logger.get().logEndpoint(requestMethod, finalPath));

    Set<String> produces = new HashSet<>();
    produces.addAll(Arrays.asList(defaultProduces));
    produces.addAll(getProduces(controllerRequestMapping));
    produces.addAll(getProduces(methodRequestMapping));

    Set<String> consumes = new HashSet<>();
    consumes.addAll(Arrays.asList(defaultConsumes));
    consumes.addAll(getCondumes(controllerRequestMapping));
    consumes.addAll(getCondumes(methodRequestMapping));

    for (ReflectParameter parameter : method.getParameters()) {
      if (parameter.getType() != null && parameter.getType().getClassType() != null) {
        RequestParser parser = null;
        for (RequestParser requestParser : requestParsers) {
          if (requestParser.getClassName().equals(parameter.getType().getClassType().getName()) || requestParser.getClassName().equals(parameter.getType().getClassType().getSimpleName())) {
            parser = requestParser;
            break;
          }
        }
        if (parser != null) {
          List<Parameter> params = parser.parse(parameter, controller, method, schemaCollector);
          parameters.addAll(params);
          continue;
        }
      }

      String name = parameter.getName();
      Schema schema = schemaCollector.collect(parameter.getType());
      String description = null;
      for (ReflectDescriptionTag tag : method.getDescription().getTags("param")) {
        if (name.equals(tag.getKeyword())) {
          description = tag.getDescription();
          break;
        }
      }

      if (parameter.hasAnnotation(TYPE_REQUEST_BODY)) {
        ParameterBody bodyParam = new ParameterBody();
        bodyParam.setSchema(schema);
        bodyParam.setName(name);
        bodyParam.setDescription(description);
        bodyParam.setIn(ParameterPlacing.BODY);
        parameters.add(bodyParam);
      }
      else if (parameter.hasAnnotation(TYPE_REQUEST_PARAM)) {
        ReflectAnnotation annotation = parameter.getAnnotation(TYPE_REQUEST_PARAM);
        ParameterVariable param = new ParameterVariable();
        param.setIn(ParameterPlacing.QUERY);

        if (annotation.has("value")) {
          name = annotation.getString("value");
        }
        else if (annotation.has("name")) {
          name = annotation.getString("name");
        }
        param.setName(name);
        param.setDescription(description);
        param.setRequired(annotation.getBoolean("required"));
        param.setDefaultValue(annotation.getObject("defaultValue"));
        param.setType(schema != null ? schema.getType() : null);
        parameters.add(param);
      }
      else if (parameter.hasAnnotation(TYPE_PATH_VARIABLE)) {
        ReflectAnnotation annotation = parameter.getAnnotation(TYPE_PATH_VARIABLE);
        ParameterVariable param = new ParameterVariable();
        param.setIn(ParameterPlacing.PATH);

        if (annotation.has("value")) {
          name = annotation.getString("value");
        }
        else if (annotation.has("name")) {
          name = annotation.getString("name");
        }
        param.setName(name);
        param.setDescription(description);
        param.setRequired(true);
        param.setType(schema != null ? schema.getType() : null);
        parameters.add(param);
      }
    }

    // Find responses
    Map<String, Response> responses = new HashMap<>();
    if (method.getReturnType() != null && method.getReturnType().getClassType() != null && !method.getReturnType().getClassType().getSimpleName().equalsIgnoreCase("void")) {
      Response response = new Response();
      for (ReflectDescriptionTag tag : method.getDescription().getTags("return")) {
        response.setDescription(tag.getDescription());
        break;
      }

      // JSON View
      List<String> views = new ArrayList<>();
      method.getAnnotations().stream().filter(annotation -> annotation.getName().equals(JSON_VIEW)).forEach(annotation -> annotation.getList("value").forEach(value -> {
        if (value.getClazz() != null) {
          views.add(value.getClazz().getName());
        }
      }));

      Schema schema;
      if (views.isEmpty()) {
        schema = schemaCollector.collect(method.getReturnType());
      }
      else if (views.size() == 1) {
        schema = schemaCollector.collect(method.getReturnType(), views.get(0));
      }
      else {
        SchemaObject schemaObject = new SchemaObject();
        schemaObject.setAnyOf(new ArrayList<>());
        for (String view : views) {
          Schema subSchema = schemaCollector.collect(method.getReturnType(), view);
          schemaObject.getAnyOf().add(subSchema);
        }
        schema = schemaObject;
      }
      response.setSchema(schema);
      responses.put("default", response);
    }

    // collect response codes
    List<ReflectDescriptionTag> responseTags = method.getDescription().getTags("response");
    responseTags.forEach(tag -> {
      String responseCode = tag.getKeyword();
      Response response = new Response();
      response.setDescription(tag.getDescription());
      responses.put(responseCode, response);
    });

    // create builders
    List<PathBuilder> pathBuilders = new ArrayList<>();
    for (String requestMethod : methods) {
      PathBuilder builder = new PathBuilder();
      builder.path(path);
      builder.requestMethod(requestMethod);
      builder.component(controller);
      builder.method(method);
      builder.description(method.getDescription().getText());
      builder.operationId(method.getSimpleName());
      builder.parameters(parameters);
      builder.responses(responses);
      builder.consumes(new ArrayList<>(consumes));
      builder.produces(new ArrayList<>(produces));

      pathBuilders.add(builder);
    }
    return pathBuilders;
  }

  private String getPath(ReflectAnnotation requestMapping) {
    String path = "";
    if (requestMapping != null) {
      if (requestMapping.has("value")) {
        if (requestMapping.getList("value") != null) {
          for (ReflectAnnotationValue p : requestMapping.getList("value")) {
            path = p.getString();
          }
        }
        else {
          path = requestMapping.getString("value");
        }
      }
      else if (requestMapping.has("path")) {
        if (requestMapping.getList("path") != null) {
          for (ReflectAnnotationValue p : requestMapping.getList("path")) {
            path = p.getString();
          }
        }
        else {
          path = requestMapping.getString("path");
        }
      }
    }
    return path;
  }

  private Set<String> getMethods(ReflectAnnotation requestMapping) {
    Set<String> methodSet = new HashSet<>();
    if (requestMapping != null) {

      if (requestMapping.getName().equals(Types.GET_MAPPING.getClassName())) {
        methodSet.add("get");
      }
      else if (requestMapping.getName().equals(Types.DELETE_MAPPING.getClassName())) {
        methodSet.add("delete");
      }
      else if (requestMapping.getName().equals(Types.POST_MAPPING.getClassName())) {
        methodSet.add("post");
      }
      else if (requestMapping.getName().equals(Types.PUT_MAPPING.getClassName())) {
        methodSet.add("put");
      }
      else if (requestMapping.getName().equals(Types.PATCH_MAPPING.getClassName())) {
        methodSet.add("patch");
      }

      List<ReflectAnnotationValue> methods = requestMapping.getList("method");
      if (methods != null) {
        for (ReflectAnnotationValue method : methods) {
          if (method.getString().startsWith("org.springframework.web.bind.annotation.RequestMethod.")) {
            methodSet.add(method.getString().substring("org.springframework.web.bind.annotation.RequestMethod.".length()).toLowerCase());
          }
        }
      }
    }
    return methodSet;
  }

  private Set<String> getProduces(ReflectAnnotation requestMapping) {
    Set<String> produces = new HashSet<>();
    if (requestMapping != null) {
      if (requestMapping.has("produces")) {
        List<ReflectAnnotationValue> mimes = requestMapping.getList("produces");
        if (mimes != null) {
          for (ReflectAnnotationValue mime : mimes) {
            produces.add(mime.getString());
          }
        }
      }
    }
    return produces;
  }

  private Set<String> getCondumes(ReflectAnnotation requestMapping) {
    Set<String> produces = new HashSet<>();
    if (requestMapping != null) {
      if (requestMapping.has("consumes")) {
        List<ReflectAnnotationValue> mimes = requestMapping.getList("consumes");
        if (mimes != null) {
          for (ReflectAnnotationValue mime : mimes) {
            produces.add(mime.getString());
          }
        }
      }
    }
    return produces;
  }

}
