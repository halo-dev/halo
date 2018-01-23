package cc.ryanc.halo.repository;

import cc.ryanc.halo.model.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author : RYAN0UP
 * @date : 2017/11/14
 * @version : 1.0
 * description:
 */
public interface UserRepository extends JpaRepository<User,Long>{
    /**
     * 根据用户名和密码查询
     * @param userName userName
     * @param userPass userPass
     * @return User
     */
    User findByUserNameAndUserPass(String userName,String userPass);

    /**
     * 根据用户编号和密码查询
     * @param userId userId
     * @param userPass userpass
     * @return User
     */
    User findByUserIdAndUserPass(Long userId,String userPass);
}
