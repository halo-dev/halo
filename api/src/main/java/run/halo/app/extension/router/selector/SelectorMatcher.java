package run.halo.app.extension.router.selector;

import org.springframework.lang.NonNull;
import run.halo.app.extension.index.query.LabelCondition;

public interface SelectorMatcher {

    String getKey();

    /**
     * Returns true if a field value matches.
     *
     * @param s the field value
     * @return the boolean
     */
    boolean test(String s);

    @NonNull
    LabelCondition toCondition();

}
