package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.domain.Link;
import cc.ryanc.halo.repository.LinkRepository;
import cc.ryanc.halo.service.LinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @author : RYAN0UP
 * @date : 2017/11/14
 * @version : 1.0
 * description:
 */
@Service
public class LinkServiceImpl implements LinkService {

    @Autowired
    private LinkRepository linkRepository;

    private static final String LINK_KEY = "'link_key'";

    private static final String LINK_CACHE_NAME = "link_cache";

    /**
     * 保存友情链接 清除缓存
     *
     * @param link link
     * @return Link
     */
    @CacheEvict(value = LINK_CACHE_NAME,key = LINK_KEY)
    @Override
    public Link saveByLink(Link link) {
        return linkRepository.save(link);
    }

    /**
     * 移除友情链接 清除缓存
     *
     * @param linkId linkId
     * @return link
     */
    @CacheEvict(value = LINK_CACHE_NAME,key = LINK_KEY)
    @Override
    public Optional<Link> removeByLinkId(Long linkId) {
        Optional<Link> link = this.findByLinkId(linkId);
        linkRepository.delete(link.get());
        return link;
    }

    /**
     * 修改友情链接 清除缓存
     *
     * @param link link
     * @return Link
     */
    @CachePut(value = LINK_CACHE_NAME,key = "#link.linkId+'link'")
    @CacheEvict(value = LINK_CACHE_NAME,key = LINK_KEY)
    @Override
    public Link updateByLink(Link link) {
        return linkRepository.save(link);
    }

    /**
     * 查询所有友情链接 缓存
     *
     * @return list
     */
    @Cacheable(value = LINK_CACHE_NAME,key = LINK_KEY)
    @Override
    public List<Link> findAllLinks() {
        return linkRepository.findAll();
    }

    /**
     * 根据编号查询友情链接 缓存
     *
     * @param linkId linkId
     * @return Link
     */
    @Cacheable(value = LINK_CACHE_NAME,key = "#linkId+'link'")
    @Override
    public Optional<Link> findByLinkId(Long linkId) {
        return linkRepository.findById(linkId);
    }
}
