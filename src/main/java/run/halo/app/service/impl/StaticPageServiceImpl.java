package run.halo.app.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.PageUtil;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.Assert;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import run.halo.app.config.properties.HaloProperties;
import run.halo.app.exception.ServiceException;
import run.halo.app.handler.staticdeploy.StaticDeployHandlers;
import run.halo.app.handler.theme.config.support.ThemeProperty;
import run.halo.app.model.dto.PhotoDTO;
import run.halo.app.model.entity.*;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.model.enums.StaticDeployType;
import run.halo.app.model.properties.PostProperties;
import run.halo.app.model.properties.StaticDeployProperties;
import run.halo.app.model.support.HaloConst;
import run.halo.app.model.support.StaticPageFile;
import run.halo.app.model.vo.AdjacentPostVO;
import run.halo.app.model.vo.PostDetailVO;
import run.halo.app.model.vo.PostListVO;
import run.halo.app.model.vo.SheetDetailVO;
import run.halo.app.service.*;
import run.halo.app.utils.DateTimeUtils;
import run.halo.app.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * Static Page service implementation.
 *
 * @author ryanwang
 * @date 2019-12-25
 */
@Slf4j
@Service
public class StaticPageServiceImpl implements StaticPageService {

    private final Path pagesDir;

    private final PostService postService;

    private final PostCategoryService postCategoryService;

    private final PostTagService postTagService;

    private final PostMetaService postMetaService;

    private final SheetService sheetService;

    private final CategoryService categoryService;

    private final TagService tagService;

    private final LinkService linkService;

    private final PhotoService photoService;

    private final JournalService journalService;

    private final ThemeService themeService;

    private final HaloProperties haloProperties;

    private final OptionService optionService;

    private final FreeMarkerConfigurer freeMarkerConfigurer;

    private final StaticDeployHandlers staticDeployHandlers;

    public StaticPageServiceImpl(PostService postService,
                                 PostCategoryService postCategoryService,
                                 PostTagService postTagService,
                                 PostMetaService postMetaService,
                                 SheetService sheetService,
                                 CategoryService categoryService,
                                 TagService tagService,
                                 LinkService linkService,
                                 PhotoService photoService,
                                 JournalService journalService,
                                 ThemeService themeService,
                                 HaloProperties haloProperties,
                                 OptionService optionService,
                                 FreeMarkerConfigurer freeMarkerConfigurer,
                                 StaticDeployHandlers staticDeployHandlers) throws IOException {
        this.postService = postService;
        this.postCategoryService = postCategoryService;
        this.postTagService = postTagService;
        this.postMetaService = postMetaService;
        this.sheetService = sheetService;
        this.categoryService = categoryService;
        this.tagService = tagService;
        this.linkService = linkService;
        this.photoService = photoService;
        this.journalService = journalService;
        this.themeService = themeService;
        this.haloProperties = haloProperties;
        this.optionService = optionService;
        this.freeMarkerConfigurer = freeMarkerConfigurer;
        this.staticDeployHandlers = staticDeployHandlers;

        pagesDir = Paths.get(haloProperties.getWorkDir(), PAGES_FOLDER);
        FileUtils.createIfAbsent(pagesDir);
        Files.createDirectories(Paths.get(STATIC_PAGE_PACK_DIR));
    }

    @Override
    public void generate() {
        try {
            this.cleanFolder();
            this.generateIndex(1);
            this.generatePost();
            this.generateArchives(1);
            this.generateSheet();
            this.generateLink();
            this.generatePhoto(1);
            this.generateCategories();
            this.generateTags();
            this.generateRss();
            this.generateAtom();
            this.generateSiteMapHtml();
            this.generateSiteMapXml();
            this.generateRobots();
            this.generateReadme();
            this.copyThemeFolder();
            this.copyUpload();
            this.copyStatic();
        } catch (Exception e) {
            throw new ServiceException("生成静态页面失败！", e);
        }
    }

