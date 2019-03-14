package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.domain.Tag;
import cc.ryanc.halo.repository.TagRepository;
import cc.ryanc.halo.service.TagService;
import cc.ryanc.halo.service.base.AbstractCrudService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 *     标签业务逻辑实现类
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2018/1/12
 */
@Service
public class TagServiceImpl extends AbstractCrudService<Tag, Long> implements TagService {

    private static final String POSTS_CACHE_NAME = "posts";

    private final TagRepository tagRepository;

    public TagServiceImpl(TagRepository tagRepository) {
        super(tagRepository);
        this.tagRepository = tagRepository;
    }

    /**
     * 新增/修改标签
     *
     * @param tag tag
     * @return Tag
     */
    @Override
    @CacheEvict(value = POSTS_CACHE_NAME, allEntries = true, beforeInvocation = true)
    public Tag create(Tag tag) {
        return super.create(tag);
    }

    /**
     * 根据编号移除标签
     *
     * @param tagId tagId
     * @return Tag
     */
    @Override
    @CacheEvict(value = POSTS_CACHE_NAME, allEntries = true, beforeInvocation = true)
    public Tag removeById(Long tagId) {
        return super.removeById(tagId);
    }

    /**
     * 根据标签路径查询
     *
     * @param tagUrl tagUrl
     * @return Tag
     */
    @Override
    public Tag findByTagUrl(String tagUrl) {
        return tagRepository.findTagByTagUrl(tagUrl);
    }

    /**
     * 根据标签名称查询
     *
     * @param tagName tagName
     * @return Tag
     */
    @Override
    public Tag findTagByTagName(String tagName) {
        return tagRepository.findTagByTagName(tagName);
    }

    /**
     * 转换标签字符串为实体集合
     *
     * @param tagList tagList
     * @return List
     */
    @Override
    public List<Tag> strListToTagList(String tagList) {
        final String[] tags = tagList.split(",");
        final List<Tag> tagsList = new ArrayList<>();
        for (String tag : tags) {
            final Tag t = findTagByTagName(tag);
            Tag nt = null;
            if (null != t) {
                tagsList.add(t);
            } else {
                nt = new Tag();
                nt.setTagName(tag);
                nt.setTagUrl(tag);
                tagsList.add(create(nt));
            }
        }
        return tagsList;
    }
}
