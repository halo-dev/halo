package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.entity.Link;
import cc.ryanc.halo.repository.LinkRepository;
import cc.ryanc.halo.service.LinkService;
import cc.ryanc.halo.service.base.AbstractCrudService;
import org.springframework.stereotype.Service;

/**
 * LinkService implementation class
 *
 * @author : RYAN0UP
 * @date : 2019-03-14
 */
@Service
public class LinkServiceImpl extends AbstractCrudService<Link, Integer> implements LinkService {

    private LinkRepository linkRepository;

    public LinkServiceImpl(LinkRepository linkRepository) {
        super(linkRepository);
        this.linkRepository = linkRepository;
    }
}
