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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    public AttachmentHandlerCovertImpl(PostService postService, AttachmentService attachmentService, HaloProperties haloProperties) {
        this.postService = postService;
        this.attachmentService = attachmentService;
        this.workDir = FileHandler.normalizeDirectory(haloProperties.getWorkDir());
    }

    @Async
    public Future<String> covertHandlerByPosts(
            Integer sourceAttachmentTypeId,
            Boolean deleteOldAttachment,
            Boolean uploadAllInAttachment,
            Boolean uploadAllInPost) {
        String taskParams = MessageFormat.format("sourceAttachmentTypeId: {0}, deleteOldAttachment: {1}, uploadAllInAttachment: {2}, uploadAllInPost: {3}",
                sourceAttachmentTypeId,
                deleteOldAttachment,
                uploadAllInAttachment,
                uploadAllInPost);
        StopWatch stopWatch = DateUtil.createStopWatch("Covert Attachment Handler");
        log.info("Start covert attachment handler: sourceAttachmentTypeId: " + taskParams);

        stopWatch.start(taskParams);
        try {
            doCovertHandlerByPosts(sourceAttachmentTypeId, deleteOldAttachment, uploadAllInAttachment, uploadAllInPost);
            stopWatch.stop();
            String res = MessageFormat.format(
                    "Covert attachment handler has finished!\n{0}", stopWatch.prettyPrint());
            log.info(res);
            return new AsyncResult<>(res);
        } catch (IOException e) {
            log.info("ERROR:" + e.toString());
            e.printStackTrace();
        }
        return new AsyncResult<>("Covert attachment handler Failed!");
    }


    private void doCovertHandlerByPosts(
            Integer attachmentTypeId,
            Boolean deleteOldAttachment,
            Boolean uploadAllInAttachment,
            Boolean uploadAllInPost) throws IOException {

        Map<String, List<Integer>> pathInPosts = AttachmentHandlerCovertUtils.getPathInPost(postService.listAll());
        Map<String, Integer> pathInAttachments = AttachmentHandlerCovertUtils.getPathInAttachment(attachmentService.listAll(), attachmentTypeId);
        Iterator<Map.Entry<String, List<Integer>>> pathInPostsIterator = pathInPosts.entrySet().iterator();
        StringBuilder stringBuilder = new StringBuilder();
        while (pathInPostsIterator.hasNext()) {
            Map.Entry<String, List<Integer>> pathInPostEntry = pathInPostsIterator.next();
            Iterator<Map.Entry<String, Integer>> pathInAttachmentsIterator = pathInAttachments.entrySet().iterator();
            while (pathInAttachmentsIterator.hasNext()) {
                Map.Entry<String, Integer> pathInAttachmentEntry = pathInAttachmentsIterator.next();
                if (Boolean.TRUE.equals(attachmentInPost(pathInPostEntry.getKey(), pathInAttachmentEntry.getKey()))) {
                    Attachment oldAttachment = attachmentService.getById(pathInAttachmentEntry.getValue());
                    Boolean f = updatePostAttachment(
                            pathInPostEntry.getKey(),
                            oldAttachment.getName(),
                            pathInPostEntry.getValue(),
                            stringBuilder);
                    if (Boolean.TRUE.equals(f) && Boolean.TRUE.equals(deleteOldAttachment)) {
                        doDeleteAttachment(pathInAttachmentEntry.getValue());
                    }
                    pathInAttachmentsIterator.remove();
                }
            }
            pathInPostsIterator.remove();
        }

        if (Boolean.TRUE.equals(uploadAllInAttachment)) {
            for (Map.Entry<String, Integer> pathInAttachmentEntry : pathInAttachments.entrySet()) {
                Attachment newAttachment = uploadFile(
                        pathInAttachmentEntry.getKey(),
                        attachmentService.getById(pathInAttachmentEntry.getValue()).getName());
                if (null != newAttachment && Boolean.TRUE.equals(deleteOldAttachment)) {
                    doDeleteAttachment(pathInAttachmentEntry.getValue());
                }
            }
        }

        if (Boolean.TRUE.equals(uploadAllInPost)) {
            for (Map.Entry<String, List<Integer>> pathInPostEntry : pathInPosts.entrySet()) {
                updatePostAttachment(
                        pathInPostEntry.getKey(),
                        AttachmentHandlerCovertUtils.getBaseNameFromUrl(pathInPostEntry.getKey()),
                        pathInPostEntry.getValue(),
                        stringBuilder);
            }
        }
    }

    private Boolean attachmentInPost(String pathInPost, String pathInAttachment) throws UnsupportedEncodingException {
        if (null == pathInPost || null == pathInAttachment) {
            return false;
        }
        return pathInPost.contains(pathInAttachment) || pathInPost.contains(AttachmentHandlerCovertUtils.encodeFileBaseName(pathInAttachment));
    }

    private void doDeleteAttachment(Integer attachmentId) {
        try {
            attachmentService.removePermanently(attachmentId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Boolean updatePostAttachment(String oldAttachmentPath, String fileBaseName, List<Integer> pathInPosts, StringBuilder stringBuilder) throws IOException {
        Attachment newAttachment = uploadFile(oldAttachmentPath, fileBaseName);
        if (null != newAttachment) {
            String newAttachmentPath = attachmentService.convertToDto(newAttachment).getPath();
            for (Integer postId : pathInPosts) {
                Post post = postService.getById(postId);
                stringBuilder.append(post.getOriginalContent());
                AttachmentHandlerCovertUtils.strBuilderReplaceAll(stringBuilder, oldAttachmentPath, newAttachmentPath);
                post.setOriginalContent(stringBuilder.toString());
                stringBuilder.delete(0, stringBuilder.length());
                if (Boolean.TRUE.equals(attachmentInPost(post.getThumbnail(), oldAttachmentPath))) {
                    post.setThumbnail(newAttachmentPath);
                }
                postService.createOrUpdateBy(post);
            }
            return true;
        }
        return false;
    }

    private String getTmpAttachmentPath(String urlStr, String fileBaseName) throws IOException {
        if (!FilenameUtils.getExtension(urlStr).equals(FilenameUtils.getExtension(fileBaseName))) {
            fileBaseName = fileBaseName + "." + FilenameUtils.getExtension(urlStr);
        }
        String tmpAttachmentPath = String.valueOf(Paths.get(FileUtils.getTempDirectoryPath(), fileBaseName));

        if (urlStr.startsWith("http")) {
            AttachmentHandlerCovertUtils.downloadFile(
                    AttachmentHandlerCovertUtils.encodeFileBaseName(urlStr),
                    tmpAttachmentPath);
        } else {
            String oldAttachmentPath = URLDecoder.decode(String.valueOf(Paths.get(workDir, urlStr)), "utf-8");
            File oldAttachment = new File(oldAttachmentPath);
            File tmpAttachment = new File(tmpAttachmentPath);
            if (oldAttachment.exists()) {
                FileUtils.copyFile(oldAttachment, tmpAttachment);
                FileUtils.waitFor(tmpAttachment, 100);
            }
        }
        return tmpAttachmentPath;
    }

    private Attachment uploadFile(String oldAttachmentPath, String fileBaseName) throws IOException {
        File tmpAttachment = new File(getTmpAttachmentPath(oldAttachmentPath, fileBaseName));
        try {
            if (tmpAttachment.exists()) {
                MultipartFile multipartFile = AttachmentHandlerCovertUtils.getMultipartFile(tmpAttachment);
                return attachmentService.upload(multipartFile);
            } else {
                log.warn("Can not download file: {}", oldAttachmentPath);
            }
        } catch (Exception e) {
            log.warn("Can not upload file: {}", oldAttachmentPath);
            e.printStackTrace();
        } finally {
            Files.deleteIfExists(tmpAttachment.toPath());
        }
        return null;
    }
}
