package run.halo.app.theme.finders.vo;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import run.halo.app.core.extension.User;
import run.halo.app.extension.Metadata;
import run.halo.app.infra.utils.JsonUtils;

/**
 * Tests for {@link UserVo}.
 *
 * @author guqing
 * @since 2.0.1
 */
class UserVoTest {

    @Test
    void from() throws JSONException {
        User user = new User();
        user.setMetadata(new Metadata());
        user.getMetadata().setName("fake-user");
        user.setSpec(new User.UserSpec());
        user.getSpec().setPassword("123456");
        user.getSpec().setEmail("example@example.com");
        user.getSpec().setAvatar("avatar");
        user.getSpec().setDisplayName("fake-user-display-name");
        user.getSpec().setBio("user bio");
        user.getSpec().setDisabled(false);
        user.getSpec().setPhone("123456789");
        user.getSpec().setRegisteredAt(Instant.parse("2022-01-01T00:00:00.00Z"));
        user.getSpec().setLoginHistoryLimit(5);
        user.getSpec().setTwoFactorAuthEnabled(false);

        user.setStatus(new User.UserStatus());
        user.getStatus().setLastLoginAt(Instant.parse("2022-01-02T00:00:00.00Z"));
        User.LoginHistory loginHistory = new User.LoginHistory();
        loginHistory.setLoginAt(Instant.parse("2022-01-02T00:00:00.00Z"));
        loginHistory.setReason("login reason");
        loginHistory.setUserAgent("user agent");
        user.getStatus().setLoginHistories(List.of(loginHistory));

        UserVo userVo = UserVo.from(user);
        JSONAssert.assertEquals("""
                {
                    "metadata": {
                        "name": "fake-user"
                    },
                    "spec": {
                        "displayName": "fake-user-display-name",
                        "avatar": "avatar",
                        "email": "example@example.com",
                        "phone": "123456789",
                        "password": "[PROTECTED]",
                        "bio": "user bio",
                        "registeredAt": "2022-01-01T00:00:00Z",
                        "twoFactorAuthEnabled": false,
                        "disabled": false,
                        "loginHistoryLimit": 5
                    },
                    "status": {
                        "loginHistories": []
                    }
                }
                """,
            JsonUtils.objectToJson(userVo),
            true);
    }

    @Test
    void fromWhenStatusIsNull() {
        User user = new User();
        user.setMetadata(new Metadata());
        user.getMetadata().setName("fake-user");
        user.setSpec(new User.UserSpec());
        UserVo userVo = UserVo.from(user);

        assertThat(userVo).isNotNull();
    }
}