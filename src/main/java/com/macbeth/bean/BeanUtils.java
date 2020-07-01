package com.macbeth.bean;

import com.google.common.collect.Lists;
import com.macbeth.base.Copyable;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class BeanUtils {

    public static Class<?> COLLECTION = Collection.class;
    public static List<Class<?>> simpleObject = Lists.newArrayList();
    static {
        simpleObject.add(Byte.class);
        simpleObject.add(Short.class);
        simpleObject.add(Integer.class);
        simpleObject.add(Long.class);
        simpleObject.add(Double.class);
        simpleObject.add(Float.class);
        simpleObject.add(Boolean.class);
        simpleObject.add(Character.class);
        simpleObject.add(String.class);
    }

    /**
     * 拷贝源对象简单属性到目标对象
     * @param source
     * @param target
     * @param ignoreNull
     */
    public static void copyProperties(Object source, Object target, Boolean ignoreNull){

        Objects.requireNonNull(source, "源对象不能为null");
        Objects.requireNonNull(target, "目标对象不能为null");
        Class<?> targetClass = target.getClass();
        log.info("开始解析");

        getDeclaredFieldsStream(source).filter(field -> isSimpleObject(field.getType()))
                .forEach(field -> copySimpleProperties(source, target, ignoreNull, targetClass, field));
    }

    /**
     * 拷贝简单对象和复杂对象到目标对象
     * @param source
     * @param target
     * @param ignoreNull
     */
    public static void copyPropertiesWithComplexProperty(Object source, Object target, Boolean ignoreNull) {
        Objects.requireNonNull(source);
        Objects.requireNonNull(target);

        getDeclaredFieldsStream(source).forEach(field ->  {
            try {
                copyPropertiesWithComplex(source, target, ignoreNull, field);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        });
    }

    private static Stream<Field> getDeclaredFieldsStream(Object source) {
        Field[] fields = source.getClass().getDeclaredFields();
        String collect = Arrays.stream(fields).map(field -> field.getName()).collect(Collectors.joining(","));
        log.info("目标对象的字段{}",collect);
        return Arrays.stream(fields);
    }

    private static void copySimpleProperties(Object source, Object target, Boolean ignoreNull, Class<?> targetClass, Field field) {
        field.setAccessible(true);
        try {
            fillSimpleProperties(source, target, ignoreNull, targetClass, field);
        } catch (NoSuchFieldException e) {
            log.error("设置或获取字段{}的值或字段field，出错：{}", field.getName(), e.getMessage());
        } catch (IllegalAccessException e) {
            log.error("获取字段{}的值，出错：{}", field.getName(), e.getMessage());
        }
    }

    private static void fillSimpleProperties(Object source, Object target, Boolean ignoreNull, Class<?> targetClass, Field field) throws IllegalAccessException, NoSuchFieldException {
        Object value = field.get(source);
        if (ignoreNull && Objects.isNull(value)) return;
        fullValueIntoTarget(target, targetClass, field, value);
    }

    private static void fullValueIntoTarget(Object target, Class<?> targetClass, Field field, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field targetField = targetClass.getDeclaredField(field.getName());
        if (Objects.isNull(targetField)) return;
        targetField.setAccessible(true);
        targetField.set(target, value);
    }

    private static void copyPropertiesWithComplex(Object source, Object target, Boolean ignoreNull, Field field) throws IllegalAccessException, NoSuchFieldException, IOException, ClassNotFoundException {
        log.info("正在处理字段{}", field.getName());
        field.setAccessible(true);
        if (isSimpleObject(field.getType())) {
            fillSimpleProperties(source,target, ignoreNull, target.getClass(), field);
        } else if (isCollectionObject(field.getType())) {
            copyCollectionProperty(source, target, ignoreNull, field);
        } else if (isCopyAble(field.getType())) {
            fillCopyableToTarget(source, target, field);
        }
    }

    private static void fillCopyableToTarget(Object source, Object target, Field field) throws IllegalAccessException, IOException, ClassNotFoundException, NoSuchFieldException {
        Object value = field.get(source);
        Object valueClone = cloneCopyable(value);
        Field declaredField = target.getClass().getDeclaredField(field.getName());
        declaredField.setAccessible(true);
        declaredField.set(target, valueClone);
    }

    private static void copyCollectionProperty(Object source, Object target, Boolean ignoreNull, Field field) throws IllegalAccessException, NoSuchFieldException {
        Collection collection = (Collection) field.get(source);
        if (collection.size() <= 0) return;
        Iterator iterator = collection.iterator();
        for (;iterator.hasNext();) {
            Object value = iterator.next();
            fillCollectionPropertiesValue(target, ignoreNull, field, value);
        }
    }

    private static void fillCollectionPropertiesValue(Object target, Boolean ignoreNull, Field field, Object value) throws NoSuchFieldException, IllegalAccessException {
        if (ignoreNull && Objects.isNull(value)) return;
        Field declaredField = target.getClass().getDeclaredField(field.getName());
        declaredField.setAccessible(true);
        Collection targetCollection = (Collection) declaredField.get(target);

        if (isSimpleObject(value.getClass())) {
            targetCollection.add(value);
        } else if (isCopyAble(value.getClass()) && Objects.nonNull(value)) {
            fillCopyableValueToCollection(value, targetCollection);
        }
    }

    private static void fillCopyableValueToCollection(Object value, Collection targetCollection) {
        try {
            Object o = cloneCopyable(value);
            targetCollection.add(o);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static Object cloneCopyable(Object value) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(value);
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        return ois.readObject();
    }

    private static Boolean isSimpleObject(Class targetClass){
        Objects.requireNonNull(targetClass,"需要判断的对象不能为null");

        return simpleObject.contains(targetClass);
    }

    private static Boolean isCollectionObject(Class targetClass){
        Objects.requireNonNull(targetClass,"需要判断的对象不能为null");

        if (Objects.equals(COLLECTION, targetClass)) return true;
        Class<?>[] interfaces = targetClass.getInterfaces();
        if (interfaces.length <= 0) return false;
        return Arrays.stream(interfaces).map(i -> isCollectionObject(i)).findFirst().get();
    }

    private static Boolean isCopyAble(Class targetClass){
        Objects.requireNonNull(targetClass,"需要判断的对象不能为null");

        if (Copyable.class.equals(targetClass)) return true;
        Class[] interfaces = targetClass.getInterfaces();
        if (interfaces.length <= 0) return false;
        return Arrays.stream(interfaces).map(i -> isCopyAble(i)).findFirst().get();
    }
}