    @Override
    public void deploy() {
        StaticDeployType type = getStaticDeployType();

        staticDeployHandlers.deploy(type);
    }

    @Override
    public Path zipStaticPagesDirectory() {
        try {
            String staticPagePackName = HaloConst.STATIC_PAGE_PACK_PREFIX +
                DateTimeUtils.format(LocalDateTime.now(), DateTimeUtils.HORIZONTAL_LINE_DATETIME_FORMATTER) +
                IdUtil.simpleUUID().hashCode() + ".zip";
            Path staticPageZipPath = Files.createFile(Paths.get(STATIC_PAGE_PACK_DIR, staticPagePackName));

            FileUtils.zip(pagesDir, staticPageZipPath);

            return staticPageZipPath;
        } catch (IOException e) {
            throw new ServiceException("Failed to zip static pages directory", e);
        }
    }

    @Override
    public List<StaticPageFile> listFile() {
        return listStaticPageFileTree(pagesDir);
    }

    @Nullable
    private List<StaticPageFile> listStaticPageFileTree(@NonNull Path topPath) {
        Assert.notNull(topPath, "Top path must not be null");

        if (!Files.isDirectory(topPath)) {
            return null;
        }

        try (Stream<Path> pathStream = Files.list(topPath)) {
            List<StaticPageFile> staticPageFiles = new LinkedList<>();

            pathStream.forEach(path -> {
                StaticPageFile staticPageFile = new StaticPageFile();
                staticPageFile.setId(IdUtil.fastSimpleUUID());
                staticPageFile.setName(path.getFileName().toString());
                staticPageFile.setIsFile(Files.isRegularFile(path));
                if (Files.isDirectory(path)) {
                    staticPageFile.setChildren(listStaticPageFileTree(path));
                }

                staticPageFiles.add(staticPageFile);
            });

            staticPageFiles.sort(new StaticPageFile());
            return staticPageFiles;
        } catch (IOException e) {
            throw new ServiceException("Failed to list sub files", e);
        }
    }

    /**
     * Clean static pages folder
     */
    private void cleanFolder() {
        FileUtils.deleteFolderQuietly(pagesDir);
    }

    /**
     * Generate index.html and page/{page}/index.html.
     *
     * @param page current page.
     * @throws IOException       IOException
     * @throws TemplateException TemplateException
     */
    private void generateIndex(int page) throws IOException, TemplateException {
        if (!themeService.templateExists("index.ftl")) {
            log.warn("index.ftl not found,skip!");
            return;
        }

        ModelMap model = new ModelMap();

        String indexSort = optionService.getByPropertyOfNonNull(PostProperties.INDEX_SORT).toString();
        int pageSize = optionService.getPostPageSize();
        Pageable pageable = PageRequest.of(page >= 1 ? page - 1 : page, pageSize, Sort.by(DESC, "topPriority").and(Sort.by(DESC, indexSort)));
        Page<Post> postPage = postService.pageBy(PostStatus.PUBLISHED, pageable);
        Page<PostListVO> posts = postService.convertToListVo(postPage);
        int[] rainbow = PageUtil.rainbow(page, posts.getTotalPages(), 3);

        model.addAttribute("is_index", true);
        model.addAttribute("posts", posts);
        model.addAttribute("rainbow", rainbow);

        Template template = freeMarkerConfigurer.getConfiguration().getTemplate(themeService.renderWithSuffix("index"));
        String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);

        FileWriter fileWriter;

        if (page == 1) {
            fileWriter = new FileWriter(getPageFile("index.html"), "UTF-8");
        } else {
            fileWriter = new FileWriter(getPageFile("page/" + page + "/index.html"), "UTF-8");
        }

        fileWriter.write(html);

