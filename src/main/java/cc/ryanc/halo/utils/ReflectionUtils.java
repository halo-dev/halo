package cc.ryanc.halo.utils;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Reflection utilities.
 *
 * @author johnniang
 */
public class ReflectionUtils {

    private ReflectionUtils() {
    }

    /**
     * Gets parameterized type.
     *
     * @param interfaceType interface type must not be null
     * @param genericTypes  generic type array
     * @return parameterized type of the interface or null if it is mismatch
     */
    @Nullable
    public static ParameterizedType getParameterizedType(@NonNull Class<?> interfaceType, Type... genericTypes) {
        Assert.notNull(interfaceType, "Interface type must not be null");

        ParameterizedType currentType = null;

        for (Type genericType : genericTypes) {
            if (genericType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericType;
                if (parameterizedType.getRawType().getTypeName().equals(interfaceType.getTypeName())) {
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
     * @param interfaceType       interface type must not be null
     * @param implementationClass implementation class of the interface must not be null
     * @return parameterized type of the interface or null if it is mismatch
     */
    @Nullable
    public static ParameterizedType getParameterizedType(@NonNull Class<?> interfaceType, Class<?> implementationClass) {
        Assert.notNull(interfaceType, "Interface type must not be null");

        if (implementationClass == null) {
            // If the super class is Object parent then return null
            return null;
        }

        // Get parameterized type
        ParameterizedType currentType = getParameterizedType(interfaceType, implementationClass.getGenericInterfaces());

        if (currentType != null) {
            // return the current type
            return currentType;
        }

        Class<?> superclass = implementationClass.getSuperclass();

        return getParameterizedType(interfaceType, superclass);
    }
}
