package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.entity.User;
import cc.ryanc.halo.repository.UserRepository;
import cc.ryanc.halo.service.UserService;
import cc.ryanc.halo.service.base.AbstractCrudService;
import org.springframework.stereotype.Service;

/**
 * UserService implementation class
 *
 * @author : RYAN0UP
 * @date : 2019-03-14
 */
@Service
public class UserServiceImpl extends AbstractCrudService<User, Integer> implements UserService {

    private UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        super(userRepository);
        this.userRepository = userRepository;
    }
}
