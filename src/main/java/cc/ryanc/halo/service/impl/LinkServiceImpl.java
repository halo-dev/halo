package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.domain.Link;
import cc.ryanc.halo.repository.LinkRepository;
import cc.ryanc.halo.service.LinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @author : RYAN0UP
 * @date : 2017/11/14
 */
@Service
public class LinkServiceImpl implements LinkService {

    @Autowired
    private LinkRepository linkRepository;

    /**
     * 新增/修改友情链接
     *
     * @param link link
     * @return Link
     */
    @Override
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
