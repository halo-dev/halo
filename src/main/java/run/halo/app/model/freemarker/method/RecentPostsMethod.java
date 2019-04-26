package run.halo.app.model.freemarker.method;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author ryanwang
 * @date : 2018/12/31
 */
@Component
public class RecentPostsMethod implements TemplateMethodModelEx {
    @Override
    public Object exec(List arguments) throws TemplateModelException {
        // TODO Complete recent post method.
        return null;
    }
}
