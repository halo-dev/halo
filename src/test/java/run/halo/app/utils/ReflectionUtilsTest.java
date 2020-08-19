package run.halo.app.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import run.halo.app.model.params.BaseCommentParam;
import run.halo.app.model.params.JournalCommentParam;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Reflection utils test.
 *
 * @author johnniang
 * @date 19-4-25
 */
@Slf4j
class ReflectionUtilsTest {

    @Test
    void getBaseCommentParamParameterizedTypeTest() {
        Class<JournalCommentParam> paramClass = JournalCommentParam.class;

        log.debug(paramClass.getTypeName());
        log.debug(paramClass.getSuperclass().getTypeName());
        Type genericSuperclass = paramClass.getGenericSuperclass();
        log.debug(genericSuperclass.getTypeName());
        for (Type genericInterface : paramClass.getGenericInterfaces()) {
            log.debug(genericInterface.getTypeName());
            log.debug(genericInterface.getClass().toString());
        }

        Type[] genericInterfaces = paramClass.getSuperclass().getGenericInterfaces();
        for (Type genericInterface : genericInterfaces) {
            log.debug(genericInterface.getTypeName());
            log.debug(genericInterface.getClass().toString());
        }

        ParameterizedType parameterizedType = ReflectionUtils.getParameterizedTypeBySuperClass(BaseCommentParam.class, paramClass);

        log.debug(Objects.requireNonNull(parameterizedType).getTypeName());
        for (Type type : parameterizedType.getActualTypeArguments()) {
            log.debug(type.getTypeName());
        }


        //        ParameterizedType parameterizedType = ReflectionUtils.getParameterizedType(InputConverter.class, paramClass);
        //
        //        log.debug(parameterizedType.toString());
        //
        //        log.debug(parameterizedType.getActualTypeArguments()[0].toString());
        //
        //        parameterizedType = ReflectionUtils.getParameterizedType(BaseCommentParam.class, paramClass);
        //
        //        log.debug(parameterizedType.toString());
        //
        //        log.debug(parameterizedType.getActualTypeArguments()[0].toString());
    }
}