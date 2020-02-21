package run.halo.app.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import run.halo.app.model.entity.CommentBlackList;
import run.halo.app.repository.base.BaseRepository;

import java.util.Optional;

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
    Optional<CommentBlackList> findByIpAddress(String ipAddress);

    /**
     * Update Comment BlackList By IPAddress
     *
     * @param commentBlackList
     * @return result
     */
    @Modifying
    @Query("UPDATE CommentBlackList SET banTime=:#{#commentBlackList.banTime} WHERE ipAddress=:#{#commentBlackList.ipAddress}")
    int updateByIpAddress(@Param("commentBlackList") CommentBlackList commentBlackList);
}
