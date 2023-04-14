package run.halo.app.plugin;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.service.AttachmentService;
import run.halo.app.extension.DefaultSchemeManager;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.ExternalUrlSupplier;

/**
 * <p>This {@link SharedApplicationContextHolder} class is used to hold a singleton instance of
 * {@link SharedApplicationContext}.</p>
 * <p>If sharedApplicationContext cache is null when calling the {@link #getInstance()} method,
 * then it will call {@link #createSharedApplicationContext()} to create and cache it. Otherwise,
 * it will be obtained directly.</p>
 * <p>It is thread safe.</p>
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class SharedApplicationContextHolder {

    private final ApplicationContext rootApplicationContext;
    private volatile SharedApplicationContext sharedApplicationContext;

    public SharedApplicationContextHolder(ApplicationContext applicationContext) {
        this.rootApplicationContext = applicationContext;
    }

    /**
     * Get singleton instance of {@link SharedApplicationContext}.
     *
     * @return a singleton instance of {@link SharedApplicationContext}.
     */
    public SharedApplicationContext getInstance() {
        if (this.sharedApplicationContext == null) {
            synchronized (SharedApplicationContextHolder.class) {
                if (this.sharedApplicationContext == null) {
                    this.sharedApplicationContext = createSharedApplicationContext();
                }
            }
        }
        return this.sharedApplicationContext;
    }

    SharedApplicationContext createSharedApplicationContext() {
        // TODO Optimize creation timing
        SharedApplicationContext sharedApplicationContext = new SharedApplicationContext();
        sharedApplicationContext.refresh();

        DefaultListableBeanFactory beanFactory =
            (DefaultListableBeanFactory) sharedApplicationContext.getBeanFactory();

        // register shared object here
        var extensionClient = rootApplicationContext.getBean(ExtensionClient.class);
        var reactiveExtensionClient = rootApplicationContext.getBean(ReactiveExtensionClient.class);
        beanFactory.registerSingleton("extensionClient", extensionClient);
        beanFactory.registerSingleton("reactiveExtensionClient", reactiveExtensionClient);

        DefaultSchemeManager defaultSchemeManager =
            rootApplicationContext.getBean(DefaultSchemeManager.class);
        beanFactory.registerSingleton("schemeManager", defaultSchemeManager);
        beanFactory.registerSingleton("externalUrlSupplier",
            rootApplicationContext.getBean(ExternalUrlSupplier.class));
        beanFactory.registerSingleton("serverSecurityContextRepository",
            rootApplicationContext.getBean(ServerSecurityContextRepository.class));
        beanFactory.registerSingleton("attachmentService",
            rootApplicationContext.getBean(AttachmentService.class));
        // TODO add more shared instance here

        return sharedApplicationContext;
    }
}
