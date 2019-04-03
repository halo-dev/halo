package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.exception.AlreadyExistsException;
import cc.ryanc.halo.model.dto.LinkOutputDTO;
import cc.ryanc.halo.model.entity.Link;
import cc.ryanc.halo.model.params.LinkParam;
import cc.ryanc.halo.model.vo.LinkTeamVO;
import cc.ryanc.halo.repository.LinkRepository;
import cc.ryanc.halo.service.LinkService;
import cc.ryanc.halo.service.base.AbstractCrudService;
import cc.ryanc.halo.utils.ServiceUtils;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;
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

        return convertTo(listAll(sort));
    }

    /**
     * List link by group
     *
     * @return List<LinkTeamVO>
     */
    @Override
    public List<LinkTeamVO> listTeamVos() {
        // TODO list team
        return null;
    }

    @Override
    public List<LinkTeamVO> listTeamVos(Sort sort) {
        Assert.notNull(sort, "Sort info must not be null");

        // List all links
        List<LinkOutputDTO> links = listDtos(sort);

        // Get teams
        Set<String> teams = ServiceUtils.fetchProperty(links, LinkOutputDTO::getTeam);

        // Convert to team link list map (Key: team, value: link list)
        Map<String, List<LinkOutputDTO>> teamLinkListMap = ServiceUtils.convertToListMap(teams, links, LinkOutputDTO::getTeam);

        List<LinkTeamVO> result = new LinkedList<>();

        // Wrap link team vo list
        teamLinkListMap.forEach((team, linkList) -> {
            // Build link team vo
            LinkTeamVO linkTeamVO = new LinkTeamVO();
            linkTeamVO.setTeam(team);
            linkTeamVO.setLinks(linkList);

            // Add it to result
            result.add(linkTeamVO);
        });

        return result;
    }

    @Override
    public Link createBy(LinkParam linkParam) {
        Assert.notNull(linkParam, "Link param must not be null");

        // Check the name
        boolean exist = existByName(linkParam.getName());

        if (exist) {
            throw new AlreadyExistsException("Link name " + linkParam.getName() + " has already existed").setErrorData(linkParam.getName());
        }

        return create(linkParam.convertTo());
    }

    @Override
    public boolean existByName(String name) {
        Assert.hasText(name, "Link name must not be blank");
        Link link = new Link();
        link.setName(name);

        return linkRepository.exists(Example.of(link));
    }

    @NonNull
    private List<LinkOutputDTO> convertTo(@Nullable List<Link> links) {
        if (CollectionUtils.isEmpty(links)) {
            return Collections.emptyList();
        }

        return links.stream().map(link -> new LinkOutputDTO().<LinkOutputDTO>convertFrom(link))
                .collect(Collectors.toList());
    }
}
