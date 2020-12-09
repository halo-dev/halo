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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
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


    public AttachmentHandlerCovertImpl(
            PostService postService, AttachmentService attachmentService,
            HaloProperties haloProperties) {
        this.postService = postService;
        this.attachmentService = attachmentService;

        this.workDir = FileHandler.normalizeDirectory(haloProperties.getWorkDir());
    }

    /**
     * 方法入口
     *
     * @param sourceAttachmentType  source attachment type (e.g. LOCAL), default = LOCAL.
     * @param deleteOldAttachment   Whether to delete old attachments, default = false.
     * @param uploadAllInAttachment Whether to upload all attachments, default = false.
     * @param uploadAllInPost       Whether to download and upload all pictures in the all posts, default = false.
     * @return Future<String>
     */
    @Async
    public Future<String> covertHandlerByPosts(
            AttachmentType sourceAttachmentType, Boolean deleteOldAttachment,
            Boolean uploadAllInAttachment, Boolean uploadAllInPost) {

        String taskParams = MessageFormat.format(
                "sourceAttachmentTypeId: {0}, deleteOldAttachment: {1}, uploadAllInAttachment: {2}, uploadAllInPost: {3}",
                sourceAttachmentType,
                deleteOldAttachment,
                uploadAllInAttachment,
                uploadAllInPost);
        StopWatch stopWatch = DateUtil.createStopWatch("Covert Attachment Handler");
        log.info("Start covert attachment handler: sourceAttachmentTypeId: " + taskParams);

        stopWatch.start(taskParams);
        try {
            doCovertHandlerByPosts(sourceAttachmentType, deleteOldAttachment, uploadAllInAttachment, uploadAllInPost);
            stopWatch.stop();
            String res = MessageFormat.format(
                    "Covert attachment handler has finished!\n{0}", stopWatch.prettyPrint());
            log.info(res);
            return new AsyncResult<>(res);
        } catch (Exception e) {
            String res = MessageFormat.format(
                    "Covert attachment handler Failed!\n{0}", e);
            log.warn(res);
            return new AsyncResult<>(res);
        }
    }

    private void doCovertHandlerByPosts(
            AttachmentType attachmentType,
            Boolean deleteOldAttachment,
            Boolean uploadAllInAttachment,
            Boolean uploadAllInPost) throws IOException {

        Map<String, Set<Integer>> pathInPosts = AttachmentHandlerCovertUtils.getPathInPost(postService.listAll());
        Map<String, Integer> pathInAttachments = AttachmentHandlerCovertUtils
                .getPathInAttachment(attachmentService.listAll(), attachmentType);
        Iterator<Map.Entry<String, Set<Integer>>> pathInPostsIterator = pathInPosts.entrySet().iterator();
        StringBuilder stringBuilder = new StringBuilder();
        while (pathInPostsIterator.hasNext()) {
            Map.Entry<String, Set<Integer>> pathInPostEntry = pathInPostsIterator.next();
            Iterator<Map.Entry<String, Integer>> pathInAttachmentsIterator = pathInAttachments.entrySet().iterator();
            while (pathInAttachmentsIterator.hasNext()) {
                Map.Entry<String, Integer> pathInAttachmentEntry = pathInAttachmentsIterator.next();
                if (attachmentInPost(pathInPostEntry.getKey(), pathInAttachmentEntry.getKey())) {
                    Attachment oldAttachment = attachmentService.getById(pathInAttachmentEntry.getValue());
                    boolean f = updatePostAttachment(
                            pathInPostEntry.getKey(),
                            oldAttachment.getName(),
                            pathInPostEntry.getValue(),
                            stringBuilder);
                    if (f && Boolean.TRUE.equals(deleteOldAttachment)) {
                        doDeleteAttachment(pathInAttachmentEntry.getValue());
                    }
                    pathInAttachmentsIterator.remove();
                    pathInPostsIterator.remove();
                    break;
                }
            }
        }

        if (Boolean.TRUE.equals(uploadAllInAttachment)) {
            doUploadAllInAttachment(pathInAttachments, deleteOldAttachment);
        }

        if (Boolean.TRUE.equals(uploadAllInPost)) {
            doUploadAllInPost(pathInPosts, stringBuilder);
        }
    }

    private void doUploadAllInAttachment(Map<String, Integer> pathInAttachments, Boolean deleteOldAttachment) {
        for (Map.Entry<String, Integer> pathInAttachmentEntry : pathInAttachments.entrySet()) {
            Attachment newAttachment = uploadFile(
                    pathInAttachmentEntry.getKey(),
                    attachmentService.getById(pathInAttachmentEntry.getValue()).getName());
            if (null != newAttachment && Boolean.TRUE.equals(deleteOldAttachment)) {
                doDeleteAttachment(pathInAttachmentEntry.getValue());
            }
        }
    }

    private void doUploadAllInPost(
            Map<String, Set<Integer>> pathInPosts, StringBuilder stringBuilder) throws IOException {

        for (Map.Entry<String, Set<Integer>> pathInPostEntry : pathInPosts.entrySet()) {
            updatePostAttachment(
                    pathInPostEntry.getKey(),
                    AttachmentHandlerCovertUtils.getBaseNameFromUrl(pathInPostEntry.getKey()),
                    pathInPostEntry.getValue(),
                    stringBuilder);
        }
    }

    /**
     * 判断附件是否被post引用
     * <p>
     * Is the attachment cited in the post
     *
     * @param pathInPost       the path in Post
     * @param pathInAttachment the path in Attachment library
     * @return Is the attachment cited in the post
     * @throws UnsupportedEncodingException url encode exception
     */
    private boolean attachmentInPost(String pathInPost, String pathInAttachment) throws UnsupportedEncodingException {

        if (null == pathInPost || null == pathInAttachment) {
            return false;
        }

        if (pathInPost.contains(pathInAttachment)) {
            return true;
        }

        if (pathInPost.contains(AttachmentHandlerCovertUtils.encodeFileBaseName(false, pathInAttachment))) {
            return true;
        }

        return AttachmentHandlerCovertUtils.encodeFileBaseName(true, pathInPost)
                .contains(AttachmentHandlerCovertUtils.encodeFileBaseName(false, pathInAttachment));
    }

    /**
     * 删除源handler的旧附件
     * <p>
     * Delete the old attachment from the source handler
     *
     * @param attachmentId attachment id
     */
    private void doDeleteAttachment(Integer attachmentId) {
        try {
            attachmentService.removePermanently(attachmentId);
        } catch (Exception e) {
            log.warn("Remove attachment permanently failed, Attachment Id: {}\n{}", attachmentId, e);
        }
    }

    /**
     * 更新post和post缩略图中的图片链接
     * <p>
     * Update the attachment path in the post
     *
     * @param oldAttachmentPath old Attachment Path
     * @param fileBaseName      file Base Name
     * @param pathInPosts       path In Posts
     * @param stringBuilder     to update post content
     * @return update success or not
     * @throws IOException Attachment delete Exception
     */
    private boolean updatePostAttachment(
            String oldAttachmentPath, String fileBaseName,
            Set<Integer> pathInPosts, StringBuilder stringBuilder) throws IOException {
        Attachment newAttachment = uploadFile(oldAttachmentPath, fileBaseName);
        if (null != newAttachment) {
            String newAttachmentPath = attachmentService.convertToDto(newAttachment).getPath();
            newAttachmentPath = AttachmentHandlerCovertUtils.encodeFileBaseName(false, newAttachmentPath);
            for (Integer postId : pathInPosts) {
                Post post = postService.getById(postId);
                stringBuilder.append(post.getOriginalContent());
                AttachmentHandlerCovertUtils.strBuilderReplaceAll(stringBuilder, oldAttachmentPath, newAttachmentPath);
                post.setOriginalContent(stringBuilder.toString());
                stringBuilder.delete(0, stringBuilder.length());
                if (attachmentInPost(post.getThumbnail(), oldAttachmentPath)) {
                    post.setThumbnail(newAttachmentPath);
                }
                postService.createOrUpdateBy(post);
            }
            return true;
        }
        return false;
    }

    /**
     * 下载图片并返回临时路径
     * <p>
     * Download the attachment and return to the temporary file path
     *
     * @param urlStr       attachment url
     * @param fileBaseName file Base Name
     * @return tmp Attachment Path
     */
    private String getTmpAttachmentPath(String urlStr, String fileBaseName) {

        if (!AttachmentHandlerCovertUtils.getImageExtension(urlStr)
                .equals(AttachmentHandlerCovertUtils.getImageExtension(fileBaseName))) {
            fileBaseName = fileBaseName + "." + AttachmentHandlerCovertUtils.getImageExtension(urlStr);
        }

        String tmpAttachmentPath = Paths.get(FileUtils.getTempDirectoryPath(), fileBaseName).toString();

        try {
            if (urlStr.startsWith("http")) {
                try {
                    AttachmentHandlerCovertUtils.downloadFile(urlStr, tmpAttachmentPath);
                } catch (IOException e) {
                    log.warn("Download Failed: {}.\n{}\nTry to URLEncode...", urlStr, e);
                    AttachmentHandlerCovertUtils.downloadFile(
                            AttachmentHandlerCovertUtils.encodeFileBaseName(false, urlStr),
                            tmpAttachmentPath);
                }
            } else {
                String oldAttachmentPath = URLDecoder.decode(Paths.get(workDir, urlStr).toString(), CHARACTER_SET_JDK8);
                File oldAttachment = new File(oldAttachmentPath);
                File tmpAttachment = new File(tmpAttachmentPath);
                if (oldAttachment.exists()) {
                    FileUtils.copyFile(oldAttachment, tmpAttachment);
                    FileUtils.waitFor(tmpAttachment, 100);
                }
            }
        } catch (IOException e) {
            log.warn("Download Failed: {}\n{}", urlStr, e);
        }

        return tmpAttachmentPath;
    }

    /**
     * 将图片上传到当前handler
     * <p>
     * upload File to current handler
     *
     * @param oldAttachmentPath old Attachment Path
     * @param fileBaseName      file Base Name
     * @return new attachment or null
     */
    private Attachment uploadFile(String oldAttachmentPath, String fileBaseName) {

        File tmpAttachment = new File(
                getTmpAttachmentPath(AttachmentHandlerCovertUtils.splitStyleRule(oldAttachmentPath), fileBaseName)
        );

        try {
            return uploadAttachment(tmpAttachment);
        } catch (Exception e) {
            log.warn("Can not upload file: {}\n{}", tmpAttachment.getPath(), e);
        }
        return null;
    }

    private Attachment uploadAttachment(File tmpAttachment) throws IOException {
        try {
            MultipartFile multipartFile = AttachmentHandlerCovertUtils.getMultipartFile(tmpAttachment);
            return attachmentService.upload(multipartFile);
        } finally {
            Files.deleteIfExists(tmpAttachment.toPath());
        }
    }
}
