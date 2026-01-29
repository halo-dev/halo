package run.halo.app.security.switchuser;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.test.context.support.WithSecurityContext;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@WithSecurityContext(factory = SwitchUserSecurityContextFactory.class)
@interface WithSwitchUser {

    String username();

    String targetUsername();

    String[] roles() default {};

    String[] targetRoles() default {};

}
