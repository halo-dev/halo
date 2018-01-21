package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.domain.Tag;
import cc.ryanc.halo.repository.TagRepository;
import cc.ryanc.halo.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author : RYAN0UP
 * @version : 1.0
 * @date : 2018/1/12
 * description :
 */
@Service
public class TagServiceImpl implements TagService {

    @Autowired
    private TagRepository tagRepository;

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
    public Tag removeByTagId(Integer tagId) {
        Tag tag = findByTagId(tagId);
        tagRepository.delete(tag);
        return tag;
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
    public Tag findByTagId(Integer tagId) {
        return tagRepository.findOne(tagId);
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
