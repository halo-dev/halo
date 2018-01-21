package cc.ryanc.halo.service;

import cc.ryanc.halo.model.domain.UserMeta;

import java.util.List;

/**
 * @author : RYAN0UP
 * @version : 1.0
 * @date : 2017/11/14
 * description:
 */
public interface UserMetaService {

    /**
     * 保存单个用户附加信息
     * @param userMeta userMeta
     */
    void saveByUserMeta(UserMeta userMeta);

    /**
     * 批量保存用户附加信息
     * @param userMetas userMeta
     */
    void saveByUserMetas(List<UserMeta> userMetas);

    /**
     * 根据userMetaKey来查询，主要用于判断重复
     * @param key key
     * @return userMeta
     */
    UserMeta findByUserMetaKey(String key);
}
