package cc.ryanc.halo.service;

import cc.ryanc.halo.model.domain.Tag;

import java.util.List;
import java.util.Optional;

/**
 * @author : RYAN0UP
 * @date : 2018/1/12
 * @version : 1.0
 * description :
 */
public interface TagService {

    /**
     * 新增/修改标签
     *
     * @param tag tag
     * @return Tag
     */
    Tag saveByTag(Tag tag);

    /**
     * 根据编号移除标签
     *
     * @param tagId tagId
     * @return Tag
     */
    Tag removeByTagId(Long tagId);

    /**
     * 获取所有标签
     *
     * @return list
     */
    List<Tag> findAllTags();

    /**
     * 根据编号查询标签
     *
     * @param tagId tagId
     * @return Link
     */
    Optional<Tag> findByTagId(Long tagId);

    /**
     * 根据标签路径查询
     *
     * @param tagUrl tagUrl
     * @return tag
     */
    Tag findByTagUrl(String tagUrl);

    /**
     * 根据标签名称查询
     *
     * @param tagName tagName
     * @return tag
     */
    Tag findTagByTagName(String tagName);

    /**
     * 转换标签字符串为实体集合
     *
     * @param tagList tagList
     * @return list
     */
    List<Tag> strListToTagList(String tagList);
}
