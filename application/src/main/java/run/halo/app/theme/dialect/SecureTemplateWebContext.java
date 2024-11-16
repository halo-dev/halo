package run.halo.app.theme.dialect;

import org.springframework.context.ApplicationContext;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.web.IWebExchange;

/**
 * Secure template web context.
 * <p>It's used to prevent some dangerous variables such as {@link ApplicationContext} from being
 * accessed.
 *
 * @author guqing
 * @see SecureTemplateContext
 * @since 2.20.4
 */
class SecureTemplateWebContext extends SecureTemplateContext implements IWebContext {
    private final IWebContext delegate;

    /**
     * The delegate must be an instance of IWebContext to create a SecureTemplateWebContext.
     */
    public SecureTemplateWebContext(ITemplateContext delegate) {
        super(delegate);
        if (delegate instanceof IWebContext webContext) {
            this.delegate = webContext;
        } else {
            throw new IllegalArgumentException("The delegate must be an instance of IWebContext");
        }
    }

    @Override
    public IWebExchange getExchange() {
        return delegate.getExchange();
    }
}
