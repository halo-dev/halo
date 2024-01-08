package run.halo.app.extension.router.selector;

import lombok.Getter;

@Getter
public class AndSelectorMatcher extends LogicalMatcher {
    private final SelectorMatcher left;
    private final SelectorMatcher right;

    public AndSelectorMatcher(SelectorMatcher left, SelectorMatcher right) {
        this.left = left;
        this.right = right;
    }

    public boolean test(String s) {
        return left.test(s) && right.test(s);
    }
}
