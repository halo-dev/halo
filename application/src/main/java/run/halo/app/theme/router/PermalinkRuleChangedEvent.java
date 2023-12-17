package run.halo.app.theme.router;

import org.springframework.context.ApplicationEvent;
import run.halo.app.theme.DefaultTemplateEnum;

public class PermalinkRuleChangedEvent extends ApplicationEvent {
    private final DefaultTemplateEnum template;
    private final String oldRule;
    private final String rule;

    public PermalinkRuleChangedEvent(Object source, DefaultTemplateEnum template,
        String oldRule, String rule) {
        super(source);
        this.template = template;
        this.oldRule = oldRule;
        this.rule = rule;
    }

    public DefaultTemplateEnum getTemplate() {
        return template;
    }

    public String getOldRule() {
        return oldRule;
    }

    public String getRule() {
        return rule;
    }
}
