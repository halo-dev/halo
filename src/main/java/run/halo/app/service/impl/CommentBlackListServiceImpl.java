package run.halo.app.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import run.halo.app.model.entity.CommentBlackList;
import run.halo.app.model.enums.CommentViolationTypeEnum;
import run.halo.app.model.properties.CommentProperties;
import run.halo.app.repository.CommentBlackListRepository;
import run.halo.app.repository.PostCommentRepository;
import run.halo.app.service.CommentBlackListService;
import run.halo.app.service.OptionService;
import run.halo.app.service.base.AbstractCrudService;
import run.halo.app.utils.DateTimeUtils;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

/**
 * Comment BlackList Service Implements
 *
 * @author Lei XinXin
 * @date 2020/1/3
 */

@Service
@Slf4j
public class CommentBlackListServiceImpl extends AbstractCrudService<CommentBlackList, Long> implements CommentBlackListService {
    private final CommentBlackListRepository commentBlackListRepository;
    private final PostCommentRepository postCommentRepository;
    private final OptionService optionService;


    public CommentBlackListServiceImpl(CommentBlackListRepository commentBlackListRepository, PostCommentRepository postCommentRepository, OptionService optionService) {
        super(commentBlackListRepository);
        this.commentBlackListRepository = commentBlackListRepository;
        this.postCommentRepository = postCommentRepository;
        this.optionService = optionService;
    }

    @Override
    public CommentViolationTypeEnum commentsBanStatus(String ipAddress) {
        /*
        N=后期可配置
        1. 获取评论次数；
        2. 判断N分钟内，是否超过规定的次数限制，超过后需要每隔N分钟才能再次评论；
        3. 如果在时隔N分钟内，还有多次评论，可被认定为恶意攻击者；
        4. 对恶意攻击者进行N分钟的封禁；
        */
        Optional<CommentBlackList> blackList = commentBlackListRepository.findByIpAddress(ipAddress);
        LocalDateTime now = LocalDateTime.now();
        Date endTime = new Date(DateTimeUtils.toEpochMilli(now));
        Integer banTime = optionService.getByPropertyOrDefault(CommentProperties.COMMENT_BAN_TIME, Integer.class, 10);
        Date startTime = new Date(DateTimeUtils.toEpochMilli(now.minusMinutes(banTime)));
        Integer range = optionService.getByPropertyOrDefault(CommentProperties.COMMENT_RANGE, Integer.class, 30);
        boolean isPresent = postCommentRepository.countByIpAndTime(ipAddress, startTime, endTime) >= range;
        if (isPresent && blackList.isPresent()) {
            update(now, blackList.get(), banTime);
            return CommentViolationTypeEnum.FREQUENTLY;
        } else if (isPresent) {
            CommentBlackList commentBlackList = CommentBlackList
                .builder()
                .banTime(getBanTime(now, banTime))
                .ipAddress(ipAddress)
                .build();
            super.create(commentBlackList);
            return CommentViolationTypeEnum.FREQUENTLY;
        }
        return CommentViolationTypeEnum.NORMAL;
    }

    private void update(LocalDateTime localDateTime, CommentBlackList blackList, Integer banTime) {
        blackList.setBanTime(getBanTime(localDateTime, banTime));
        int updateResult = commentBlackListRepository.updateByIpAddress(blackList);
        Optional.of(updateResult)
            .filter(result -> result <= 0).ifPresent(result -> log.error("更新评论封禁时间失败"));
    }

    private Date getBanTime(LocalDateTime localDateTime, Integer banTime) {
        return new Date(DateTimeUtils.toEpochMilli(localDateTime.plusMinutes(banTime)));
    }
}
