package cc.ryanc.halo.model.freemarker.method;

import cn.hutool.core.util.RandomUtil;
import freemarker.template.SimpleNumber;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author : RYAN0UP
 * @date : 2018/12/21
 */
@Component
public class RandomMethod implements TemplateMethodModelEx {

    /**
     * 生成随机数
     *
     * @param arguments 参数
     * @return Object
     * @throws TemplateModelException TemplateModelException
     */
    @Override
    public Object exec(List arguments) throws TemplateModelException {
        SimpleNumber argOne = (SimpleNumber) arguments.get(0);
        SimpleNumber argTwo = (SimpleNumber) arguments.get(1);
        int start = argOne.getAsNumber().intValue();
        int end = argTwo.getAsNumber().intValue();
        return RandomUtil.randomInt(start, end);
    }
}
