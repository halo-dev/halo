package run.halo.app.theme.finders.impl;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Counter;
import run.halo.app.core.extension.content.Category;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.theme.finders.Finder;
import run.halo.app.theme.finders.SiteStatsFinder;
import run.halo.app.theme.finders.vo.SiteStatsVo;

/**
 * A default implementation of {@link SiteStatsFinder}.
 *
 * @author guqing
 * @since 2.0.0
 */
@AllArgsConstructor
@Finder("siteStatsFinder")
public class SiteStatsFinderImpl implements SiteStatsFinder {
    private final ReactiveExtensionClient client;

    @Override
    public Mono<SiteStatsVo> getStats() {
        return client.list(Counter.class, null, null)
            .reduce(SiteStatsVo.empty(), (stats, counter) -> {
                stats.setVisit(stats.getVisit() + counter.getVisit());
                stats.setComment(stats.getComment() + counter.getApprovedComment());
                stats.setUpvote(stats.getUpvote() + counter.getUpvote());
                return stats;
            })
            .flatMap(siteStatsVo -> postCount()
                .doOnNext(siteStatsVo::setPost)
                .thenReturn(siteStatsVo)
            )
            .flatMap(siteStatsVo -> categoryCount()
                .doOnNext(siteStatsVo::setCategory)
                .thenReturn(siteStatsVo));
    }

    Mono<Integer> postCount() {
        return client.list(Post.class, post -> !post.isDeleted() && post.isPublished(), null)
            .count()
            .map(Long::intValue);
    }

    Mono<Integer> categoryCount() {
        return client.list(Category.class, null, null)
            .count()
            .map(Long::intValue);
    }

}
