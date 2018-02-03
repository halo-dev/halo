package cc.ryanc.halo.service;

import cc.ryanc.halo.model.domain.Tag;

import java.util.List;

/**
 * @author : RYAN0UP
 * @version : 1.0
 * @date : 2018/1/12
 * description :
 */
public interface TagService {

    /**
     * 新增标签
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
     * 更新标签
     *
     * @param tag tag
     * @return tag
     */
    Tag updateByTag(Tag tag);

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
    Tag findByTagId(Long tagId);

    /**
     * 根据标签路径查询
     *
     * @param tagUrl tagUrl
     * @return tag
     */
    Tag findByTagUrl(String tagUrl);
}
