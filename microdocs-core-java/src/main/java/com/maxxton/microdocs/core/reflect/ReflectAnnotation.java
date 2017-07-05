package com.maxxton.microdocs.core.reflect;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Steven Hermans
 */
public class ReflectAnnotation extends ReflectDoc{

    private String packageName;
    private Map<String, ReflectAnnotationValue> properties = new HashMap();

    public Map<String, ReflectAnnotationValue> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, ReflectAnnotationValue> properties) {
        this.properties = properties;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public boolean has(String property){
        return properties.containsKey(property);
    }

    public ReflectAnnotationValue get(String property){
        return properties.get(property);
    }
}
