package cc.ryanc.halo.service;

import cc.ryanc.halo.model.domain.Link;

import java.util.List;
import java.util.Optional;

/**
 * @author : RYAN0UP
 * @date : 2017/11/14
 * @version : 1.0
 * description:
 */
public interface LinkService {
    /**
     * 新增友情链接
     *
     * @param link link
     * @return Link
     */
    Link saveByLink(Link link);

    /**
     * 根据编号删除
     *
     * @param linkId linkId
     * @return Link
     */
    Optional<Link> removeByLinkId(Long linkId);

    /**
     * 修改
     *
     * @param link link
     * @return Link
     */
    Link updateByLink(Link link);

    /**
     * 查询所有
     *
     * @return List
     */
    List<Link> findAllLinks();

    /**
     * 根据编号查询单个链接
     *
     * @param linkId linkId
     * @return Link
     */
    Optional<Link> findByLinkId(Long linkId);
}
