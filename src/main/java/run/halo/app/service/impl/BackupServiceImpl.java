package run.halo.app.service.impl;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONObject;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.Yaml;
import run.halo.app.cache.StringCacheStore;
import run.halo.app.config.properties.HaloProperties;
import run.halo.app.exception.NotFoundException;
import run.halo.app.exception.ServiceException;
import run.halo.app.model.dto.BackupDTO;
import run.halo.app.model.dto.post.BasePostDetailDTO;
import run.halo.app.model.entity.Post;
import run.halo.app.model.entity.Tag;
import run.halo.app.model.support.HaloConst;
import run.halo.app.security.util.SecurityUtils;
import run.halo.app.service.BackupService;
import run.halo.app.service.OptionService;
import run.halo.app.service.PostService;
import run.halo.app.service.PostTagService;
import run.halo.app.utils.HaloUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static run.halo.app.model.support.HaloConst.TEMP_TOKEN;
import static run.halo.app.model.support.HaloConst.TEMP_TOKEN_EXPIRATION;

/**
 * Backup service implementation.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-04-26
 */
@Service
@Slf4j
public class BackupServiceImpl implements BackupService {

    public static final String BACKUP_TOKEN_KEY_PREFIX = "backup-token-";
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private final PostService postService;
    private final PostTagService postTagService;
    private final OptionService optionService;
    private final StringCacheStore cacheStore;
    private final HaloProperties haloProperties;

    public BackupServiceImpl(PostService postService,
                             PostTagService postTagService,
                             OptionService optionService,
                             StringCacheStore stringCacheStore,
                             HaloProperties haloProperties) {
        this.postService = postService;
        this.postTagService = postTagService;
        this.optionService = optionService;
        this.cacheStore = stringCacheStore;
        this.haloProperties = haloProperties;
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
                replaceAll("[\\?\\\\/:|<>\\*\\[\\]\\(\\)\\$%\\{\\}@~\\.]", "").
                replaceAll("\\s", "");
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
            } else if (post.getDeleted()) {
                drafts.add(one);
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
            String haloZipFileName = HaloConst.HALO_BACKUP_PREFIX +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss-")) +
                    IdUtil.simpleUUID().hashCode() + ".zip";
            // Create halo zip file
            Path haloZipPath = Files.createFile(Paths.get(haloProperties.getBackupDir(), haloZipFileName));

            // Zip halo
            run.halo.app.utils.FileUtils.zip(Paths.get(this.haloProperties.getWorkDir()), haloZipPath);

            // Build backup dto
            return buildBackupDto(haloZipPath);
        } catch (IOException e) {
            throw new ServiceException("Failed to backup halo", e);
        }
    }

    @Override
    public List<BackupDTO> listHaloBackups() {
        // Build backup dto
        try (Stream<Path> subPathStream = Files.list(Paths.get(haloProperties.getBackupDir()))) {
            return subPathStream
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
    public void deleteHaloBackup(String fileName) {
        Assert.hasText(fileName, "File name must not be blank");

        Path backupRootPath = Paths.get(haloProperties.getBackupDir());

        // Get backup path
        Path backupPath = backupRootPath.resolve(fileName);

        // Check directory traversal
        run.halo.app.utils.FileUtils.checkDirectoryTraversal(backupRootPath, backupPath);

        try {
            // Delete backup file
            Files.delete(backupPath);
        } catch (NoSuchFileException e) {
            throw new NotFoundException("The file " + fileName + " was not found", e);
        } catch (IOException e) {
            throw new ServiceException("Failed to delete backup", e);
        }
    }

    @Override
    public Resource loadFileAsResource(String fileName) {
        Assert.hasText(fileName, "Backup file name must not be blank");

        // Get backup file path
        Path backupFilePath = Paths.get(haloProperties.getBackupDir(), fileName).normalize();
        try {
            // Build url resource
            Resource backupResource = new UrlResource(backupFilePath.toUri());
            if (!backupResource.exists()) {
                // If the backup resouce is not exist
                throw new NotFoundException("The file " + fileName + " was not found");
            }
            // Return the backup resource
            return backupResource;
        } catch (MalformedURLException e) {
            throw new NotFoundException("The file " + fileName + " was not found", e);
        }
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
        try {
            backup.setDownloadUrl(buildDownloadUrl(backupFileName));
            backup.setDownloadLink(backup.getDownloadUrl());
            backup.setFilename(backupFileName);
            backup.setUpdateTime(Files.getLastModifiedTime(backupPath).toMillis());
            backup.setFileSize(Files.size(backupPath));
        } catch (IOException e) {
            throw new ServiceException("Failed to access file " + backupPath, e);
        } catch (URISyntaxException e) {
            throw new ServiceException("Failed to generate download link for file: " + backupPath, e);
        }

        return backup;
    }

    /**
     * Builds download url.
     *
     * @param filename filename must not be blank
     * @return download url
     */
    private String buildDownloadUrl(@NonNull String filename) throws URISyntaxException {
        Assert.hasText(filename, "File name must not be blank");

        // Composite http url
        String backupFullUrl = HaloUtils.compositeHttpUrl(optionService.getBlogBaseUrl(), "api/admin/backups/halo", filename);

        // Get temp token
        String tempToken = cacheStore.get(buildBackupTokenKey(filename)).orElseGet(() -> {
            String token = buildTempToken(1);
            // Cache this projection
            cacheStore.putIfAbsent(buildBackupTokenKey(filename), token, TEMP_TOKEN_EXPIRATION.toDays(), TimeUnit.DAYS);
            return token;
        });

        return new URIBuilder(backupFullUrl).addParameter(TEMP_TOKEN, tempToken).toString();
    }

    private String buildBackupTokenKey(String backupFileName) {
        return BACKUP_TOKEN_KEY_PREFIX + backupFileName;
    }

    private String buildTempToken(@NonNull Object value) {
        Assert.notNull(value, "Temp token value must not be null");

        // Generate temp token
        String tempToken = HaloUtils.randomUUIDWithoutDash();

        // Cache the token
        cacheStore.putIfAbsent(SecurityUtils.buildTempTokenKey(tempToken), value.toString(), TEMP_TOKEN_EXPIRATION.toDays(), TimeUnit.DAYS);

        return tempToken;
    }
}
