package run.halo.app.handler.migrate;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;
import run.halo.app.exception.ServiceException;
import run.halo.app.handler.migrate.converter.Converter;
import run.halo.app.handler.migrate.converter.WordPressConverter;
import run.halo.app.handler.migrate.support.vo.PostVO;
import run.halo.app.handler.migrate.support.wordpress.Rss;
import run.halo.app.model.enums.MigrateType;
import run.halo.app.service.*;
import run.halo.app.utils.XmlMigrateUtils;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * WordPress migrate handler
 *
 * @author ryanwang
 * @author guqing
 * @date 2019-10-28
 */
@Slf4j
@Component
@SuppressWarnings("unchecked")
public class WordPressMigrateHandler implements MigrateHandler {

    private final AttachmentService attachmentService;

    private final PostService postService;

    private final LinkService linkService;

    private final MenuService menuService;

    private final CategoryService categoryService;

    private final TagService tagService;

    private final PostCommentService postCommentService;

    private final SheetCommentService sheetCommentService;

    private final SheetService sheetService;

    private final PhotoService photoService;

    private final PostCategoryService postCategoryService;

    private final PostTagService postTagService;

    public WordPressMigrateHandler(AttachmentService attachmentService,
                                   PostService postService,
                                   LinkService linkService,
                                   MenuService menuService,
                                   CategoryService categoryService,
                                   TagService tagService,
                                   PostCommentService postCommentService,
                                   SheetCommentService sheetCommentService,
                                   SheetService sheetService,
                                   PhotoService photoService,
                                   PostCategoryService postCategoryService,
                                   PostTagService postTagService) {
        this.attachmentService = attachmentService;
        this.postService = postService;
        this.linkService = linkService;
        this.menuService = menuService;
        this.categoryService = categoryService;
        this.tagService = tagService;
        this.postCommentService = postCommentService;
        this.sheetCommentService = sheetCommentService;
        this.sheetService = sheetService;
        this.photoService = photoService;
        this.postCategoryService = postCategoryService;
        this.postTagService = postTagService;
    }

    @Override
    public void migrate(MultipartFile file) {
        try {
            String migrationContent = FileCopyUtils.copyToString(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
            String jsonString = XmlMigrateUtils.xml2jsonString(migrationContent);
            JSONObject json = JSONObject.parseObject(jsonString);
            Rss rss = json.getObject("rss", Rss.class);

            // 转换
            Converter<Rss, List<PostVO>> converter = new WordPressConverter();

            List<PostVO> postVoList = converter.convertFrom(rss);

            log.debug("Migrated posts: [{}]", postVoList);
        } catch (Exception e) {
            throw new ServiceException("WordPress 入出文件 " + file.getOriginalFilename() + " 读取失败", e);
        }
    }

    @Override
    public boolean supportType(MigrateType type) {
        return MigrateType.WORDPRESS.equals(type);
    }
}
