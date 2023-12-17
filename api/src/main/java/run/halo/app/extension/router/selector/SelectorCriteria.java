package run.halo.app.extension.router.selector;

import java.util.Set;

public record SelectorCriteria(String key, Operator operator, Set<String> values) {

}
