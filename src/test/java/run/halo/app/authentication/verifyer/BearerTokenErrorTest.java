package run.halo.app.authentication.verifyer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import run.halo.app.identity.authentication.verifier.BearerTokenError;

/**
 * Tests for {@link BearerTokenError}.
 *
 * @author guqing
 * @see <a href="https://tools.ietf.org/html/rfc6750#section-3.1">Bearer Token Error</a>
 * @since 2.0.0
 */
public class BearerTokenErrorTest {
    private static final String TEST_ERROR_CODE = "test-code";

    private static final HttpStatus TEST_HTTP_STATUS = HttpStatus.UNAUTHORIZED;

    private static final String TEST_DESCRIPTION = "test-description";

    private static final String TEST_URI = "https://example.com";

    private static final String TEST_SCOPE = "test-scope";

    @Test
    public void constructorWithErrorCodeWhenErrorCodeIsValidThenCreated() {
        BearerTokenError error =
            new BearerTokenError(TEST_ERROR_CODE, TEST_HTTP_STATUS, null, null);
        assertThat(error.getErrorCode()).isEqualTo(TEST_ERROR_CODE);
        assertThat(error.getHttpStatus()).isEqualTo(TEST_HTTP_STATUS);
        assertThat(error.getDescription()).isNull();
        assertThat(error.getUri()).isNull();
        assertThat(error.getScope()).isNull();
    }

    @Test
    public void constructorWithErrorCodeThenThrowIllegalArgumentException() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new BearerTokenError(null, TEST_HTTP_STATUS, null, null))
            .withMessage("errorCode cannot be empty");
    }

    @Test
    public void constructorWhenErrorCodeIsEmptyThenThrowIllegalArgumentException() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new BearerTokenError("", TEST_HTTP_STATUS, null, null))
            .withMessage("errorCode cannot be empty");
    }

    @Test
    public void constructorWhenHttpStatusIsNullThenThrowIllegalArgumentException() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new BearerTokenError(TEST_ERROR_CODE, null, null, null))
            .withMessage("httpStatus cannot be null");
    }

    @Test
    public void constructorWithAllParametersWhenAllParametersAreValidThenCreated() {
        BearerTokenError error =
            new BearerTokenError(TEST_ERROR_CODE, TEST_HTTP_STATUS, TEST_DESCRIPTION, TEST_URI,
                TEST_SCOPE);
        assertThat(error.getErrorCode()).isEqualTo(TEST_ERROR_CODE);
        assertThat(error.getHttpStatus()).isEqualTo(TEST_HTTP_STATUS);
        assertThat(error.getDescription()).isEqualTo(TEST_DESCRIPTION);
        assertThat(error.getUri()).isEqualTo(TEST_URI);
        assertThat(error.getScope()).isEqualTo(TEST_SCOPE);
    }

    @Test
    public void constructorWithAllParametersWhenErrorCodeIsNullThenThrowIllegalArgumentException() {
        assertThatIllegalArgumentException()
            .isThrownBy(
                () -> new BearerTokenError(null, TEST_HTTP_STATUS, TEST_DESCRIPTION, TEST_URI,
                    TEST_SCOPE))
            .withMessage("errorCode cannot be empty");
    }

    @Test
    public void constructorWithAllParametersThrowIllegalArgumentException1() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new BearerTokenError("", TEST_HTTP_STATUS, TEST_DESCRIPTION, TEST_URI,
                TEST_SCOPE))
            .withMessage("errorCode cannot be empty");
    }

    @Test
    public void constructorWithAllParametersThrowIllegalArgumentException2() {
        assertThatIllegalArgumentException()
            .isThrownBy(
                () -> new BearerTokenError(TEST_ERROR_CODE, null, TEST_DESCRIPTION, TEST_URI,
                    TEST_SCOPE))
            .withMessage("httpStatus cannot be null");
    }

    @Test
    public void constructorWhenErrorCodeIsInvalidThenThrowIllegalArgumentException() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new BearerTokenError(TEST_ERROR_CODE + "\"",
                TEST_HTTP_STATUS, TEST_DESCRIPTION, TEST_URI, TEST_SCOPE)
            )
            .withMessageContaining("errorCode")
            .withMessageContaining("RFC 6750");
    }

    @Test
    public void constructorWithAllParametersThrowIllegalArgumentException3() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new BearerTokenError(TEST_ERROR_CODE, TEST_HTTP_STATUS,
                TEST_DESCRIPTION + "\"", TEST_URI, TEST_SCOPE)
            )
            .withMessageContaining("description")
            .withMessageContaining("RFC 6750");
    }

    @Test
    public void constructorWithAllParametersThrowIllegalArgumentException4() {
        assertThatIllegalArgumentException()
            .isThrownBy(
                () -> new BearerTokenError(TEST_ERROR_CODE, TEST_HTTP_STATUS, TEST_DESCRIPTION,
                    TEST_URI + "\"", TEST_SCOPE)
            )
            .withMessageContaining("errorUri")
            .withMessageContaining("RFC 6750");
    }

    @Test
    public void constructorWithAllParametersWhenScopeIsInvalidThenThrowIllegalArgumentException() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new BearerTokenError(TEST_ERROR_CODE, TEST_HTTP_STATUS,
                TEST_DESCRIPTION, TEST_URI, TEST_SCOPE + "\"")
            )
            .withMessageContaining("scope")
            .withMessageContaining("RFC 6750");
    }
}
