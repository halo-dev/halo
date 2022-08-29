package run.halo.app.theme.router;

import org.springframework.context.ApplicationEvent;
import run.halo.app.theme.DefaultTemplateEnum;

public class PermalinkRuleChangedEvent extends ApplicationEvent {
    private final DefaultTemplateEnum template;
    private final String rule;

    public PermalinkRuleChangedEvent(Object source, DefaultTemplateEnum template,
        String rule) {
        super(source);
        this.template = template;
        this.rule = rule;
    }

    public DefaultTemplateEnum getTemplate() {
        return template;
    }

    public String getRule() {
        return rule;
    }
}
