package com.suchtool.nicetool.util.spring;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.lang.reflect.Method;
import java.util.Map;

public class SpringBootUtil {
    /**
     * 解析运行类的包名
     */
    public static String parseRunClassPackage() {
        Map<String, Object> runClassBeanMap = ApplicationContextHolder.getContext()
                .getBeansWithAnnotation(SpringBootApplication.class);
        Object runObject = runClassBeanMap.entrySet().iterator().next().getValue();
        return AopUtil.getTargetClass(runObject).getPackage().getName();
    }

    /**
     * 判断方法是否在运行类的包下边
     */
    public static boolean inRunClassPackage(Method method) {
        return inRunClassPackage(method.getDeclaringClass());
    }

    /**
     * 判断类是否在运行类的包下边
     */
    public static boolean inRunClassPackage(Class<?> declaringClass) {
        String runPackageName = SpringBootUtil.parseRunClassPackage();
        return declaringClass.getName().startsWith(runPackageName.trim());
    }
}
