package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.domain.Tag;
import cc.ryanc.halo.repository.TagRepository;
import cc.ryanc.halo.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @author : RYAN0UP
 * @date : 2018/1/12
 * @version : 1.0
 * description :
 */
@Service
public class TagServiceImpl implements TagService {

    @Autowired
    private TagRepository tagRepository;

    private static final String CATEGORY_KEY = "'category_key'";

    private static final String CATEGORY_CACHE_NAME = "cateCache";

    /**
     * 新增标签
     *
     * @param tag tag
     * @return Tag
     */
    @Override
    public Tag saveByTag(Tag tag) {
        return tagRepository.save(tag);
    }

    /**
     * 根据编号移除标签
     *
     * @param tagId tagId
     * @return Tag
     */
    @Override
    public Tag removeByTagId(Long tagId) {
        Optional<Tag> tag = findByTagId(tagId);
        tagRepository.delete(tag.get());
        return tag.get();
    }

    /**
     * 更新标签
     *
     * @param tag tag
     * @return tag
     */
    @Override
    public Tag updateByTag(Tag tag) {
        return tagRepository.save(tag);
    }

    /**
     * 获取所有标签
     *
     * @return list
     */
    @Override
    public List<Tag> findAllTags() {
        return tagRepository.findAll();
    }

    /**
     * 根据编号查询标签
     *
     * @param tagId tagId
     * @return Link
     */
    @Override
    public Optional<Tag> findByTagId(Long tagId) {
        return tagRepository.findById(tagId);
    }

    /**
     * 根据标签路径查询
     *
     * @param tagUrl tagUrl
     * @return tag
     */
    @Override
    public Tag findByTagUrl(String tagUrl) {
        return tagRepository.findTagByTagUrl(tagUrl);
    }
}
