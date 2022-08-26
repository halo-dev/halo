package run.halo.app.content;

import org.springframework.context.ApplicationEvent;
import run.halo.app.theme.DefaultTemplateEnum;

public class PermalinkRuleChangedEvent extends ApplicationEvent {
    private DefaultTemplateEnum template;
    private String rule;

    public PermalinkRuleChangedEvent(Object source, DefaultTemplateEnum template,
        String rule) {
        super(source);
    }

    public DefaultTemplateEnum getTemplate() {
        return template;
    }

    public String getRule() {
        return rule;
    }
}
