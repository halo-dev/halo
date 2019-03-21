package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.dto.LinkOutputDTO;
import cc.ryanc.halo.model.entity.Link;
import cc.ryanc.halo.repository.LinkRepository;
import cc.ryanc.halo.service.LinkService;
import cc.ryanc.halo.service.base.AbstractCrudService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Collectors;

/**
 * LinkService implementation class
 *
 * @author : RYAN0UP
 * @date : 2019-03-14
 */
@Service
public class LinkServiceImpl extends AbstractCrudService<Link, Integer> implements LinkService {

    private final LinkRepository linkRepository;

    public LinkServiceImpl(LinkRepository linkRepository) {
        super(linkRepository);
        this.linkRepository = linkRepository;
    }

    /**
     * List link dtos.
     *
     * @param sort sort
     * @return all links
     */
    @Override
    public List<LinkOutputDTO> listDtos(Sort sort) {
        Assert.notNull(sort, "Sort info must not be null");

        return listAll(sort).stream().map(link -> (LinkOutputDTO) new LinkOutputDTO().convertFrom(link)).collect(Collectors.toList());
    }
}
