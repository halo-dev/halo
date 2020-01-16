package run.halo.app.service;

import run.halo.app.model.enums.CommentViolationTypeEnum;

/**
 * Comment BlackList Service
 *
 * @author Lei XinXin
 * @date 2020/1/3
 */
public interface CommentBlackListService {
    /**
     * 评论封禁状态
     *
     * @param ipAddress ip地址
     * @return boolean
     */
    CommentViolationTypeEnum commentsBanStatus(String ipAddress);
}
