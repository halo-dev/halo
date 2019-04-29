package run.halo.app.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import run.halo.app.cache.StringCacheStore;
import run.halo.app.exception.BadRequestException;
import run.halo.app.model.dto.CountDTO;
import run.halo.app.model.enums.CommentStatus;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.params.LoginParam;
import run.halo.app.security.context.SecurityContextHolder;
import run.halo.app.security.token.AuthToken;
import run.halo.app.service.*;

/**
 * Admin service implementation.
 *
 * @author johnniang
 * @date 19-4-29
 */
@Slf4j
@Service
public class AdminServiceImpl implements AdminService {

    private final PostService postService;

    private final SheetService sheetService;

    private final AttachmentService attachmentService;

    private final PostCommentService postCommentService;

    private final SheetCommentService sheetCommentService;

    private final JournalCommentService journalCommentService;

    private final OptionService optionService;

    private final UserService userService;

    private final LinkService linkService;

    private final StringCacheStore cacheStore;

    public AdminServiceImpl(PostService postService,
                            SheetService sheetService,
                            AttachmentService attachmentService,
                            PostCommentService postCommentService,
                            SheetCommentService sheetCommentService,
                            JournalCommentService journalCommentService,
                            OptionService optionService,
                            UserService userService,
                            LinkService linkService,
                            StringCacheStore cacheStore) {
        this.postService = postService;
        this.sheetService = sheetService;
        this.attachmentService = attachmentService;
        this.postCommentService = postCommentService;
        this.sheetCommentService = sheetCommentService;
        this.journalCommentService = journalCommentService;
        this.optionService = optionService;
        this.userService = userService;
        this.linkService = linkService;
        this.cacheStore = cacheStore;
    }

    @Override
    public AuthToken authenticate(LoginParam loginParam) {
        Assert.notNull(loginParam, "Login param must not be null");

        return null;
    }

    @Override
    public void clearAuthentication() {
        // Check if the current is logging in
        boolean authenticated = SecurityContextHolder.getContext().isAuthenticated();

        if (!authenticated) {
            throw new BadRequestException("You haven't logged in yet, so you can't log out");
        }

        log.info("You have been logged out, looking forward to your next visit!");
    }

    @Override
    public CountDTO getCount() {
        CountDTO countDTO = new CountDTO();
        countDTO.setPostCount(postService.countByStatus(PostStatus.PUBLISHED));
        countDTO.setAttachmentCount(attachmentService.count());

        // Handle comment count
        long postCommentCount = postCommentService.countByStatus(CommentStatus.PUBLISHED);
        long sheetCommentCount = sheetCommentService.countByStatus(CommentStatus.PUBLISHED);
        long journalCommentCount = journalCommentService.countByStatus(CommentStatus.PUBLISHED);

        countDTO.setCommentCount(postCommentCount + sheetCommentCount + journalCommentCount);

        long birthday = optionService.getBirthday();
        long days = (System.currentTimeMillis() - birthday) / (1000 * 24 * 3600);
        countDTO.setEstablishDays(days);

        countDTO.setLinkCount(linkService.count());

        countDTO.setVisitCount(postService.countVisit() + sheetService.countVisit());
        countDTO.setLikeCount(postService.countLike() + sheetService.countLike());
        return countDTO;
    }
}
