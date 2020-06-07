package run.halo.app.annotation;


import java.lang.annotation.*;

/**
 * @author giveup
 * @description SensitiveConceal
 * @date 8:18 PM 26/5/2020
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface SensitiveConceal {
}
