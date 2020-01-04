package run.halo.app.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import run.halo.app.model.entity.CommentBlackList;
import run.halo.app.repository.base.BaseRepository;

/**
 * 评论黑名单Repository
 *
 * @author Lei XinXin
 * @date 2020/1/3
 */
public interface CommentBlackListRepository extends BaseRepository<CommentBlackList, Long> {

    /**
     * 根据IP地址获取数据
     *
     * @param ipAddress
     * @return
     */
    @Query("SELECT commentBlackList FROM CommentBlackList commentBlackList WHERE commentBlackList.ipAddress=?1")
    CommentBlackList getByIpAddress(String ipAddress);

    /**
     * Update Comment BlackList By IPAddress
     *
     * @param commentBlackList
     * @return result
     */
    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query("UPDATE CommentBlackList SET banTime=:#{#commentBlackList.banTime} WHERE ipAddress=:#{#commentBlackList.ipAddress}")
    int updateByIpAddress(@Param("commentBlackList") CommentBlackList commentBlackList);
}
