package cc.ryanc.halo.utils;

import org.springframework.util.CollectionUtils;
import org.springframework.validation.FieldError;

import javax.validation.ConstraintViolation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 有关字段验证的工具类
 *
 * @author johnniang
 */
public class ValidationUtils {

    private ValidationUtils() {
    }

    /**
     * 将字段验证错误转换为标准的map型，key:value = field:message
     *
     * @param constraintViolations constraint violations(contain error information)
     * @return 如果返回null则未出现错误
     */
    public static Map<String, String> mapWithValidError(Set<ConstraintViolation<Object>> constraintViolations) {
        Map<String, String> errMap = null;
        if (!CollectionUtils.isEmpty(constraintViolations)) {
            // if not empty
            errMap = new HashMap<>(4);
            for (ConstraintViolation<Object> constraintViolation : constraintViolations) {
                errMap.put(constraintViolation.getPropertyPath().toString(), constraintViolation.getMessage());
            }
        }
        return errMap;
    }

    public static String stringWithValidError(Set<ConstraintViolation<?>> violations, boolean allError) {
        StringBuilder errString = new StringBuilder();
        if (!CollectionUtils.isEmpty(violations)) {
            for (ConstraintViolation<?> violation : violations) {
                if (errString.length() > 0) {
                    errString.append(",");
                }
                errString.append(violation.getMessage());
                if (!allError && errString.length() > 0) {
                    return errString.toString();
                }
            }
        }
        return errString.toString();
    }

    /**
     * 将字段验证错误转换为标准的map型，key:value = field:message
     *
     * @param fieldErrors 字段错误组
     * @return 如果返回null，则表示未出现错误
     */
    public static Map<String, String> mapWithFieldError(List<FieldError> fieldErrors) {
        Map<String, String> errMap = null;

        if (!CollectionUtils.isEmpty(fieldErrors)) {
            // 如果不为空
            errMap = new HashMap<>(4);
            for (FieldError fieldError : fieldErrors) {
                errMap.put(fieldError.getField(), fieldError.getDefaultMessage());
            }
        }

        return errMap;
    }

    public static String stringWithFieldError(List<FieldError> fieldErrors, boolean allError) {
        StringBuilder errString = new StringBuilder();
        if (!CollectionUtils.isEmpty(fieldErrors)) {
            // 如果不为空
            for (FieldError fieldError : fieldErrors) {
                if (errString.length() > 0) {
                    errString.append(",");
                }
                errString.append(fieldError.getField()).append(":").append(fieldError.getDefaultMessage());
                if (!allError && errString.length() > 0) {
                    return errString.toString();
                }
            }
        }

        return errString.toString();
    }
}