package run.halo.app.service.impl;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.json.JSONObject;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.Yaml;
import run.halo.app.config.properties.HaloProperties;
import run.halo.app.exception.NotFoundException;
import run.halo.app.exception.ServiceException;
import run.halo.app.model.dto.BackupDTO;
import run.halo.app.model.dto.post.BasePostDetailDTO;
import run.halo.app.model.entity.Post;
import run.halo.app.model.entity.Tag;
import run.halo.app.model.support.HaloConst;
import run.halo.app.service.BackupService;
import run.halo.app.service.OptionService;
import run.halo.app.service.PostService;
import run.halo.app.service.PostTagService;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Backup service implementation.
 *
 * @author johnniang
 * @date 2019-04-26
 */
@Service
@Slf4j
public class BackupServiceImpl implements BackupService {

    private final PostService postService;

    private final PostTagService postTagService;

    private final OptionService optionService;

    private final HaloProperties halo;

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    public BackupServiceImpl(PostService postService,
                             PostTagService postTagService,
                             OptionService optionService,
                             HaloProperties halo) {
        this.postService = postService;
        this.postTagService = postTagService;
        this.optionService = optionService;
        this.halo = halo;
    }

    @Override
    public BasePostDetailDTO importMarkdown(MultipartFile file) throws IOException {

        // Read markdown content.
        String markdown = IoUtil.read(file.getInputStream(), StandardCharsets.UTF_8);

        // TODO sheet import

        return postService.importMarkdown(markdown, file.getOriginalFilename());
    }

    @Override
    public JSONObject exportHexoMDs() {
        final JSONObject ret = new JSONObject();
        final List<JSONObject> posts = new ArrayList<>();
        ret.put("posts", (Object) posts);
        final List<JSONObject> passwords = new ArrayList<>();
        ret.put("passwords", (Object) passwords);
        final List<JSONObject> drafts = new ArrayList<>();
        ret.put("drafts", (Object) drafts);

        List<Post> postList = postService.listAll();
        Map<Integer, List<Tag>> talMap = postTagService.listTagListMapBy(postList.stream().map(Post::getId).collect(Collectors.toList()));
        for (Post post : postList) {
            final Map<String, Object> front = new LinkedHashMap<>();
            final String title = post.getTitle();
            front.put("title", title);
            final String date = DateFormatUtils.format(post.getCreateTime(), "yyyy-MM-dd HH:mm:ss");
            front.put("date", date);
            front.put("updated", DateFormatUtils.format(post.getUpdateTime(), "yyyy-MM-dd HH:mm:ss"));
            final List<String> tags = talMap.get(post.getId()).stream().map(Tag::getName).collect(Collectors.toList());
            if (tags.isEmpty()) {
                tags.add("halo");
            }
            front.put("tags", tags);
            front.put("permalink", "");
            final JSONObject one = new JSONObject();
            one.put("front", new Yaml().dump(front));
            one.put("title", title);
            one.put("content", post.getOriginalContent());
            one.put("created", post.getCreateTime().getTime());

            if (StringUtils.isNotBlank(post.getPassword())) {
                passwords.add(one);
                continue;
            } else if (post.getDeleted()) {
                drafts.add(one);
                continue;
            } else {
                posts.add(one);
            }
        }

        return ret;
    }

    @Override
    public void exportHexoMd(List<JSONObject> posts, String dirPath) {
        posts.forEach(post -> {
            final String filename = sanitizeFilename(post.optString("title")) + ".md";
            final String text = post.optString("front") + "---" + LINE_SEPARATOR + post.optString("content");

            try {
                final String date = DateFormatUtils.format(post.optLong("created"), "yyyyMM");
                final String dir = dirPath + File.separator + date + File.separator;
                new File(dir).mkdirs();
                FileUtils.writeStringToFile(new File(dir + filename), text, "UTF-8");
            } catch (final Exception e) {
                log.error("Write markdown file failed", e);
            }
        });
    }

    @Override
    public BackupDTO zipWorkDirectory() {
        // Zip work directory to temporary file
        try {
            // Create zip path for halo zip
            String haloZipFileName = new StringBuilder().append(HaloConst.HALO_BACKUP_PREFIX)
                    .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")))
                    .append(IdUtil.simpleUUID())
                    .append(".zip").toString();
            // Create halo zip file
            Path haloZipPath = Files.createFile(Paths.get(halo.getBackupDir(), haloZipFileName));

            // Zip halo
            run.halo.app.utils.FileUtils.zip(Paths.get(this.halo.getWorkDir()), haloZipPath);

            // Build backup dto
            return buildBackupDto(haloZipPath);
        } catch (IOException e) {
            throw new ServiceException("Failed to backup halo", e);
        }
    }

    @Override
    public List<BackupDTO> listHaloBackups() {
        try {
            // Build backup dto
            return Files.list(Paths.get(halo.getBackupDir()))
                    .filter(backupPath -> StringUtils.startsWithIgnoreCase(backupPath.getFileName().toString(), HaloConst.HALO_BACKUP_PREFIX))
                    .map(this::buildBackupDto)
                    .sorted((leftBackup, rightBackup) -> {
                        // Sort the result
                        if (leftBackup.getUpdateTime() < rightBackup.getUpdateTime()) {
                            return 1;
                        } else if (leftBackup.getUpdateTime() > rightBackup.getUpdateTime()) {
                            return -1;
                        }
                        return 0;
                    }).collect(Collectors.toList());
        } catch (IOException e) {
            throw new ServiceException("Failed to fetch backups", e);
        }
    }

    @Override
    public void deleteHaloBackup(String filename) {
        Assert.hasText(filename, "File name must not be blank");

        // Get backup path
        Path backupPath = Paths.get(halo.getBackupDir(), filename);

        try {
            // Delete backup file
            Files.delete(backupPath);

        } catch (NoSuchFileException e) {
            throw new NotFoundException("The file " + filename + " was not found", e);
        } catch (IOException e) {
            throw new ServiceException("Failed to delete backup", e);
        }
    }

    /**
     * Sanitizes the specified file name.
     *
     * @param unsanitized the specified file name
     * @return sanitized file name
     */
    public static String sanitizeFilename(final String unsanitized) {
        return unsanitized.
                replaceAll("[^(a-zA-Z0-9\\u4e00-\\u9fa5\\.)]", "").
                replaceAll("[\\?\\\\/:|<>\\*\\[\\]\\(\\)\\$%\\{\\}@~]", "").
                replaceAll("\\s", "");
    }

    /**
     * Builds backup dto.
     *
     * @param backupPath backup path must not be null
     * @return backup dto
     */
    private BackupDTO buildBackupDto(@NonNull Path backupPath) {
        Assert.notNull(backupPath, "Backup path must not be null");

        String backupFileName = backupPath.getFileName().toString();
        BackupDTO backup = new BackupDTO();
        backup.setDownloadUrl(buildDownloadUrl(backupFileName));
        backup.setFilename(backupFileName);
        try {
            backup.setUpdateTime(Files.getLastModifiedTime(backupPath).toMillis());
        } catch (IOException e) {
            throw new ServiceException("Failed to get last modified time of " + backupPath.toString(), e);
        }

        return backup;
    }

    /**
     * Builds download url.
     *
     * @param filename filename must not be blank
     * @return download url
     */
    private String buildDownloadUrl(@NonNull String filename) {
        Assert.hasText(filename, "File name must not be blank");

        return StringUtils.joinWith("/",
                optionService.getBlogBaseUrl(),
                StringUtils.removeEnd(StringUtils.removeStart(halo.getBackupUrlPrefix(), "/"), "/"),
                filename);
    }
}
