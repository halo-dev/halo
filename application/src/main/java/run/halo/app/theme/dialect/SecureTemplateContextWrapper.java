package run.halo.app.theme.dialect;

import org.thymeleaf.context.Contexts;
import org.thymeleaf.context.ITemplateContext;

/**
 * Wrap the delegate template context to a secure template context according to whether it is a
 * WebContext.
 *
 * @author guqing
 * @since 2.20.4
 */
public class SecureTemplateContextWrapper {

    /**
     * Wrap the delegate template context to a secure template context.
     */
    static SecureTemplateContext wrap(ITemplateContext delegate) {
        if (Contexts.isWebContext(delegate)) {
            return new SecureTemplateWebContext(delegate);
        }
        return new SecureTemplateContext(delegate);
    }
}
