package run.halo.app.extension.router.selector;

import lombok.Getter;

@Getter
public class OrSelectorMatcher extends LogicalMatcher {
    private final SelectorMatcher left;
    private final SelectorMatcher right;

    public OrSelectorMatcher(SelectorMatcher left, SelectorMatcher right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public String getKey() {
        return null;
    }

    @Override
    public boolean test(String s) {
        return left.test(s) || right.test(s);
    }
}
