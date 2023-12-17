package run.halo.app.extension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * GVK is an annotation to specific metadata of Extension.
 *
 * @author johnniang
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface GVK {

    /**
     * @return group name of Extension.
     */
    String group();

    /**
     * @return version name of Extension.
     */
    String version();

    /**
     * @return kind name of Extension.
     */
    String kind();

    /**
     * @return plural name of Extension.
     */
    String plural();

    /**
     * @return singular name of Extension.
     */
    String singular();

}
