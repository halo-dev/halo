package run.halo.app.core.freemarker.tag;

import cn.hutool.core.util.PageUtil;
import cn.hutool.core.util.RandomUtil;
import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import java.io.IOException;
import java.util.Map;
import org.springframework.stereotype.Component;
import run.halo.app.model.support.HaloConst;

/**
 * Freemarker custom tag of tools.
 *
 * @author ryanwang
 * @date 2020-01-17
 */
@Component
public class ToolTagDirective implements TemplateDirectiveModel {

    public ToolTagDirective(Configuration configuration) {
        configuration.setSharedVariable("toolTag", this);
    }

    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars,
        TemplateDirectiveBody body) throws TemplateException, IOException {
        final DefaultObjectWrapperBuilder builder =
            new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_25);

        if (params.containsKey(HaloConst.METHOD_KEY)) {
            String method = params.get(HaloConst.METHOD_KEY).toString();
            switch (method) {
                case "rainbowPage":
                    int page = Integer.parseInt(params.get("page").toString());
                    int total = Integer.parseInt(params.get("total").toString());
                    int display = Integer.parseInt(params.get("display").toString());
                    env.setVariable("numbers",
                        builder.build().wrap(PageUtil.rainbow(page, total, display)));
                    break;
                case "random":
                    int min = Integer.parseInt(params.get("min").toString());
                    int max = Integer.parseInt(params.get("max").toString());
                    env.setVariable("number", builder.build().wrap(RandomUtil.randomInt(min, max)));
                    break;
                default:
                    break;
            }
        }
        body.render(env.getOut());
    }
}
