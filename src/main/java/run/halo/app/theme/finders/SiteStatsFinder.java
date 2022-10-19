package run.halo.app.theme.finders;

import run.halo.app.theme.finders.vo.SiteStatsVo;

/**
 * Site statistics finder.
 *
 * @author guqing
 * @since 2.0.0
 */
public interface SiteStatsFinder {

    SiteStatsVo getStats();
}
