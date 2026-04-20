package run.halo.app.theme.finders.vo;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang3.ObjectUtils;
import run.halo.app.core.extension.User;
import run.halo.app.extension.MetadataOperator;
import run.halo.app.infra.utils.JsonUtils;

@Value
@Builder
public class UserVo implements ExtensionVoOperator {

    @Schema(requiredMode = REQUIRED)
    MetadataOperator metadata;

    User.UserSpec spec;

    User.UserStatus status;

    /**
     * Converts to {@link UserVo} from {@link User}.
     *
     * @param user user extension
     * @return user value object.
     */
    public static UserVo from(User user) {
        User.UserStatus statusCopy =
            JsonUtils.deepCopy(ObjectUtils.defaultIfNull(user.getStatus(), new User.UserStatus()));

        User.UserSpec userSpecCopy = JsonUtils.deepCopy(user.getSpec());
        userSpecCopy.setPassword("[PROTECTED]");
        return UserVo.builder()
            .metadata(user.getMetadata())
            .spec(userSpecCopy)
            .status(statusCopy)
            .build();
    }
}
