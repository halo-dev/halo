package cc.ryanc.halo.service;

import cc.ryanc.halo.model.domain.Tag;
import cc.ryanc.halo.service.base.CrudService;

import java.util.List;

/**
 * <pre>
 *     标签业务逻辑接口
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2018/1/12
 */
public interface TagService extends CrudService<Tag, Long> {

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
