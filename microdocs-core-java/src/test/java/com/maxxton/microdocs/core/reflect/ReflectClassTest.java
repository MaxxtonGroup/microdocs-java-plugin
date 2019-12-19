package com.maxxton.microdocs.core.reflect;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * @author Steven Hermans
 */
public class ReflectClassTest {

  @org.junit.Test
  public void testHasParent() {
    ReflectClass<ArrayList<?>> arrayListClass = new ReflectClass<>();
    arrayListClass.setSimpleName(ArrayList.class.getSimpleName());
    arrayListClass.setName(ArrayList.class.getCanonicalName());

    ReflectClass<List<?>> listClass = new ReflectClass<>();
    listClass.setSimpleName(List.class.getSimpleName());
    listClass.setName(List.class.getCanonicalName());
    ReflectGenericClass genericClass = new ReflectGenericClass();
    genericClass.setClassType(listClass);
    List<ReflectGenericClass> interfaces = new ArrayList<>();
    interfaces.add(genericClass);

    arrayListClass.setInterfaces(interfaces);

    boolean result = arrayListClass.hasParent(List.class.getCanonicalName());
    assertTrue(result);

  }

  @org.junit.Test
  public void testHasParentNotLoaded() {
    ReflectClass<ArrayList<?>> arrayListClass = new ReflectClass<>();
    arrayListClass.setSimpleName(ArrayList.class.getSimpleName());
    arrayListClass.setName(ArrayList.class.getCanonicalName());

    boolean result = arrayListClass.hasParent(List.class.getCanonicalName());
    assertTrue(result);

  }

}
