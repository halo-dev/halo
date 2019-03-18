package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.cache.StringCacheStore;
import cc.ryanc.halo.exception.BadRequestException;
import cc.ryanc.halo.exception.NotFoundException;
import cc.ryanc.halo.model.entity.User;
import cc.ryanc.halo.repository.UserRepository;
import cc.ryanc.halo.service.UserService;
import cc.ryanc.halo.service.base.AbstractCrudService;
import cc.ryanc.halo.utils.DateUtils;
import cn.hutool.crypto.digest.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * UserService implementation class
 *
 * @author : RYAN0UP
 * @date : 2019-03-14
 */
@Service
public class UserServiceImpl extends AbstractCrudService<User, Integer> implements UserService {

    private final UserRepository userRepository;

    private final StringCacheStore stringCacheStore;

    public UserServiceImpl(UserRepository userRepository,
                           StringCacheStore stringCacheStore) {
        super(userRepository);
        this.userRepository = userRepository;
        this.stringCacheStore = stringCacheStore;
    }

    @Override
    public Optional<User> getByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User getByUsernameOfNonNull(String username) {
        return getByUsername(username).orElseThrow(() -> new NotFoundException("The username dose not exist").setErrorData(username));
    }

    @Override
    public User login(String username, String password) {
        Assert.hasText(username, "Username must not be blank");
        Assert.hasText(password, "Password must not be blank");

        // Ger user by username
        User user = getByUsernameOfNonNull(username);

        // Check expiration
        if (user.getExpireTime() != null && DateUtils.now().before(user.getExpireTime())) {
            // If expired
            // TODO replace by i18n
            throw new BadRequestException("You have been locked temporarily");
        }


        if (!BCrypt.checkpw(password, user.getPassword())) {
            // If the password is mismatched
            // Add login failure count
            Integer loginFailureCount = stringCacheStore.get(LOGIN_FAILURE_COUNT_KEY).map(countString -> Integer.valueOf(countString)).orElse(0);

            if (loginFailureCount >= MAX_LOGIN_TRY) {
                // Set expiration
                user.setExpireTime(org.apache.commons.lang3.time.DateUtils.addMilliseconds(DateUtils.now(), LOCK_MINUTES));
                // Update user
                update(user);
            }

            loginFailureCount++;

            stringCacheStore.put(LOGIN_FAILURE_COUNT_KEY, loginFailureCount.toString(), LOCK_MINUTES, TimeUnit.MINUTES);

            // TODO replace by i18n
            throw new BadRequestException("Username or password is mismatched, last " + (MAX_LOGIN_TRY - loginFailureCount) + " retry(s)");
        }

        // TODO Set session

        return user;
    }

}
