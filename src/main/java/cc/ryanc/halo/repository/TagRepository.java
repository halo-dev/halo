package cc.ryanc.halo.repository;

import cc.ryanc.halo.model.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author : RYAN0UP
 * @date : 2018/1/12
 * @version : 1.0
 */
public interface TagRepository extends JpaRepository<Tag,Long>{

    /**
     * 根据标签路径查询，用于验证是否已经存在该路径
     *
     * @param tagUrl tagUrl
     * @return tag
     */
    Tag findTagByTagUrl(String tagUrl);

    /**
     * 根据标签名称查询
     *
     * @param tagName 标签名
     * @return tag
     */
    Tag findTagByTagName(String tagName);
}
