package run.halo.app.infra.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link PathUtils}.
 *
 * @author guqing
 * @since 2.0.0
 */
class PathUtilsTest {

    @Test
    void combinePath() {
        Map<String, String> combinePathCases = getCombinePathCases();
        combinePathCases.forEach((segments, expected) -> {
            String s = PathUtils.combinePath(segments.split(","));
            assertThat(s).isEqualTo(expected);
        });

        String s = PathUtils.combinePath("a", "", "c");
        assertThat(s).isEqualTo("/a/c");
    }

    private Map<String, String> getCombinePathCases() {
        Map<String, String> combinePathCases = new HashMap<>();
        combinePathCases.put("a,b,c", "/a/b/c");
        combinePathCases.put("/a,b,c", "/a/b/c");
        combinePathCases.put("/a,b/,c", "/a/b/c");
        combinePathCases.put("/a,/b/,c", "/a/b/c");
        return combinePathCases;
    }

    @Test
    void appendPathSeparatorIfMissing() {
        String s = PathUtils.appendPathSeparatorIfMissing("a");
        assertThat(s).isEqualTo("a/");

        s = PathUtils.appendPathSeparatorIfMissing("a/");
        assertThat(s).isEqualTo("a/");

        s = PathUtils.appendPathSeparatorIfMissing(null);
        assertThat(s).isEqualTo(null);
    }

    @Test
    void simplifyPathPattern() {
        assertThat(PathUtils.simplifyPathPattern("/a/b/c")).isEqualTo("/a/b/c");
        assertThat(PathUtils.simplifyPathPattern("/a/{b}/c")).isEqualTo("/a/{b}/c");
        assertThat(PathUtils.simplifyPathPattern("/a/{b}/*")).isEqualTo("/a/{b}/*");
        assertThat(PathUtils.simplifyPathPattern("/archives/{year:\\d{4}}/{month:\\d{2}}"))
            .isEqualTo("/archives/{year}/{month}");
        assertThat(PathUtils.simplifyPathPattern("/archives/{year:\\d{4}}/{slug}"))
            .isEqualTo("/archives/{year}/{slug}");
        assertThat(PathUtils.simplifyPathPattern("/archives/{year:\\d{4}}/page/{page:\\d+}"))
            .isEqualTo("/archives/{year}/page/{page}");
    }

    @Test
    void isAbsoluteUri() {
        String[] absoluteUris = new String[] {
            "ftp://ftp.is.co.za/rfc/rfc1808.txt",
            "http://www.ietf.org/rfc/rfc2396.txt",
            "ldap://[2001:db8::7]/c=GB?objectClass?one",
            "mailto:John.Doe@example.com",
            "news:comp.infosystems.www.servers.unix",
            "tel:+1-816-555-1212",
            "telnet://192.0.2.16:80/",
            "urn:oasis:names:specification:docbook:dtd:xml:4.1.2",
            "data:text/vnd-example+xyz;foo=bar;base64,R0lGODdh",
            "irc://irc.example.com:6667/#some-channel",
            "ircs://irc.example.com:6667/#some-channel",
            "irc6://irc.example.com:6667/#some-channel"
        };
        for (String uri : absoluteUris) {
            assertThat(PathUtils.isAbsoluteUri(uri)).isTrue();
        }

        String[] paths = new String[] {
            "//example.com/path/resource.txt",
            "/path/resource.txt",
            "path/resource.txt",
            "../resource.txt",
            "./resource.txt",
            "resource.txt",
            "#fragment",
            "",
            null
        };
        for (String path : paths) {
            assertThat(PathUtils.isAbsoluteUri(path)).isFalse();
        }
    }
}