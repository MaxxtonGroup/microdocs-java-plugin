package com.maxxton.microdocs.core.builder;

import java.util.ArrayList;
import java.util.List;

import com.maxxton.microdocs.core.domain.schema.SchemaMapping;
import com.maxxton.microdocs.core.domain.schema.SchemaMappings;

/**
 * @author s.hermans
 */
public class SchemaMappingsBuilder implements Builder<SchemaMappings> {

  private SchemaMappings mappings;

  private void init() {
    if (mappings == null) {
      mappings = new SchemaMappings();
    }
  }

  private void initJson() {
    init();
    if (mappings.getJson() == null) {
      mappings.setJson(new SchemaMapping());
    }
  }

  private void initRelational() {
    init();
    if (mappings.getRelational() == null) {
      mappings.setRelational(new SchemaMapping());
    }
  }

  private void initClient() {
    init();
    if (mappings.getClient() == null) {
      mappings.setClient(new SchemaMapping());
    }
  }

  public SchemaMappingsBuilder jsonIgnore(boolean ignore) {
    initJson();
    mappings.getJson().setIgnore(ignore);
    return this;
  }

  public SchemaMappingsBuilder jsonName(String name) {
    initJson();
    mappings.getJson().setName(name);
    return this;
  }

  public SchemaMappingsBuilder view(String name) {
    initJson();
    if (mappings.getJson().getViews() == null) {
      mappings.getJson().setViews(new ArrayList<>());
    }
    mappings.getJson().getViews().add(name);
    return this;
  }

  public SchemaMappingsBuilder jsonPrimary(boolean primary) {
    initJson();
    mappings.getJson().setPrimary(primary);
    return this;
  }

  public SchemaMappingsBuilder jsonTables(List<String> tables) {
    initJson();
    mappings.getJson().setTables(tables);
    return this;
  }

  public SchemaMappingsBuilder relationalIgnore(boolean ignore) {
    initRelational();
    mappings.getRelational().setIgnore(ignore);
    return this;
  }

  public SchemaMappingsBuilder relationalName(String name) {
    initRelational();
    mappings.getRelational().setName(name);
    return this;
  }

  public SchemaMappingsBuilder relationalPrimary(boolean primary) {
    initRelational();
    mappings.getRelational().setPrimary(primary);
    return this;
  }

  public SchemaMappingsBuilder relationalTables(List<String> tables) {
    initRelational();
    mappings.getRelational().setTables(tables);
    return this;
  }

  public SchemaMappingsBuilder clientIgnore(boolean ignore) {
    initClient();
    mappings.getClient().setIgnore(ignore);
    return this;
  }

  public SchemaMappingsBuilder clientName(String name) {
    initClient();
    mappings.getClient().setName(name);
    return this;
  }

  @Override
  public SchemaMappings build() {
    return mappings;
  }
}
