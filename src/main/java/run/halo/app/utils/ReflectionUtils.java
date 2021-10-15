package run.halo.app.utils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Reflection utilities.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-03-15
 */
public class ReflectionUtils {

    private ReflectionUtils() {
    }

    /**
     * Gets parameterized type.
     *
     * @param superType super type must not be null (super class or super interface)
     * @param genericTypes generic type array
     * @return parameterized type of the interface or null if it is mismatch
     */
    @Nullable
    public static ParameterizedType getParameterizedType(@NonNull Class<?> superType,
        Type... genericTypes) {
        Assert.notNull(superType, "Interface or super type must not be null");

        ParameterizedType currentType = null;

        for (Type genericType : genericTypes) {
            if (genericType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericType;
                if (parameterizedType.getRawType().getTypeName().equals(superType.getTypeName())) {
                    currentType = parameterizedType;
                    break;
                }
            }
        }

        return currentType;
    }

    /**
     * Gets parameterized type.
     *
     * @param interfaceType interface type must not be null
     * @param implementationClass implementation class of the interface must not be null
     * @return parameterized type of the interface or null if it is mismatch
     */
    @Nullable
    public static ParameterizedType getParameterizedType(@NonNull Class<?> interfaceType,
        Class<?> implementationClass) {
        Assert.notNull(interfaceType, "Interface type must not be null");
        Assert.isTrue(interfaceType.isInterface(), "The give type must be an interface");

        if (implementationClass == null) {
            // If the super class is Object parent then return null
            return null;
        }

        // Get parameterized type
        ParameterizedType currentType =
            getParameterizedType(interfaceType, implementationClass.getGenericInterfaces());

        if (currentType != null) {
            // return the current type
            return currentType;
        }

        Class<?> superclass = implementationClass.getSuperclass();

        return getParameterizedType(interfaceType, superclass);
    }

    /**
     * Gets parameterized type by super class.
     *
     * @param superClassType super class type must not be null
     * @param extensionClass extension class
     * @return parameterized type or null
     */
    @Nullable
    public static ParameterizedType getParameterizedTypeBySuperClass(
        @NonNull Class<?> superClassType, Class<?> extensionClass) {

        if (extensionClass == null) {
            return null;
        }

        return getParameterizedType(superClassType, extensionClass.getGenericSuperclass());
    }

    /**
     * Gets field value from Object.
     *
     * @param fieldName fieldName must not be null
     * @param object object must not be null.
     * @return value
     */
    public static Object getFieldValue(@NonNull String fieldName, @NonNull Object object) {
        Assert.notNull(fieldName, "FieldName must not be null");
        Assert.notNull(object, "Object type must not be null");
        Object value = null;
        try {
            String firstLetter = fieldName.substring(0, 1).toUpperCase();
            String getter = "get" + firstLetter + fieldName.substring(1);
            Method method = object.getClass().getMethod(getter);
            value = method.invoke(object);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }
}
