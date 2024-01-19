package run.halo.app.extension.router.selector;

public interface SelectorMatcher {

    String getKey();

    /**
     * Returns true if a field value matches.
     *
     * @param s the field value
     * @return the boolean
     */
    boolean test(String s);
}
