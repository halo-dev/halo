package run.halo.app.service.impl;

import org.springframework.stereotype.Service;
import run.halo.app.service.*;

/**
 * DataProcessService implementation.
 *
 * @author ryanwang
 * @date 2019-12-29
 */
@Service
public class DataProcessServiceImpl implements DataProcessService {

    private final PostService postService;

    private final SheetService sheetService;

    private final PostCommentService postCommentService;

    private final SheetCommentService sheetCommentService;

    private final JournalCommentService journalCommentService;

    private final AttachmentService attachmentService;

    private final OptionService optionService;

    private final PhotoService photoService;

    private final ThemeSettingService themeSettingService;

    public DataProcessServiceImpl(PostService postService,
                                  SheetService sheetService,
                                  PostCommentService postCommentService,
                                  SheetCommentService sheetCommentService,
                                  JournalCommentService journalCommentService,
                                  AttachmentService attachmentService,
                                  OptionService optionService,
                                  PhotoService photoService,
                                  ThemeSettingService themeSettingService) {
        this.postService = postService;
        this.sheetService = sheetService;
        this.postCommentService = postCommentService;
        this.sheetCommentService = sheetCommentService;
        this.journalCommentService = journalCommentService;
        this.attachmentService = attachmentService;
        this.optionService = optionService;
        this.photoService = photoService;
        this.themeSettingService = themeSettingService;
    }

    @Override
    public void replaceAllUrl(String oldUrl, String newUrl) {
        postService.replaceUrl(oldUrl, newUrl);
        sheetService.replaceUrl(oldUrl, newUrl);
        postCommentService.replaceUrl(oldUrl, newUrl);
        sheetCommentService.replaceUrl(oldUrl, newUrl);
        journalCommentService.replaceUrl(oldUrl, newUrl);
        attachmentService.replaceUrl(oldUrl, newUrl);
        optionService.replaceUrl(oldUrl, newUrl);
        photoService.replaceUrl(oldUrl, newUrl);
        themeSettingService.replaceUrl(oldUrl, newUrl);
    }
}
