package run.halo.app.model.freemarker.method;

import freemarker.template.Configuration;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author ryanwang
 * @date : 2018/12/31
 */
@Component
@Deprecated
public class RecentCommentsMethod implements TemplateMethodModelEx {

    public RecentCommentsMethod(Configuration configuration) {
        configuration.setSharedVariable("recentCommentsMethod", this);
    }

    @Override
    public Object exec(List arguments) throws TemplateModelException {
        // TODO Complete recent comments method.
        return null;
    }
}
