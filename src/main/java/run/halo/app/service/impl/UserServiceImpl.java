package run.halo.app.service.impl;

import cn.hutool.core.lang.Validator;
import cn.hutool.crypto.digest.BCrypt;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import run.halo.app.cache.StringCacheStore;
import run.halo.app.cache.lock.CacheLock;
import run.halo.app.event.logger.LogEvent;
import run.halo.app.event.user.UserUpdatedEvent;
import run.halo.app.exception.BadRequestException;
import run.halo.app.exception.ForbiddenException;
import run.halo.app.exception.NotFoundException;
import run.halo.app.model.entity.User;
import run.halo.app.model.enums.LogType;
import run.halo.app.model.params.UserParam;
import run.halo.app.repository.UserRepository;
import run.halo.app.security.context.SecurityContextHolder;
import run.halo.app.security.filter.AdminAuthenticationFilter;
import run.halo.app.security.support.UserDetail;
import run.halo.app.service.UserService;
import run.halo.app.service.base.AbstractCrudService;
import run.halo.app.utils.DateUtils;
import run.halo.app.utils.HaloUtils;
import run.halo.app.utils.ServletUtils;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * UserService implementation class
 *
 * @author ryanwang
 * @date : 2019-03-14
 */
@Service
public class UserServiceImpl extends AbstractCrudService<User, Integer> implements UserService {

    private final UserRepository userRepository;

    private final StringCacheStore stringCacheStore;

    private final ApplicationEventPublisher eventPublisher;