        if (postPage.hasNext()) {
            generateIndex(postPage.getNumber() + 2);
            log.info("Generate page/{}/index.html", postPage.getNumber() + 2);
        }
    }

    /**
     * Generate archives/index.html and archives/page/{page}/index.html.
     *
     * @param page current page
     * @throws IOException       IOException
     * @throws TemplateException TemplateException
     */
    private void generateArchives(int page) throws IOException, TemplateException {

        if (!themeService.templateExists("archives.ftl")) {
            log.warn("archives.ftl not found,skip!");
            return;
        }

        ModelMap model = new ModelMap();

        Pageable pageable = PageRequest.of(page - 1, optionService.getPostPageSize(), Sort.by(DESC, "topPriority"));

        Page<Post> postPage = postService.pageBy(PostStatus.PUBLISHED, pageable);
        Page<PostListVO> postListVos = postService.convertToListVo(postPage);
        int[] pageRainbow = PageUtil.rainbow(page, postListVos.getTotalPages(), 3);

        model.addAttribute("is_archives", true);
        model.addAttribute("pageRainbow", pageRainbow);
        model.addAttribute("posts", postListVos);

        Template template = freeMarkerConfigurer.getConfiguration().getTemplate(themeService.renderWithSuffix("archives"));
        String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);

        FileWriter fileWriter;

        if (page == 1) {
            fileWriter = new FileWriter(getPageFile("archives/index.html"), "UTF-8");
        } else {
            fileWriter = new FileWriter(getPageFile("archives/page/" + page + "/index.html"), "UTF-8");
        }

        fileWriter.write(html);

        if (postPage.hasNext()) {
            generateArchives(postPage.getNumber() + 2);
            log.info("Generate page/{}/index.html", postPage.getNumber() + 2);
        }
    }

    /**
     * Generate archives/{slug}/index.html.
     *
     * @throws IOException       IOException
     * @throws TemplateException TemplateException
     */
    private void generatePost() throws IOException, TemplateException {

        if (!themeService.templateExists("post.ftl")) {
            log.warn("post.ftl not found,skip!");
            return;
        }

        List<Post> posts = postService.listAllBy(PostStatus.PUBLISHED);

        for (Post post : posts) {
            log.info("Generate archives/{}/index.html", post.getSlug());
            ModelMap model = new ModelMap();

            AdjacentPostVO adjacentPostVO = postService.getAdjacentPosts(post);
            adjacentPostVO.getOptionalPrevPost().ifPresent(prevPost -> model.addAttribute("prevPost", prevPost));
            adjacentPostVO.getOptionalNextPost().ifPresent(nextPost -> model.addAttribute("nextPost", nextPost));

            List<Category> categories = postCategoryService.listCategoriesBy(post.getId());
            List<Tag> tags = postTagService.listTagsBy(post.getId());
            List<PostMeta> metas = postMetaService.listBy(post.getId());

            model.addAttribute("is_post", true);
            model.addAttribute("post", postService.convertToDetailVo(post));
            model.addAttribute("categories", categories);
            model.addAttribute("tags", tags);
            model.addAttribute("metas", postMetaService.convertToMap(metas));

            Template template = freeMarkerConfigurer.getConfiguration().getTemplate(themeService.renderWithSuffix("post"));
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);

            FileWriter fileWriter = new FileWriter(getPageFile("archives/" + post.getSlug() + "/index.html"), "UTF-8");
            fileWriter.write(html);
            log.info("Generate archives/{}/index.html succeed.", post.getSlug());
        }
    }

    /**
     * Generate s/{slug}/index.html.
     *
     * @throws IOException       IOException
     * @throws TemplateException TemplateException
     */
    private void generateSheet() throws IOException, TemplateException {
        if (!themeService.templateExists("sheet.ftl")) {
            log.warn("sheet.ftl not found,skip!");
            return;
        }

        List<Sheet> sheets = sheetService.listAllBy(PostStatus.PUBLISHED);
        for (Sheet sheet : sheets) {
            log.info("Generate s/{}/index.html", sheet.getSlug());
            ModelMap model = new ModelMap();

            SheetDetailVO sheetDetailVO = sheetService.convertToDetailVo(sheet);
            model.addAttribute("sheet", sheetDetailVO);
            model.addAttribute("post", sheetDetailVO);
            model.addAttribute("is_sheet", true);

            String templateName = "sheet";

            if (themeService.templateExists(ThemeService.CUSTOM_SHEET_PREFIX + sheet.getTemplate() + HaloConst.SUFFIX_FTL)) {
                templateName = ThemeService.CUSTOM_SHEET_PREFIX + sheet.getTemplate();
            }

            Template template = freeMarkerConfigurer.getConfiguration().getTemplate(themeService.renderWithSuffix(templateName));
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);

            FileWriter fileWriter = new FileWriter(getPageFile("s/" + sheet.getSlug() + "/index.html"), "UTF-8");
            fileWriter.write(html);

            log.info("Generate s/{}/index.html succeed.", sheet.getSlug());
        }
    }

    /**
     * Generate links/index.html.
     *
     * @throws IOException       IOException
     * @throws TemplateException TemplateException
     */
    private void generateLink() throws IOException, TemplateException {
        log.info("Generate links.html");

        if (!themeService.templateExists("links.ftl")) {
            log.warn("links.ftl not found,skip!");
            return;
        }

        Template template = freeMarkerConfigurer.getConfiguration().getTemplate(themeService.renderWithSuffix("links"));
        String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, null);
        FileWriter fileWriter = new FileWriter(getPageFile("links/index.html"), "UTF-8");
        fileWriter.write(html);

        log.info("Generate links.html succeed.");
    }

    /**
     * Generate photos/index.html and photos/page/{page}/index.html.
     *
     * @throws IOException       IOException
     * @throws TemplateException TemplateException
     */
    private void generatePhoto(int page) throws IOException, TemplateException {
        log.info("Generate photos.html");

        if (!themeService.templateExists("photos.ftl")) {
            log.warn("photos.ftl not found,skip!");
            return;
        }

        ModelMap model = new ModelMap();

        Pageable pageable = PageRequest.of(page >= 1 ? page - 1 : page, 10, Sort.by(DESC, "createTime"));
        Page<PhotoDTO> photos = photoService.pageBy(pageable);

        model.addAttribute("photos", photos);

        FileWriter fileWriter;

        if (page == 1) {
            fileWriter = new FileWriter(getPageFile("photos/index.html"), "UTF-8");
        } else {
            fileWriter = new FileWriter(getPageFile("photos/page/" + page + "/photos.html"), "UTF-8");
        }

        Template template = freeMarkerConfigurer.getConfiguration().getTemplate(themeService.renderWithSuffix("photos"));
        String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
        fileWriter.write(html);

        log.info("Generate photos.html succeed.");

        if (photos.hasNext()) {
            generatePhoto(photos.getNumber() + 2);
            log.info("Generate page/{}/index.html", photos.getNumber() + 2);
        }
    }

    /**
     * Generate categories/index.html.
     *
     * @throws IOException       IOException
     * @throws TemplateException TemplateException
     */
    private void generateCategories() throws IOException, TemplateException {
        log.info("Generate categories.html");

        ModelMap model = new ModelMap();

        if (!themeService.templateExists("categories.ftl")) {
            log.warn("categories.ftl not found,skip!");
            return;
        }

        model.addAttribute("is_categories", true);
        Template template = freeMarkerConfigurer.getConfiguration().getTemplate(themeService.renderWithSuffix("categories"));
        String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
        FileWriter fileWriter = new FileWriter(getPageFile("categories/index.html"), "UTF-8");
        fileWriter.write(html);

        List<Category> categories = categoryService.listAll();
        for (Category category : categories) {
            generateCategory(1, category);
        }

        log.info("Generate categories.html succeed.");
    }

    /**
     * Generate categories/{slug}/index.html and categories/{slug}/{page}/index.html.
     *
     * @param page     current page
     * @param category current category
     * @throws IOException       IOException
     * @throws TemplateException TemplateException
     */
    private void generateCategory(int page, Category category) throws IOException, TemplateException {
        if (!themeService.templateExists("category.ftl")) {
            log.warn("category.ftl not found,skip!");
            return;
        }

        ModelMap model = new ModelMap();

        final Pageable pageable = PageRequest.of(page - 1, optionService.getPostPageSize(), Sort.by(DESC, "createTime"));
        Page<Post> postPage = postCategoryService.pagePostBy(category.getId(), PostStatus.PUBLISHED, pageable);
        Page<PostListVO> posts = postService.convertToListVo(postPage);
        final int[] rainbow = PageUtil.rainbow(page, posts.getTotalPages(), 3);

        model.addAttribute("is_category", true);
        model.addAttribute("posts", posts);
        model.addAttribute("rainbow", rainbow);
        model.addAttribute("category", category);

        Template template = freeMarkerConfigurer.getConfiguration().getTemplate(themeService.renderWithSuffix("category"));
        String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);

        FileWriter fileWriter;

        if (page == 1) {
            fileWriter = new FileWriter(getPageFile("categories/" + category.getSlug() + "/index.html"), "UTF-8");
        } else {
            fileWriter = new FileWriter(getPageFile("categories/" + category.getSlug() + "/page/" + page + "/index.html"), "UTF-8");
        }

        fileWriter.write(html);

        if (postPage.hasNext()) {
            generateCategory(postPage.getNumber() + 2, category);
            log.info("Generate categories/{}/page/{}/index.html", category.getSlug(), postPage.getNumber() + 2);
        }
    }

    /**
     * Generate tags/{slug}/index.html and tags/{slug}/{page}/index.html.
     *
     * @param page     current page
     * @param tag      current tag
     * @throws IOException       IOException
     * @throws TemplateException TemplateException
     */
    private void generateTag(int page, Tag tag) throws IOException, TemplateException {
        if (!themeService.templateExists("tag.ftl")) {
            log.warn("tag.ftl not found,skip!");
            return;
        }

        ModelMap model = new ModelMap();

        final Pageable pageable = PageRequest.of(page - 1, optionService.getPostPageSize(), Sort.by(DESC, "createTime"));
        Page<Post> postPage = postTagService.pagePostsBy(tag.getId(), PostStatus.PUBLISHED, pageable);
        Page<PostListVO> posts = postService.convertToListVo(postPage);
        final int[] rainbow = PageUtil.rainbow(page, posts.getTotalPages(), 3);

        model.addAttribute("is_tag", true);
        model.addAttribute("posts", posts);
        model.addAttribute("rainbow", rainbow);
        model.addAttribute("tag", tag);

        Template template = freeMarkerConfigurer.getConfiguration().getTemplate(themeService.renderWithSuffix("tag"));
        String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);

        FileWriter fileWriter;

        if (page == 1) {
            fileWriter = new FileWriter(getPageFile("tags/" + tag.getSlug() + "/index.html"), "UTF-8");
        } else {
            fileWriter = new FileWriter(getPageFile("tags/" + tag.getSlug() + "/page/" + page + "/index.html"), "UTF-8");
        }

        fileWriter.write(html);

        if (postPage.hasNext()) {
            generateTag(postPage.getNumber() + 2, tag);
            log.info("Generate tags/{}/page/{}/index.html", tag.getSlug(), postPage.getNumber() + 2);
        }
    }

    /**
     * Generate tags/index.html.
     *
     * @throws IOException       IOException
     * @throws TemplateException TemplateException
     */
    private void generateTags() throws IOException, TemplateException {
        log.info("Generate tags.html");

        ModelMap model = new ModelMap();

        if (!themeService.templateExists("tags.ftl")) {
            log.warn("tags.ftl not found,skip!");
            return;
        }

        model.addAttribute("is_tags", true);
        Template template = freeMarkerConfigurer.getConfiguration().getTemplate(themeService.renderWithSuffix("tags"));
        String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
        FileWriter fileWriter = new FileWriter(getPageFile("tags/index.html"), "UTF-8");
        fileWriter.write(html);

        log.info("Generate tags.html succeed.");

        List<Tag> tags = tagService.listAll();
        for (Tag tag : tags) {
            generateTag(1, tag);
        }
    }

    /**
     * Generate rss.xml and feed.xml
     *
     * @throws IOException       IOException
     * @throws TemplateException TemplateException
     */
    private void generateRss() throws IOException, TemplateException {
        log.info("Generate rss.xml/feed.xml");

        ModelMap model = new ModelMap();

        model.addAttribute("posts", buildPosts(buildPostPageable(optionService.getRssPageSize())));

        Template template = freeMarkerConfigurer.getConfiguration().getTemplate("common/web/rss.ftl");
        String xml = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
        FileWriter rssWriter = new FileWriter(getPageFile("rss.xml"), "UTF-8");
        rssWriter.write(xml);

        FileWriter feedWriter = new FileWriter(getPageFile("feed.xml"), "UTF-8");
        feedWriter.write(xml);

        log.info("Generate rss.xml/feed.xml succeed.");
    }

    /**
     * Generate atom.xml
     *
     * @throws IOException       IOException
     * @throws TemplateException TemplateException
     */
    private void generateAtom() throws IOException, TemplateException {
        log.info("Generate atom.xml");

        ModelMap model = new ModelMap();

        model.addAttribute("posts", buildPosts(buildPostPageable(optionService.getRssPageSize())));

        Template template = freeMarkerConfigurer.getConfiguration().getTemplate("common/web/atom.ftl");
        String xml = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
        FileWriter fileWriter = new FileWriter(getPageFile("atom.xml"), "UTF-8");
        fileWriter.write(xml);

        log.info("Generate atom.xml succeed.");
    }

    /**
     * Generate sitemap.html
     *
     * @throws IOException       IOException
     * @throws TemplateException TemplateException
     */
    private void generateSiteMapHtml() throws IOException, TemplateException {
        log.info("Generate sitemap.html");

        ModelMap model = new ModelMap();

        model.addAttribute("posts", buildPosts(buildPostPageable(optionService.getRssPageSize())));

        Template template = freeMarkerConfigurer.getConfiguration().getTemplate("common/web/sitemap_html.ftl");
        String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
        FileWriter fileWriter = new FileWriter(getPageFile("sitemap.html"), "UTF-8");
        fileWriter.write(html);

        log.info("Generate sitemap.html succeed.");
    }

    /**
     * Generate sitemap.xml
     *
     * @throws IOException       IOException
     * @throws TemplateException TemplateException
     */
    private void generateSiteMapXml() throws IOException, TemplateException {
        log.info("Generate sitemap.xml");

        ModelMap model = new ModelMap();

        model.addAttribute("posts", buildPosts(buildPostPageable(optionService.getRssPageSize())));

        Template template = freeMarkerConfigurer.getConfiguration().getTemplate("common/web/sitemap_xml.ftl");
        String xml = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
        FileWriter fileWriter = new FileWriter(getPageFile("sitemap.xml"), "UTF-8");
        fileWriter.write(xml);

        log.info("Generate sitemap.xml succeed.");
    }

    /**
     * Generate robots.txt
     *
     * @throws IOException       IOException
     * @throws TemplateException TemplateException
     */
    private void generateRobots() throws IOException, TemplateException {
        log.info("Generate robots.txt");

        Template template = freeMarkerConfigurer.getConfiguration().getTemplate("common/web/robots.ftl");
        String txt = FreeMarkerTemplateUtils.processTemplateIntoString(template, null);
        FileWriter fileWriter = new FileWriter(getPageFile("robots.txt"), "UTF-8");
        fileWriter.write(txt);

        log.info("Generate robots.txt succeed.");
    }

    /**
     * Generate README.md.
     *
     * @throws IOException       IOException
     * @throws TemplateException TemplateException
     */
    private void generateReadme() throws IOException, TemplateException {
        log.info("Generate readme.md");

        Template template = freeMarkerConfigurer.getConfiguration().getTemplate("common/web/readme.ftl");
        String txt = FreeMarkerTemplateUtils.processTemplateIntoString(template, null);
        FileWriter fileWriter = new FileWriter(getPageFile("README.md"), "UTF-8");
        fileWriter.write(txt);

        log.info("Generate readme.md succeed.");
    }

    /**
     * Copy current theme folder.
     *
     * @throws IOException IOException
     */
    private void copyThemeFolder() throws IOException {
        ThemeProperty activatedTheme = themeService.getActivatedTheme();
        Path path = Paths.get(pagesDir.toString(), activatedTheme.getFolderName());
        FileUtils.createIfAbsent(path);
        FileUtils.copyFolder(Paths.get(activatedTheme.getThemePath()), path);
        cleanThemeFolder(Paths.get(pagesDir.toString(), activatedTheme.getFolderName()));
    }

    private void cleanThemeFolder(Path themePath) {
        if (!Files.isDirectory(themePath)) {
            return;
        }

        try (Stream<Path> pathStream = Files.list(themePath)) {

            pathStream.forEach(path -> {
                if (!Files.isDirectory(path)) {
                    for (String suffix : USELESS_FILE_SUFFIX) {
                        if (suffix.contains(FileUtil.extName(path.toFile()))) {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else {
                    if (path.getFileName().toString().contains(".git")) {
                        FileUtils.deleteFolderQuietly(path);
                    } else {
                        cleanThemeFolder(path);
                    }
                }
            });
        } catch (IOException e) {
            throw new ServiceException("Failed to list sub files", e);
        }
    }

    /**
     * Copy upload folder.
     *
     * @throws IOException IOException
     */
    private void copyUpload() throws IOException {
        Path path = Paths.get(pagesDir.toString(), "upload");
        FileUtils.createIfAbsent(path);
        FileUtils.copyFolder(Paths.get(haloProperties.getWorkDir(), "upload"), path);
    }

    /**
     * Copy static folder.
     *
     * @throws IOException IOException
     */
    private void copyStatic() throws IOException {
        FileUtils.copyFolder(Paths.get(haloProperties.getWorkDir(), "static"), pagesDir);
    }

    /**
     * Build posts for feed
     *
     * @param pageable pageable
     * @return List<Post>
     */
    private List<PostDetailVO> buildPosts(@NonNull Pageable pageable) {
        Page<Post> postPage = postService.pageBy(PostStatus.PUBLISHED, pageable);
        Page<PostDetailVO> posts = postService.convertToDetailVo(postPage);
        posts.getContent().forEach(postListVO -> {
            try {
                // Encode post slug
                postListVO.setSlug(URLEncoder.encode(postListVO.getSlug(), StandardCharsets.UTF_8.name()));
            } catch (UnsupportedEncodingException e) {
                log.warn("Failed to encode url: " + postListVO.getSlug(), e);
            }
        });
        return posts.getContent();
    }

    /**
     * Builds page info for post.
     *
     * @param size page size
     * @return page info
     */
    @NonNull
    private Pageable buildPostPageable(int size) {
        return PageRequest.of(0, size, Sort.by(DESC, "createTime"));
    }

    private File getPageFile(String subPath) {
        Path path = Paths.get(pagesDir.toString(), subPath);
        return path.toFile();
    }

    /**
     * Get static deploy type from options.
     *
     * @return static deploy type
     */
    @NonNull
    private StaticDeployType getStaticDeployType() {
        return optionService.getEnumByPropertyOrDefault(StaticDeployProperties.DEPLOY_TYPE, StaticDeployType.class, StaticDeployType.GIT);
    }
}
