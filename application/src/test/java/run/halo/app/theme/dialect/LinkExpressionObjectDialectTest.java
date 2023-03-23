package run.halo.app.theme.dialect;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link LinkExpressionObjectDialect}.
 *
 * @author guqing
 * @since 2.0.0
 */
class LinkExpressionObjectDialectTest {

    private final LinkExpressionObjectDialect linkExpressionObjectDialect =
        new LinkExpressionObjectDialect();

    @Test
    void getExpressionObjectFactory() {
        assertThat(linkExpressionObjectDialect.getName())
            .isEqualTo("themeLink");
        assertThat(linkExpressionObjectDialect.getExpressionObjectFactory())
            .isInstanceOf(DefaultLinkExpressionFactory.class);
    }
}