package run.halo.app.task;

import cn.hutool.core.date.DateUtil;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import run.halo.app.model.entity.BasePost;
import run.halo.app.model.entity.Post;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.enums.TimeUnit;
import run.halo.app.model.properties.PostProperties;
import run.halo.app.service.OptionService;
import run.halo.app.service.PostService;

/**
 * @author Wh1te
 * @date 2020-10-19
 */
@Slf4j
@Component
public class RecycledPostCleaningTask {

    private final OptionService optionService;

    private final PostService postService;

    public RecycledPostCleaningTask(OptionService optionService, PostService postService) {
        this.optionService = optionService;
        this.postService = postService;
    }

    /**
     * Clean recycled posts if RECYCLED_POST_CLEANING_ENABLED is true
     */
    @Scheduled(cron = "0 0 */1 * * ?")
    public synchronized void run() {
        Boolean recycledPostCleaningEnabled = optionService
            .getByPropertyOrDefault(PostProperties.RECYCLED_POST_CLEANING_ENABLED, Boolean.class,
                false);
        log.debug("{} = {}", PostProperties.RECYCLED_POST_CLEANING_ENABLED.getValue(),
            recycledPostCleaningEnabled);
        if (!recycledPostCleaningEnabled) {
            return;
        }

        Integer recycledPostRetentionTime = optionService
            .getByPropertyOrDefault(PostProperties.RECYCLED_POST_RETENTION_TIME, Integer.class,
                PostProperties.RECYCLED_POST_RETENTION_TIME.defaultValue(Integer.class));
        TimeUnit timeUnit = optionService
            .getEnumByPropertyOrDefault(PostProperties.RECYCLED_POST_RETENTION_TIMEUNIT,
                TimeUnit.class, TimeUnit.DAY);
        log.debug("{} = {}", PostProperties.RECYCLED_POST_RETENTION_TIME.getValue(),
            recycledPostRetentionTime);
        log.debug("{} = {}", PostProperties.RECYCLED_POST_RETENTION_TIMEUNIT.getValue(),
            Objects.requireNonNull(timeUnit).name());

        long expiredIn;
        switch (timeUnit) {
            case HOUR:
                expiredIn = recycledPostRetentionTime;
                break;
            case DAY:
            default:
                expiredIn = recycledPostRetentionTime * 24;
                break;
        }
        List<Post> recyclePost = postService.listAllBy(PostStatus.RECYCLE);
        LocalDateTime now = LocalDateTime.now();
        List<Integer> ids = recyclePost.stream().filter(post -> {
            LocalDateTime updateTime = DateUtil.toLocalDateTime(post.getUpdateTime());
            long until = updateTime.until(now, ChronoUnit.HOURS);
            return until >= expiredIn;
        }).map(BasePost::getId).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(ids)) {
            return;
        }

        log.info("Start cleaning recycled posts");
        List<Post> posts = postService.removeByIds(ids);
        log.info(
            "Recycled posts cleaning has been completed, {} posts has been permanently deleted",
            posts.size());
    }

}
