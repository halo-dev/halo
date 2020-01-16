package run.halo.app.service;

import run.halo.app.model.dto.StatisticDTO;
import run.halo.app.model.dto.StatisticWithUserDTO;

/**
 * Statistic service interface.
 *
 * @author ryanwang
 * @date 2019-12-16
 */
public interface StatisticService {

    /**
     * Get blog statistic.
     *
     * @return statistic dto.
     */
    StatisticDTO getStatistic();

    /**
     * Get blog statistic with user info.
     *
     * @return statistic with user info dto.
     */
    StatisticWithUserDTO getStatisticWithUser();
}
