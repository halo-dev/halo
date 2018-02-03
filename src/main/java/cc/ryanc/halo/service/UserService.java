package cc.ryanc.halo.service;

import cc.ryanc.halo.model.domain.User;

import java.util.List;

/**
 * @author : RYAN0UP
 * @version : 1.0
 * @date : 2017/11/14
 * description:
 */
public interface UserService {
    /**
     * 保存个人资料
     *
     * @param user user
     */
    void saveByUser(User user);

    /**
     * 根据用户名和密码查询，用于登录
     *
     * @param userName userName
     * @param userPass userPass
     * @return User
     */
    List<User> userLoginByName(String userName,String userPass);

    /**
     * 根据邮箱和密码查询，用户登录
     * @param userEmail userEmail
     * @param userPass userPass
     * @return list
     */
    List<User> userLoginByEmail(String userEmail,String userPass);

    /**
     * 查询所有用户
     *
     * @return list
     */
    List<User> findAllUser();

    /**
     * 根据用户编号和密码查询
     *
     * @param userId userid
     * @param userPass userpass
     * @return user
     */
    User findByUserIdAndUserPass(Long userId,String userPass);
}