    public UserServiceImpl(UserRepository userRepository,
                           StringCacheStore stringCacheStore,
                           ApplicationEventPublisher eventPublisher) {
        super(userRepository);
        this.userRepository = userRepository;
        this.stringCacheStore = stringCacheStore;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Optional<User> getCurrentUser() {
        // Find all users
        List<User> users = listAll();

        if (CollectionUtils.isEmpty(users)) {
            // Return empty user
            return Optional.empty();
        }

        // Return the first user
        return Optional.of(users.get(0));
    }

    @Override
    public Optional<User> getByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User getByUsernameOfNonNull(String username) {
        return getByUsername(username).orElseThrow(() -> new NotFoundException("The username dose not exist").setErrorData(username));
    }

    /**
     * Gets user by email.
     *
     * @param email email must not be blank
     * @return an optional user
     */
    @Override
    public Optional<User> getByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Gets non null user by email.
     *
     * @param email email
     * @return user info
     * @throws NotFoundException throws when the username does not exist
     */
    @Override
    public User getByEmailOfNonNull(String email) {
        return getByEmail(email).orElseThrow(() -> new NotFoundException("The email dose not exist").setErrorData(email));
    }

    @Override
    public User login(String key, String password) {
        Assert.hasText(key, "Username or email must not be blank");
        Assert.hasText(password, "Password must not be blank");

        // Check login status
        if (SecurityContextHolder.getContext().isAuthenticated()) {
            throw new BadRequestException("You have logged in already, no need to log in again");
        }

        // Ger user by username
        User user = Validator.isEmail(key) ? getByEmailOfNonNull(key) : getByUsernameOfNonNull(key);

        Date now = DateUtils.now();

        // Check expiration
        if (user.getExpireTime() != null && user.getExpireTime().after(now)) {
            long seconds = TimeUnit.MILLISECONDS.toSeconds(user.getExpireTime().getTime() - now.getTime());
            // If expired
            throw new BadRequestException("You have been temporarily disabled，please try again " + HaloUtils.timeFormat(seconds) + " later").setErrorData(seconds);
        }

        if (!BCrypt.checkpw(password, user.getPassword())) {
            // If the password is mismatch
            // Add login failure count
            Integer loginFailureCount = stringCacheStore.getAny(LOGIN_FAILURE_COUNT_KEY, Integer.class).orElse(0);

            if (loginFailureCount >= MAX_LOGIN_TRY - 1) {
                // Set expiration
                user.setExpireTime(org.apache.commons.lang3.time.DateUtils.addMinutes(now, LOCK_MINUTES));
                // Update user
                update(user);
            }

            loginFailureCount++;

            stringCacheStore.putAny(LOGIN_FAILURE_COUNT_KEY, loginFailureCount, LOCK_MINUTES, TimeUnit.MINUTES);

            int remainder = MAX_LOGIN_TRY - loginFailureCount;

            String errorMessage = String.format("Username or password incorrect, you%shave %s", remainder <= 0 ? "" : " still ", HaloUtils.pluralize(remainder, "chance", "chances"));

            // Lot it
            eventPublisher.publishEvent(new LogEvent(this, key, LogType.LOGIN_FAILED, password));

            throw new BadRequestException(errorMessage);
        }

        // Clear the login failure count cache
        stringCacheStore.delete(LOGIN_FAILURE_COUNT_KEY);

        // Set session
        ServletUtils.getCurrentRequest().ifPresent(request -> {
            request.getSession().setAttribute(AdminAuthenticationFilter.ADMIN_SESSION_KEY, new UserDetail(user));
        });

        // Log it
        eventPublisher.publishEvent(new LogEvent(this, user.getId().toString(), LogType.LOGGED_IN, user.getUsername()));

        return user;
    }

    @Override
    public User updatePassword(String oldPassword, String newPassword, Integer userId) {
        Assert.hasText(oldPassword, "Old password must not be blank");
        Assert.hasText(newPassword, "New password must not be blank");
        Assert.notNull(userId, "User id must not be blank");

        if (oldPassword.equals(newPassword)) {
            throw new BadRequestException("There is nothing changed because new password is equal to old password");
        }

        // Get the user
        User user = getById(userId);

        // Check the user old password
        if (!BCrypt.checkpw(oldPassword, user.getPassword())) {
            throw new BadRequestException("Old password is mismatch").setErrorData(oldPassword);
        }

        // Set new password
        setPassword(user, newPassword);

        // Update this user
        User updatedUser = update(user);

        // Log it
        eventPublisher.publishEvent(new LogEvent(this, updatedUser.getId().toString(), LogType.PASSWORD_UPDATED, oldPassword));

        return updatedUser;
    }

    @Override
    public User createBy(UserParam userParam) {
        Assert.notNull(userParam, "User param must not be null");

        User user = userParam.convertTo();

        setPassword(user, userParam.getPassword());

        return create(user);
    }

    @Override
    public void mustNotExpire(User user) {
        Assert.notNull(user, "User must not be null");

        Date now = DateUtils.now();
        if (user.getExpireTime() != null && user.getExpireTime().after(now)) {
            long seconds = TimeUnit.MILLISECONDS.toSeconds(user.getExpireTime().getTime() - now.getTime());
            // If expired
            throw new ForbiddenException("You have been temporarily disabled，please try again " + HaloUtils.timeFormat(seconds) + " later").setErrorData(seconds);
        }
    }

    @Override
    public boolean passwordMatch(User user, String plainPassword) {
        Assert.notNull(user, "User must not be null");

        return !StringUtils.isBlank(plainPassword) && BCrypt.checkpw(plainPassword, user.getPassword());
    }

    @Override
    @CacheLock
    public User create(User user) {
        // Check user
        if (count() != 0) {
            throw new BadRequestException("This blog already exists a blogger");
        }

        User createdUser = super.create(user);

        eventPublisher.publishEvent(new UserUpdatedEvent(this, createdUser.getId()));

        return createdUser;
    }

    @Override
    public User update(User user) {
        User updatedUser = super.update(user);

        // Log it
        eventPublisher.publishEvent(new LogEvent(this, user.getId().toString(), LogType.PROFILE_UPDATED, user.getUsername()));
        eventPublisher.publishEvent(new UserUpdatedEvent(this, user.getId()));

        return updatedUser;
    }

    private void setPassword(@NonNull User user, @NonNull String plainPassword) {
        Assert.notNull(user, "User must not be null");
        Assert.hasText(plainPassword, "Plain password must not be blank");

        user.setPassword(BCrypt.hashpw(plainPassword, BCrypt.gensalt()));
    }

}
