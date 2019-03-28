package cc.ryanc.halo.model.freemarker.tag;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * Freemarker custom tag of post.
 *
 * @author : RYAN0UP
 * @date : 2018/4/26
 */
@Component
public class PostTagDirective implements TemplateDirectiveModel {

    private static final String METHOD_KEY = "method";

    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
        // TODO Complete article tag directive.
    }

}
