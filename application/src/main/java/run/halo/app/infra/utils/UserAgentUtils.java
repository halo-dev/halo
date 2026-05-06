package run.halo.app.infra.utils;

import java.io.IOException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.Assert;
import ua_parser.Client;
import ua_parser.Parser;

/**
 * User-Agent parsing utility class that uses the ua-parser library to parse User-Agent strings
 * and extract information about the client device, operating system, and browser.
 *
 * @author johnniang
 */
public enum UserAgentUtils {
    ;

    private static final Parser PARSER;

    static {
        var regexesResource = new ClassPathResource(
            "config/uap/regexes.yaml", UserAgentUtils.class.getClassLoader()
        );
        try {
            PARSER = new Parser(regexesResource.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(
                "Failed to load regexes.yaml during UserAgent Parser initialization", e
            );
        }
    }

    /**
     * Parses the given User-Agent string and returns a Client object containing the parsed
     * information.
     *
     * @param agentString the User-Agent string to parse
     * @return a Client object containing the parsed information
     */
    public static Client parse(String agentString) {
        Assert.hasText(agentString, "User-Agent header must not be blank");
        return PARSER.parse(agentString);
    }
}
