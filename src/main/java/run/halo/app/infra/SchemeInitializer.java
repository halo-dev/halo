package run.halo.app.infra;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.Category;
import run.halo.app.core.extension.Comment;
import run.halo.app.core.extension.Counter;
import run.halo.app.core.extension.Menu;
import run.halo.app.core.extension.MenuItem;
import run.halo.app.core.extension.Plugin;
import run.halo.app.core.extension.Post;
import run.halo.app.core.extension.Reply;
import run.halo.app.core.extension.ReverseProxy;
import run.halo.app.core.extension.Role;
import run.halo.app.core.extension.RoleBinding;
import run.halo.app.core.extension.Setting;
import run.halo.app.core.extension.SinglePage;
import run.halo.app.core.extension.Snapshot;
import run.halo.app.core.extension.Tag;
import run.halo.app.core.extension.Theme;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.attachment.Group;
import run.halo.app.core.extension.attachment.Policy;
import run.halo.app.core.extension.attachment.PolicyTemplate;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.SchemeManager;
import run.halo.app.security.authentication.pat.PersonalAccessToken;

@Component
public class SchemeInitializer implements ApplicationListener<ApplicationStartedEvent> {

    private final SchemeManager schemeManager;

    public SchemeInitializer(SchemeManager schemeManager) {
        this.schemeManager = schemeManager;
    }

    @Override
    public void onApplicationEvent(@NonNull ApplicationStartedEvent event) {
        schemeManager.register(Role.class);
        schemeManager.register(PersonalAccessToken.class);
        schemeManager.register(Plugin.class);
        schemeManager.register(RoleBinding.class);
        schemeManager.register(User.class);
        schemeManager.register(ReverseProxy.class);
        schemeManager.register(Setting.class);
        schemeManager.register(ConfigMap.class);
        schemeManager.register(Theme.class);
        schemeManager.register(Menu.class);
        schemeManager.register(MenuItem.class);
        schemeManager.register(Post.class);
        schemeManager.register(Category.class);
        schemeManager.register(Tag.class);
        schemeManager.register(Snapshot.class);
        schemeManager.register(Comment.class);
        schemeManager.register(Reply.class);
        schemeManager.register(SinglePage.class);
        // storage.halo.run
        schemeManager.register(Group.class);
        schemeManager.register(Policy.class);
        schemeManager.register(Attachment.class);
        schemeManager.register(PolicyTemplate.class);
        // metrics.halo.run
        schemeManager.register(Counter.class);
    }
}
