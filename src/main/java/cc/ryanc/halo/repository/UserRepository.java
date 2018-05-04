package cc.ryanc.halo.repository;

import cc.ryanc.halo.model.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author : RYAN0UP
 * @date : 2017/11/14
 * @version : 1.0
 */
public interface UserRepository extends JpaRepository<User,Long>{

    /**
     * 根据用户名和密码查询
     *
     * @param userName userName
     * @param userPass userPass
     * @return list
     */
    List<User> findByUserNameAndUserPass(String userName, String userPass);

    /**
     * 根据邮箱和密码查询
     *
     * @param userEmail userEmail
     * @param userPass userPass
     * @return list
     */
    List<User> findByUserEmailAndUserPass(String userEmail,String userPass);

    /**
     * 根据用户编号和密码查询
     *
     * @param userId userId
     * @param userPass userpass
     * @return User
     */
    User findByUserIdAndUserPass(Long userId,String userPass);
}
