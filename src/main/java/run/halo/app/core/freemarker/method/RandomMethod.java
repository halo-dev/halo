package run.halo.app.core.freemarker.method;

import cn.hutool.core.util.RandomUtil;
import freemarker.template.Configuration;
import freemarker.template.SimpleNumber;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Freemarker template random method.
 *
 * @author ryanwang
 * @date 2018-12-21
 */
@Component
public class RandomMethod implements TemplateMethodModelEx {

    /**
     * Constructor.
     *
     * @param configuration injected by spring.
     */
    public RandomMethod(Configuration configuration) {
        configuration.setSharedVariable("randomMethod", this);
    }

    /**
     * 生成随机数
     *
     * @param arguments 参数
     * @return Object
     * @throws TemplateModelException TemplateModelException
     */
    @Override
    public Object exec(List arguments) throws TemplateModelException {
        if (arguments.size() != 2) {
            throw new TemplateModelException("Wrong arguments! 2 arguments are needed");
        }
        SimpleNumber argOne = (SimpleNumber) arguments.get(0);
        SimpleNumber argTwo = (SimpleNumber) arguments.get(1);
        int start = argOne.getAsNumber().intValue();
        int end = argTwo.getAsNumber().intValue();
        return RandomUtil.randomInt(start, end);
    }
}
