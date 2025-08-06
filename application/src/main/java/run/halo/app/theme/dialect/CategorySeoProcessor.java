package run.halo.app.theme.dialect;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import reactor.core.publisher.Mono;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.finders.vo.CategoryVo;
import run.halo.app.theme.router.ModelConst;

/**
 * Processor for category page SEO.
 *
 * @author ryanwang
 */
@Component
@AllArgsConstructor
public class CategorySeoProcessor implements TemplateHeadProcessor {
    private static final String CATEGORY_MODEL_NAME_VARIABLE = "category";

    @Override
    public Mono<Void> process(ITemplateContext context, IModel model,
        IElementModelStructureHandler structureHandler) {
        if (!isCategoryTemplate(context)) {
            return Mono.empty();
        }

        Mono<CategoryVo> categoryMono = Mono.justOrEmpty(
            (CategoryVo) context.getVariable(CATEGORY_MODEL_NAME_VARIABLE));

        return categoryMono.map(category -> {
            IModelFactory modelFactory = context.getModelFactory();
            var description = category.getSpec().getDescription();
            if (StringUtils.isNotBlank(description)) {
                String descriptionMeta =
                    "<meta name=\"description\" content=\"" + description + "\" />\n";
                model.add(modelFactory.createText(descriptionMeta));
            }
            return model;
        }).then();
    }

    private boolean isCategoryTemplate(ITemplateContext context) {
        return DefaultTemplateEnum.CATEGORY.getValue()
            .equals(context.getVariable(ModelConst.TEMPLATE_ID));
    }
}
