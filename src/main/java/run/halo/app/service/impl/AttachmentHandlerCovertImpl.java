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
import run.halo.app.model.enums.AttachmentType;
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

    private static final String CHARACTER_SET_JDK8 = "utf-8";

    public AttachmentHandlerCovertImpl(PostService postService, AttachmentService attachmentService, HaloProperties haloProperties) {
        this.postService = postService;
        this.attachmentService = attachmentService;
        this.workDir = FileHandler.normalizeDirectory(haloProperties.getWorkDir());
    }

    /**
     * @param sourceAttachmentTypeId source attachment type id (e.g. 0,1,2), default = -1 (All AttachmentTypes).
     * @param deleteOldAttachment    Whether to delete old attachments, default = false.
     * @param uploadAllInAttachment  Whether to upload all attachments, default = false.
     * @param uploadAllInPost        Whether to download and upload all pictures in the all posts, default = false.
     * @return Future<String>
     */

    @Async
    public Future<String> covertHandlerByPosts(
            AttachmentType sourceAttachmentTypeId,
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
        } catch (Exception e) {
            log.info(e.toString());
            e.printStackTrace();
            return new AsyncResult<>("Covert attachment handler Failed!" + e.toString());
        }
    }

    private void doCovertHandlerByPosts(
            AttachmentType attachmentTypeId,
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

    /**
     * Is the attachment cited in the post
     *
     * @param pathInPost       the path in Post
     * @param pathInAttachment the path in Attachment library
     * @return Is the attachment cited in the post
     * @throws UnsupportedEncodingException url encode
     */
    private Boolean attachmentInPost(String pathInPost, String pathInAttachment) throws UnsupportedEncodingException {
        if (null == pathInPost || null == pathInAttachment) {
            return false;
        }
        return pathInPost.contains(pathInAttachment) || pathInPost.contains(AttachmentHandlerCovertUtils.encodeFileBaseName(pathInAttachment));
    }

    /**
     * Delete the old attachment from the source handler
     *
     * @param attachmentId attachment id
     */
    private void doDeleteAttachment(Integer attachmentId) {
        try {
            attachmentService.removePermanently(attachmentId);
        } catch (Exception e) {
            log.info("Delete File Failed: " + e.toString());
            e.printStackTrace();
        }
    }

    /**
     * Update the attachment path in the post
     *
     * @param oldAttachmentPath old Attachment Path
     * @param fileBaseName      file Base Name
     * @param pathInPosts       path In Posts
     * @param stringBuilder     to update post content
     * @return update success or not
     * @throws IOException Attachment delete Exception
     */
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

    /**
     * Download the attachment and return to the temporary file path
     *
     * @param urlStr       attachment url
     * @param fileBaseName file Base Name
     * @return tmp Attachment Path
     * @throws IOException File writing exception
     */
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
            String oldAttachmentPath = URLDecoder.decode(String.valueOf(Paths.get(workDir, urlStr)), CHARACTER_SET_JDK8);
            File oldAttachment = new File(oldAttachmentPath);
            File tmpAttachment = new File(tmpAttachmentPath);
            if (oldAttachment.exists()) {
                FileUtils.copyFile(oldAttachment, tmpAttachment);
                FileUtils.waitFor(tmpAttachment, 100);
            }
        }
        return tmpAttachmentPath;
    }

    /**
     * upload File to current handler
     *
     * @param oldAttachmentPath old Attachment Path
     * @param fileBaseName      file Base Name
     * @return new attachment or null
     * @throws IOException IOException
     */
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
            log.warn("Can not upload file: {}\n{}", oldAttachmentPath, e.toString());
            e.printStackTrace();
        } finally {
            Files.deleteIfExists(tmpAttachment.toPath());
        }
        return null;
    }
}
