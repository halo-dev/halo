package run.halo.app.handler.migrate.utils;

import java.lang.annotation.*;

/**
 * 该注解用于定义两个对象之间的属性映射关系
 *
 * @author guqing
 * @date 2020-1-19 13:51
 */
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PropertyMappingTo {
    /**
     * value对应的是目标对象的属性名称
     *
     * @return 返回源对象属性对应的目标对象的属性名
     */
    String value() default "";
}
