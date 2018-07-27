package cc.ryanc.halo.service;

import cc.ryanc.halo.model.domain.Link;

import java.util.List;
import java.util.Optional;

/**
 * <pre>
 *     友情链接业务逻辑接口
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2017/11/14
 */
public interface LinkService {

    /**
     * 新增/修改友情链接
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
    Link removeByLinkId(Long linkId);

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
