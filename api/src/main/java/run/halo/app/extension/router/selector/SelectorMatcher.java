package run.halo.app.extension.router.selector;

public interface SelectorMatcher {

    String getKey();

    /**
     * Returns true if a label value matches.
     *
     * @param s the label value
     * @return the boolean
     */
    boolean test(String s);
}
