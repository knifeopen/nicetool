package com.suchtool.nicetool.util.base;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * 属性工具
 */
public class PropertyUtil {
    /**
     * 获得值为null的属性名
     * @param obj 对象
     * @return 属性名字符串数组
     */
    public static String[] getNullPropertyNames(Object obj) {
        BeanWrapper src = new BeanWrapperImpl(obj);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();
 
        Set<String> emptyNames = new HashSet<>();
 
        for (PropertyDescriptor pd : pds) {
            //check if value of this property is null then add it to the collection
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null){
                emptyNames.add(pd.getName());
            }
        }
 
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }

    /**
     * 把对象中的 String 类型的空字段，转换为指定字符串
     *
     * @param obj 待转化对象
     * @param str 目标字符串
     */
    public static void replaceBlankString(Object obj, String str) {
        Field[] fields = obj.getClass().getDeclaredFields();
        if (fields == null || fields.length == 0) {
            return;
        }
        for (Field field : fields) {
            if ("String".equals(field.getType().getSimpleName())) {
                field.setAccessible(true);
                try {
                    String value = (String) field.get(obj);
                    if (!StringUtils.hasText(value)) {
                        field.set(obj, str);
                    }
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 判断是否所有字段都是null
     * @param o 对象
     * @return 是否所有的对象都是null
     */
    public static boolean allFieldAreNull(Object o) {
        Class<?> aClass = o.getClass();

        PropertyDescriptor[] beanProperties = ReflectUtils.getBeanProperties(aClass);
        for (PropertyDescriptor beanProperty : beanProperties) {
            Method readMethod = beanProperty.getReadMethod();
            try {
                Object value = readMethod.invoke(o);
                if (value != null) {
                    return false;
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        return true;
    }
}