package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.domain.User;
import cc.ryanc.halo.repository.UserRepository;
import cc.ryanc.halo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author : RYAN0UP
 * @version : 1.0
 * @date : 2017/11/14
 * description:
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * 保存个人资料
     * @param user user
     */
    @Override
    public void saveByUser(User user) {
        userRepository.save(user);
    }

    /**
     * 根据用户名和密码查询
     * @param userName userName
     * @param userPass userPass
     * @return user
     */
    @Override
    public User userLogin(String userName, String userPass) {
        return userRepository.findByUserNameAndUserPass(userName,userPass);
    }

    /**
     * 查询所有用户
     * @return list
     */
    @Override
    public List<User> findAllUser() {
        return userRepository.findAll();
    }

    /**
     * 验证修改密码时，密码是否正确
     * @param userId userid
     * @param userPass userpass
     * @return User
     */
    @Override
    public User findByUserIdAndUserPass(Integer userId, String userPass) {
        return userRepository.findByUserIdAndUserPass(userId,userPass);
    }
}
