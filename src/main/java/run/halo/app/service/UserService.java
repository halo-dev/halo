package run.halo.app.service;

import java.util.Optional;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import run.halo.app.exception.ForbiddenException;
import run.halo.app.exception.NotFoundException;
import run.halo.app.model.entity.User;
import run.halo.app.model.enums.MFAType;
import run.halo.app.model.params.UserParam;
import run.halo.app.service.base.CrudService;

/**
 * User service interface.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-03-14
 */
public interface UserService extends CrudService<User, Integer> {

    /**
     * Login failure count key.
     */
    String LOGIN_FAILURE_COUNT_KEY = "login.failure.count";

    /**
     * Max login try count.
     */
    int MAX_LOGIN_TRY = 5;

    /**
     * Lock minutes.
     */
    int LOCK_MINUTES = 10;

    /**
     * Gets current user.
     *
     * @return an optional user
     */
    @NonNull
    Optional<User> getCurrentUser();

    /**
     * Gets user by username.
     *
     * @param username username must not be blank
     * @return an optional user
     */
    @NonNull
    Optional<User> getByUsername(@NonNull String username);

    /**
     * Gets non null user by username.
     *
     * @param username username
     * @return user info
     * @throws NotFoundException throws when the username does not exist
     */
    @NonNull
    User getByUsernameOfNonNull(@NonNull String username);

    /**
     * Gets user by email.
     *
     * @param email email must not be blank
     * @return an optional user
     */
    @NonNull
    Optional<User> getByEmail(@NonNull String email);

    /**
     * Gets non null user by email.
     *
     * @param email email
     * @return user info
     * @throws NotFoundException throws when the username does not exist
     */
    @NonNull
    User getByEmailOfNonNull(@NonNull String email);

    /**
     * Updates user password.
     *
     * @param oldPassword old password must not be blank
     * @param newPassword new password must not be blank
     * @param userId user id must not be null
     * @return updated user detail
     */
    @NonNull
    User updatePassword(@NonNull String oldPassword, @NonNull String newPassword,
        @NonNull Integer userId);

    /**
     * Creates an user.
     *
     * @param userParam user param must not be null.
     * @return created user
     */
    @NonNull
    User createBy(@NonNull UserParam userParam);

    /**
     * The user must not expire.
     *
     * @param user user info must not be null
     * @throws ForbiddenException throws if the given user has been expired
     */
    void mustNotExpire(@NonNull User user);

    /**
     * Checks the password is match the user password.
     *
     * @param user user info must not be null
     * @param plainPassword plain password
     * @return true if the given password is match the user password; false otherwise
     */
    boolean passwordMatch(@NonNull User user, @Nullable String plainPassword);

    /**
     * Set user password.
     *
     * @param user user must not be null
     * @param plainPassword plain password must not be blank
     */
    void setPassword(@NonNull User user, @NonNull String plainPassword);

    /**
     * verify user's email and username
     *
     * @param username username must not be null
     * @param password password must not be null
     * @return boolean
     */
    boolean verifyUser(@NonNull String username, @NonNull String password);

    /**
     * Updates user Multi-Factor Auth.
     *
     * @param mfaType Multi-Factor Auth Type.
     * @param mfaKey Multi-Factor Auth Key.
     * @param userId user id must not be null
     * @return updated user detail
     */
    @NonNull
    User updateMFA(@NonNull MFAType mfaType, String mfaKey, @NonNull Integer userId);

}
