package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.domain.Link;
import cc.ryanc.halo.repository.LinkRepository;
import cc.ryanc.halo.service.LinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * <pre>
 *     友情链接业务逻辑实现类
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2017/11/14
 */
@Service
public class LinkServiceImpl implements LinkService {

    private static final String LINKS_CACHE_KEY = "'link'";

    private static final String LINKS_CACHE_NAME = "links";

    @Autowired
    private LinkRepository linkRepository;

    /**
     * 新增/修改友情链接
     *
     * @param link link
     * @return Link
     */
    @Override
    @CacheEvict(value = LINKS_CACHE_NAME, allEntries = true, beforeInvocation = true)
    public Link saveByLink(Link link) {
        return linkRepository.save(link);
    }

    /**
     * 移除友情链接
     *
     * @param linkId linkId
     * @return Link
     */
    @Override
    @CacheEvict(value = LINKS_CACHE_NAME, allEntries = true, beforeInvocation = true)
    public Link removeByLinkId(Long linkId) {
        Optional<Link> link = this.findByLinkId(linkId);
        linkRepository.delete(link.get());
        return link.get();
    }

    /**
     * 查询所有友情链接
     *
     * @return List
     */
    @Override
    @Cacheable(value = LINKS_CACHE_NAME, key = LINKS_CACHE_KEY)
    public List<Link> findAllLinks() {
        return linkRepository.findAll();
    }

    /**
     * 根据编号查询友情链接
     *
     * @param linkId linkId
     * @return Optional
     */
    @Override
    public Optional<Link> findByLinkId(Long linkId) {
        return linkRepository.findById(linkId);
    }
}
