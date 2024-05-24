package run.halo.app.plugin;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import run.halo.app.content.PostContentService;
import run.halo.app.core.extension.service.AttachmentService;
import run.halo.app.extension.DefaultSchemeManager;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.BackupRootGetter;
import run.halo.app.infra.ExternalLinkProcessor;
import run.halo.app.infra.ExternalUrlSupplier;
import run.halo.app.notification.NotificationCenter;
import run.halo.app.notification.NotificationReasonEmitter;

/**
 * Utility for creating shared application context.
 *
 * @author guqing
 * @author johnniang
 * @since 2.12.0
 */
public enum SharedApplicationContextFactory {
    ;

    public static ApplicationContext create(ApplicationContext rootContext) {
        // TODO Optimize creation timing
        var sharedContext = new GenericApplicationContext();
        sharedContext.registerShutdownHook();

        var beanFactory = sharedContext.getBeanFactory();

        // register shared object here
        var extensionClient = rootContext.getBean(ExtensionClient.class);
        var reactiveExtensionClient = rootContext.getBean(ReactiveExtensionClient.class);
        beanFactory.registerSingleton("extensionClient", extensionClient);
        beanFactory.registerSingleton("reactiveExtensionClient", reactiveExtensionClient);

        DefaultSchemeManager defaultSchemeManager =
            rootContext.getBean(DefaultSchemeManager.class);
        beanFactory.registerSingleton("schemeManager", defaultSchemeManager);
        beanFactory.registerSingleton("externalUrlSupplier",
            rootContext.getBean(ExternalUrlSupplier.class));
        beanFactory.registerSingleton("serverSecurityContextRepository",
            rootContext.getBean(ServerSecurityContextRepository.class));
        beanFactory.registerSingleton("attachmentService",
            rootContext.getBean(AttachmentService.class));
        beanFactory.registerSingleton("backupRootGetter",
            rootContext.getBean(BackupRootGetter.class));
        beanFactory.registerSingleton("notificationReasonEmitter",
            rootContext.getBean(NotificationReasonEmitter.class));
        beanFactory.registerSingleton("notificationCenter",
            rootContext.getBean(NotificationCenter.class));
        beanFactory.registerSingleton("externalLinkProcessor",
            rootContext.getBean(ExternalLinkProcessor.class));
        beanFactory.registerSingleton("postContentService",
            rootContext.getBean(PostContentService.class));
        // TODO add more shared instance here

        sharedContext.refresh();
        return sharedContext;
    }
}
