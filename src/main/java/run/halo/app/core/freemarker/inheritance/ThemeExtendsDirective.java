package run.halo.app.core.freemarker.inheritance;

import freemarker.core.Environment;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import kr.pe.kwonnam.freemarker.inheritance.ExtendsDirective;
import org.springframework.stereotype.Component;

/**
 * @author LIlGG
 * @date 2021/3/4
 */
@Component
public class ThemeExtendsDirective extends ExtendsDirective {
    private static final Pattern THEME_TEMPLATE_PATH_PATTERN = Pattern.compile("^themes/.*?/");

    @Override
    @SuppressWarnings("unchecked")
    public void execute(Environment env, Map params, TemplateModel[] loopVars,
        TemplateDirectiveBody body) throws TemplateException, IOException {
        String currTemplateName = getTemplateRelativePath(env);
        String name = ((SimpleScalar) params.get("name")).getAsString();

        String includeTemplateName = env.rootBasedToAbsoluteTemplateName(
            env.toFullTemplateName(currTemplateName, name)
        );
        params.put("name", new SimpleScalar(includeTemplateName));

        super.execute(env, params, loopVars, body);
    }

    private String getTemplateRelativePath(Environment env) {
        String templateName = env.getCurrentTemplate().getName();

        Matcher matcher = THEME_TEMPLATE_PATH_PATTERN.matcher(templateName);
        return matcher.find()
            ? matcher.group()
            : "";
    }
}
