package run.halo.app.security.authorization;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;
import run.halo.app.core.extension.Role;

/**
 * Tests for {@link RbacRequestEvaluation}.
 *
 * @author guqing
 * @since 2.4.0
 */
class RbacRequestEvaluationTest {

    @Test
    void resourceNameMatches() {
        RbacRequestEvaluation rbacRequestEvaluation = new RbacRequestEvaluation();
        assertThat(matchResourceName(rbacRequestEvaluation, "", "fake/test")).isTrue();
        assertThat(matchResourceName(rbacRequestEvaluation, "", "fake")).isTrue();
        assertThat(matchResourceName(rbacRequestEvaluation, "", "")).isTrue();
        assertThat(matchResourceName(rbacRequestEvaluation, "*", null)).isTrue();

        assertThat(matchResourceName(rbacRequestEvaluation, "*/test", "fake/test")).isTrue();

        assertThat(matchResourceName(rbacRequestEvaluation, "*/test", "hello/test")).isTrue();
        assertThat(matchResourceName(rbacRequestEvaluation, "*/test", "hello/fake")).isFalse();

        assertThat(matchResourceName(rbacRequestEvaluation, "test/*", "hello/fake")).isFalse();
        assertThat(matchResourceName(rbacRequestEvaluation, "test/*", "test/fake")).isTrue();
        assertThat(matchResourceName(rbacRequestEvaluation, "test/*", "test")).isTrue();
        assertThat(matchResourceName(rbacRequestEvaluation, "test/*", "hello")).isFalse();

        assertThat(matchResourceName(rbacRequestEvaluation, "*/*", "test/fake")).isTrue();
        assertThat(matchResourceName(rbacRequestEvaluation, "*/*", "test")).isTrue();

        assertThat(matchResourceName(rbacRequestEvaluation, "*", "test")).isTrue();
        assertThat(matchResourceName(rbacRequestEvaluation, "*", "hello")).isTrue();
    }

    boolean matchResourceName(RbacRequestEvaluation rbacRequestEvaluation, String rule,
        String requestedName) {
        return rbacRequestEvaluation.resourceNameMatches(new Role.PolicyRule.Builder()
            .resourceNames(rule)
            .build(), requestedName);
    }
}
