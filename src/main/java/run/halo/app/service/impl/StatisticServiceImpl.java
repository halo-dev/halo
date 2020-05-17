package run.halo.app.service.impl;

import org.springframework.stereotype.Service;
import run.halo.app.exception.ServiceException;
import run.halo.app.model.dto.StatisticDTO;
import run.halo.app.model.dto.StatisticWithUserDTO;
import run.halo.app.model.dto.UserDTO;
import run.halo.app.model.entity.User;
import run.halo.app.model.enums.CommentStatus;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.projection.VisitorLogDayCountProjection;
import run.halo.app.model.projection.VisitorLogMonthCountProjection;
import run.halo.app.service.*;
import run.halo.app.utils.DateUtils;

import java.util.Date;
import java.util.List;

/**
 * Statistic service implementation.
 *
 * @author ryanwang
 * @date 2019-12-16
 */
@Service
public class StatisticServiceImpl implements StatisticService {

    private final PostService postService;

    private final SheetService sheetService;

    private final JournalService journalService;

    private final PostCommentService postCommentService;

    private final SheetCommentService sheetCommentService;

    private final JournalCommentService journalCommentService;

    private final OptionService optionService;

    private final LinkService linkService;

    private final CategoryService categoryService;

    private final TagService tagService;

    private final UserService userService;

    private final VisitorLogService visitorLogService;

    public StatisticServiceImpl(PostService postService,
                                SheetService sheetService,
                                JournalService journalService,
                                PostCommentService postCommentService,
                                SheetCommentService sheetCommentService,
                                JournalCommentService journalCommentService,
                                OptionService optionService,
                                LinkService linkService,
                                CategoryService categoryService,
                                TagService tagService,
                                UserService userService,
                                VisitorLogService visitorLogService) {
        this.postService = postService;
        this.sheetService = sheetService;
        this.journalService = journalService;
        this.postCommentService = postCommentService;
        this.sheetCommentService = sheetCommentService;
        this.journalCommentService = journalCommentService;
        this.optionService = optionService;
        this.linkService = linkService;
        this.categoryService = categoryService;
        this.tagService = tagService;
        this.userService = userService;
        this.visitorLogService = visitorLogService;
    }

    @Override
    public StatisticDTO getStatistic() {
        StatisticDTO statisticDTO = new StatisticDTO();
        statisticDTO.setPostCount(postService.countByStatus(PostStatus.PUBLISHED));

        // Handle comment count
        long postCommentCount = postCommentService.countByStatus(CommentStatus.PUBLISHED);
        long sheetCommentCount = sheetCommentService.countByStatus(CommentStatus.PUBLISHED);
        long journalCommentCount = journalCommentService.countByStatus(CommentStatus.PUBLISHED);

        statisticDTO.setCommentCount(postCommentCount + sheetCommentCount + journalCommentCount);
        statisticDTO.setTagCount(tagService.count());
        statisticDTO.setCategoryCount(categoryService.count());
        statisticDTO.setJournalCount(journalService.count());

        long birthday = optionService.getBirthday();
        long days = (System.currentTimeMillis() - birthday) / (1000 * 24 * 3600);
        statisticDTO.setEstablishDays(days);
        statisticDTO.setBirthday(birthday);

        statisticDTO.setLinkCount(linkService.count());
        statisticDTO.setVisitCount(postService.countVisit() + sheetService.countVisit());
        statisticDTO.setLikeCount(postService.countLike() + sheetService.countLike());

        List<VisitorLogDayCountProjection> countToday = visitorLogService.getVisitCountByDay(0);
        Date today = DateUtils.getStartTimeOfDay(DateUtils.now());
        if (!countToday.isEmpty()) {
            statisticDTO.setVisitCountToday(countToday.get(0));
        } else {
            statisticDTO.setVisitCountToday(new VisitorLogDayCountProjection(today, (Long) 0L));
        }

        List<VisitorLogMonthCountProjection>  countCurrentMonth = visitorLogService.getVisitCountByMonth(0);
        if (!countCurrentMonth.isEmpty()) {
            statisticDTO.setVisitCountCurrentMonth(countCurrentMonth.get(0));
        } else {
            VisitorLogMonthCountProjection v = new VisitorLogMonthCountProjection();
            v.setMonth(DateUtils.getMonth(today));
            v.setCount((Long) 0L);
            statisticDTO.setVisitCountCurrentMonth(v);
        }
        return statisticDTO;
    }

    @Override
    public StatisticWithUserDTO getStatisticWithUser() {

        StatisticDTO statisticDTO = getStatistic();

        StatisticWithUserDTO statisticWithUserDTO = new StatisticWithUserDTO();
        statisticWithUserDTO.convertFrom(statisticDTO);

        User user = userService.getCurrentUser().orElseThrow(() -> new ServiceException("未查询到博主信息"));
        statisticWithUserDTO.setUser(new UserDTO().convertFrom(user));

        return statisticWithUserDTO;
    }
}
