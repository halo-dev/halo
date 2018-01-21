package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.domain.UserMeta;
import cc.ryanc.halo.repository.UserMetaRepository;
import cc.ryanc.halo.service.UserMetaService;
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
public class UserMetaServiceImpl implements UserMetaService {

    @Autowired
    private UserMetaRepository userMetaRepository;

    @Override
    public void saveByUserMeta(UserMeta userMeta) {
        if(null==this.findByUserMetaKey(userMeta.getUserMetaKey())){

        }
    }

    @Override
    public void saveByUserMetas(List<UserMeta> userMetas) {

    }

    /**
     * 根据key值查找
     * @param key key
     * @return
     */
    @Override
    public UserMeta findByUserMetaKey(String key) {
        return userMetaRepository.findByUserMetaKey(key);
    }
}
