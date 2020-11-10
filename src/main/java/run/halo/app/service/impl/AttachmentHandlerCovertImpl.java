package run.halo.app.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.StopWatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import run.halo.app.config.properties.HaloProperties;
import run.halo.app.handler.file.FileHandler;
import run.halo.app.model.entity.Attachment;
import run.halo.app.model.entity.Post;
import run.halo.app.service.AttachmentHandlerCovertService;
import run.halo.app.service.AttachmentService;
import run.halo.app.service.PostService;
import run.halo.app.utils.AttachmentHandlerCovertUtils;
import run.halo.app.utils.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Attachment Handler Covert implementation.
 *
 * @author xcp
 * @date 2020-11-07
 */

@Slf4j
@Service
public class AttachmentHandlerCovertImpl implements AttachmentHandlerCovertService {

    private final AttachmentService attachmentService;

    private final PostService postService;

    private final String workDir;

    private static final String ATTACHMENT_TMP_DIR = "attachment_tmp/";


    public AttachmentHandlerCovertImpl(PostService postService, AttachmentService attachmentService, HaloProperties haloProperties) {
        this.postService = postService;
        this.attachmentService = attachmentService;
        this.workDir = FileHandler.normalizeDirectory(haloProperties.getWorkDir());
    }

    @Async
    public Future<String> covertAttachmentHandler(Boolean uploadAll) {
        StopWatch stopWatch = DateUtil.createStopWatch("Covert Attachment Handler");
        stopWatch.start();
        log.info("---------- Start covert attachment handler, about a few to tens of minutes: {} ----------",
                DateUtil.now());
        try {
            doCovertAttachmentHandler(uploadAll);
            stopWatch.stop();
            String res = MessageFormat.format(
                    "Covert attachment handler has finished: {0}\n{1}",
                    DateUtil.now(),
                    stopWatch.prettyPrint());
            log.info(res);
            return new AsyncResult<>(res);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new AsyncResult<>("Covert attachment handler Failed!");
    }


    public void doCovertAttachmentHandler(Boolean uploadAll) throws IOException {
        List<Post> posts = postService.listAll();
        Map<String, List<Integer>> pathInPosts = AttachmentHandlerCovertUtils.getPathInPost(posts);

        List<Attachment> oldAttachments = attachmentService.listAll();
        Map<String, Integer> pathInAttachments = AttachmentHandlerCovertUtils.getPathInAttachment(oldAttachments);

        Iterator<Map.Entry<String, List<Integer>>> pathInPostsIterator = pathInPosts.entrySet().iterator();
        StringBuilder stringBuilder = new StringBuilder();

        while (pathInPostsIterator.hasNext()) {
            Map.Entry<String, List<Integer>> pathInPostEntry = pathInPostsIterator.next();
            Iterator<Map.Entry<String, Integer>> pathInAttachmentsIterator = pathInAttachments.entrySet().iterator();
            while (pathInAttachmentsIterator.hasNext()) {
                Map.Entry<String, Integer> pathInAttachmentEntry = pathInAttachmentsIterator.next();
                if (pathInPostEntry.getKey().contains(pathInAttachmentEntry.getKey())) {
                    Attachment oldAttachment = attachmentService.getById(pathInAttachmentEntry.getValue());
                    updatePostAttachment(
                            pathInPostEntry.getKey(),
                            oldAttachment.getName(),
                            pathInPostEntry.getValue(),
                            stringBuilder);
                    attachmentService.remove(oldAttachment);
                    pathInAttachmentsIterator.remove();
                }
            }
            pathInPostsIterator.remove();
        }

        for (Map.Entry<String, Integer> pathInAttachmentEntry : pathInAttachments.entrySet()) {
            String newAttachmentPath = uploadFile(
                    pathInAttachmentEntry.getKey(),
                    attachmentService.getById(pathInAttachmentEntry.getValue()).getName());
            if ("".equals(newAttachmentPath)) {
                log.warn("Can not upload file: {}", pathInAttachmentEntry.getKey());
            }
        }

        if (Boolean.TRUE.equals(uploadAll)) {
            for (Map.Entry<String, List<Integer>> pathInPostEntry : pathInPosts.entrySet()) {
                updatePostAttachment(
                        pathInPostEntry.getKey(),
                        AttachmentHandlerCovertUtils.getBaseNameFromUrl(pathInPostEntry.getKey()),
                        pathInPostEntry.getValue(),
                        stringBuilder);
            }
        }
    }

    public void updatePostAttachment(String oldAttachmentPath, String fileBaseName, List<Integer> pathInPosts, StringBuilder stringBuilder) throws IOException {
        String newAttachmentPath = uploadFile(oldAttachmentPath, fileBaseName);
        if (!"".equals(newAttachmentPath)) {
            for (Integer postId : pathInPosts) {
                Post post = postService.getById(postId);
                stringBuilder.append(post.getOriginalContent());
                AttachmentHandlerCovertUtils.strBuilderReplaceAll(stringBuilder, oldAttachmentPath, newAttachmentPath);
                post.setOriginalContent(stringBuilder.toString());
                stringBuilder.delete(0, stringBuilder.length());
                postService.createOrUpdateBy(post);
            }
        }
    }

    private String getTmpAttachmentPath(String urlStr, String fileBaseName) throws IOException {
        if (fileBaseName.split("\\.").length < 2) {
            fileBaseName = fileBaseName + "." + FilenameUtils.getExtension(urlStr);
        }
        String tmpAttachmentPath = workDir + ATTACHMENT_TMP_DIR + fileBaseName;

        if (urlStr.startsWith("http")) {
            AttachmentHandlerCovertUtils.downloadFile(urlStr, tmpAttachmentPath);
        } else {
            String oldAttachmentPath = URLDecoder.decode(workDir + urlStr, "utf-8");
            File oldAttachment = new File(oldAttachmentPath);
            if (oldAttachment.exists()) {
                FileUtils.copyFile(oldAttachment, new File(tmpAttachmentPath));
            }
        }
        return tmpAttachmentPath;
    }

    private String uploadFile(String oldAttachmentPath, String fileBaseName) throws IOException {
        File tmpAttachment = new File(getTmpAttachmentPath(oldAttachmentPath, fileBaseName));
        try {
            if (tmpAttachment.exists()) {
                MultipartFile multipartFile = AttachmentHandlerCovertUtils.getMultipartFile(tmpAttachment);
                Attachment attachment = attachmentService.upload(multipartFile);
                return attachment.getPath();
            } else {
                log.warn("Can not download file: {}", oldAttachmentPath);
            }
        } finally {
            Files.delete(tmpAttachment.toPath());
        }
        return "";
    }
}