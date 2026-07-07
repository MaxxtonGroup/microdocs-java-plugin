package com.maxxton.microdocs.core.reflect;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Steven Hermans
 */
public class ReflectClassTest {

  @Test
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

  @Test
  public void testHasParentNotLoaded() {
    ReflectClass<ArrayList<?>> arrayListClass = new ReflectClass<>();
    arrayListClass.setSimpleName(ArrayList.class.getSimpleName());
    arrayListClass.setName(ArrayList.class.getCanonicalName());

    boolean result = arrayListClass.hasParent(List.class.getCanonicalName());
    assertTrue(result);

  }

}
