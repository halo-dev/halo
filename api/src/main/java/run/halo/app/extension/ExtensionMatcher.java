package run.halo.app.extension;

@FunctionalInterface
public interface ExtensionMatcher {

    boolean match(Extension extension);

}
