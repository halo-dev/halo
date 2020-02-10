package run.halo.app.handler.migrate.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 关系映射工具类,用于使用@PropertyMappingTo注解映射源对象与目标对象属性之间的关系
 *
 * @author guqing
 * @date 2020-01-19 01:47
 */
public class RelationMapperUtils {
    private RelationMapperUtils() {
    }

    public static <SOURCE, TARGET> TARGET convertFrom(SOURCE source, Class<TARGET> targetClazz) {
        Map<String, Class> methodNameAndTypeMap = new HashMap<>(16);
        TARGET target;
        try {
            // 通过类的详情信息，创建目标对象 这一步等同于Hello target = new Hello()；
            target = targetClazz.getConstructor().newInstance();
            Field[] targetFields = targetClazz.getDeclaredFields();
            for (Field field : targetFields) {
                methodNameAndTypeMap.put(field.getName(), field.getType());
            }
        } catch (Exception e) {
            throw new RuntimeException("目标对象创建失败");
        }

        return propertyMapper(source, target, methodNameAndTypeMap);
    }

    private static <SOURCE, TARGET> TARGET propertyMapper(SOURCE source, TARGET target, Map<String, Class> methodNameAndTypeMap) {
        // 判断传入源数据是否为空，如果空，则抛自定义异常
        if (null == source) {
            throw new IllegalArgumentException("数据源不能为空");
        }
        // 获取源对象的类的详情信息
        Class<?> sourceClazz = source.getClass();
        // 获取源对象的所有属性
        Field[] sourceFields = sourceClazz.getDeclaredFields();

        // 循环取到源对象的单个属性
        for (Field sourceField : sourceFields) {
            boolean sourceFieldHasAnnotation = sourceField.isAnnotationPresent(PropertyMappingTo.class);

            String sourceFieldName = sourceField.getName();
            // 如果源对象没有添加注解则默认使用源对象属性名去寻找对于的目标对象属性名
            if (sourceFieldHasAnnotation) {
                PropertyMappingTo propertyMapping = sourceField.getAnnotation(PropertyMappingTo.class);
                String targetFieldName = propertyMapping.value();
                copyProperty(sourceFieldName, targetFieldName, source, target, sourceField.getType(), methodNameAndTypeMap);
            } else if (methodNameAndTypeMap.containsKey(sourceFieldName)) {
                // 如果源对象和目标对象的属性名相同则直接设置
                // 获取目标对象的属性名，将属性名首字母大写，拼接如：setUsername、setId的字符串
                // 判断源对象的属性名、属性类型是否和目标对象的属性名、属性类型一致
                copyProperty(sourceFieldName, sourceFieldName, source, target, sourceField.getType(), methodNameAndTypeMap);
            }
        }
        // 返回赋值得到的目标对象
        return target;
    }

    private static <SOURCE, TARGET> void copyProperty(String sourceFieldName, String targetFieldName,
                                                      SOURCE source, TARGET target, Class<?> sourceType,
                                                      Map<String, Class> methodNameAndTypeMap) {
        try {
            Class<?> sourceClazz = source.getClass();
            Class<?> targetClazz = target.getClass();
            // 获取源对象的属性名，将属性名首字母大写，拼接如：getUsername、getId的字符串
            String sourceGetMethodName = getterName(sourceFieldName);
            // 获得属性的get方法
            Method sourceMethod = sourceClazz.getMethod(sourceGetMethodName);
            // 调用get方法
            Object sourceFieldValue = sourceMethod.invoke(source);

            Class methodType = methodNameAndTypeMap.get(targetFieldName);
            // 通过字段名称得到set方法名称
            String targetSetMethodName = setterName(targetFieldName);
            if (methodType == sourceType) {
                // 调用方法，并将源对象get方法返回值作为参数传入
                Method targetSetMethod = targetClazz.getMethod(targetSetMethodName, sourceType);
                targetSetMethod.invoke(target, sourceFieldValue);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("转换失败，请检查属性类型是否匹配");
        }
    }

    /**
     * 通过属性名称得到对于的get或set方法
     *
     * @param fieldName  属性名称
     * @param methodType 方法类型可选值为:get或set
     * @return 返回对象的get或set方法的名称
     */
    private static String getMethodNameFromField(String fieldName, String methodType) {
        if (fieldName == null || fieldName.length() == 0) {
            return null;
        }

        /*
         * If the second char is upper, make 'get' + field name as getter name. For example, eBlog -> getBlog
         */
        if (fieldName.length() > 2) {
            String second = fieldName.substring(1, 2);
            if (second.equals(second.toUpperCase())) {
                return methodType + fieldName;
            }
        }

        // Common situation
        fieldName = methodType + fieldName.substring(0, 1).toUpperCase() +
                fieldName.substring(1);
        return fieldName;
    }

    private static String getterName(String fieldName) {
        return getMethodNameFromField(fieldName, "get");
    }

    private static String setterName(String fieldName) {
        return getMethodNameFromField(fieldName, "set");
    }
}
