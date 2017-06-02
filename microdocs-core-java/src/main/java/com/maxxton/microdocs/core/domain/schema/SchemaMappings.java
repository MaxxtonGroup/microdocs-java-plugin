package com.maxxton.microdocs.core.domain.schema;

/**
 * @author Steven Hermans
 */
public class SchemaMappings {

    private SchemaMapping json;
    private SchemaMapping relational;
    private SchemaMapping downstreamCheck;

    public SchemaMapping getJson() {
        return json;
    }

    public void setJson(SchemaMapping json) {
        this.json = json;
    }

    public SchemaMapping getRelational() {
        return relational;
    }

    public void setRelational(SchemaMapping relational) {
        this.relational = relational;
    }

    public SchemaMapping getDownstreamCheck() {
        return downstreamCheck;
    }

    public void setDownstreamCheck(SchemaMapping downstreamCheck) {
        this.downstreamCheck = downstreamCheck;
    }
}
