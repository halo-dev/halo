package cc.ryanc.halo.service;

import cc.ryanc.halo.model.domain.Tag;

import java.util.List;
import java.util.Optional;

/**
 * <pre>
 *     标签业务逻辑接口
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2018/1/12
 */
public interface TagService {

    /**
     * 新增/修改标签
     *
     * @param tag tag
     * @return Tag
     */
    Tag save(Tag tag);

    /**
     * 根据编号移除标签
     *
     * @param tagId tagId
     * @return Tag
     */
    Tag remove(Long tagId);

    /**
     * 获取所有标签
     *
     * @return List
     */
    List<Tag> findAll();

    /**
     * 根据编号查询标签
     *
     * @param tagId tagId
     * @return Optional
     */
    Optional<Tag> findByTagId(Long tagId);

    /**
     * 根据标签路径查询
     *
     * @param tagUrl tagUrl
     * @return Tag
     */
    Tag findByTagUrl(String tagUrl);

    /**
     * 根据标签名称查询
     *
     * @param tagName tagName
     * @return Tag
     */
    Tag findTagByTagName(String tagName);

    /**
     * 转换标签字符串为实体集合
     *
     * @param tagList tagList
     * @return List
     */
    List<Tag> strListToTagList(String tagList);
}
