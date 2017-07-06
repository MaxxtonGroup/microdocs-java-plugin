package com.maxxton.microdocs.core.reflect;

import java.util.HashMap;
import java.util.List;
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

    public List<ReflectAnnotationValue> getList(String property) {
        ReflectAnnotationValue value = get(property);
        if(value != null){
            return value.getList();
        }
        return null;
    }

    public ReflectAnnotationValue getChild(String property) {
        ReflectAnnotationValue value = get(property);
        if(value != null){
            return value.getChild();
        }
        return null;
    }

    public Object getObject(String property) {
        ReflectAnnotationValue value = get(property);
        if(value != null){
            return value.getObject();
        }
        return null;
    }

    public String getString(String property) {
        ReflectAnnotationValue value = get(property);
        if(value != null){
            return value.getString();
        }
        return null;
    }

    public Integer getInt(String property) {
        ReflectAnnotationValue value = get(property);
        if(value != null){
            return value.getInt();
        }
        return null;
    }

    public Double getDouble(String property) {
        ReflectAnnotationValue value = get(property);
        if(value != null){
            return value.getDouble();
        }
        return null;
    }

    public Boolean getBoolean(String property) {
        ReflectAnnotationValue value = get(property);
        if(value != null){
            return value.getBoolean();
        }
        return false;
    }

    public ReflectClass getClazz(String property) {
        ReflectAnnotationValue value = get(property);
        if(value != null){
            return value.getClazz();
        }
        return null;
    }
}
