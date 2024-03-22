package run.halo.app.infra.exception;

import java.net.URI;
import java.util.List;
import org.pf4j.DependencyResolver.WrongDependencyVersion;
import org.springframework.web.server.ServerWebInputException;

public abstract class PluginDependencyException extends ServerWebInputException {

    public PluginDependencyException(String reason) {
        super(reason);
    }

    public PluginDependencyException(String reason, Throwable cause) {
        super(reason, null, cause);
    }

    protected PluginDependencyException(String reason, Throwable cause,
        String messageDetailCode, Object[] messageDetailArguments) {
        super(reason, null, cause, messageDetailCode, messageDetailArguments);
    }

    public static class CyclicException extends PluginDependencyException {

        public static final String TYPE = "https://halo.run/probs/plugin-cyclic-dependency";

        public CyclicException() {
            super("A cyclic dependency was detected.");
            setType(URI.create(TYPE));
        }
    }

    public static class NotFoundException extends PluginDependencyException {

        public static final String TYPE = "https://halo.run/probs/plugin-dependencies-not-found";

        public NotFoundException(List<String> dependencies) {
            super("Dependencies were not found.", null, null, new Object[] {dependencies});
            setType(URI.create(TYPE));
            getBody().setProperty("dependencies", dependencies);
        }

    }

    public static class WrongVersionsException extends PluginDependencyException {

        public static final String TYPE =
            "https://halo.run/probs/plugin-dependencies-with-wrong-versions";

        public WrongVersionsException(List<WrongDependencyVersion> versions) {
            super("Dependencies have wrong version.", null, null, new Object[] {versions});
            setType(URI.create(TYPE));
            getBody().setProperty("versions", versions);
        }
    }
}
