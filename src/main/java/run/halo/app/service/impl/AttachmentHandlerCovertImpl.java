package run.halo.app.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import run.halo.app.config.properties.HaloProperties;
import run.halo.app.handler.file.FileHandler;
import run.halo.app.model.entity.Attachment;
import run.halo.app.model.entity.Post;
import run.halo.app.service.AttachmentHandlerCovertService;
import run.halo.app.service.AttachmentService;
import run.halo.app.service.PostService;
import run.halo.app.utils.AttachmentUtils;
import run.halo.app.utils.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;
import java.util.regex.Matcher;

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

    public void covertByAttachment() {
        List<Attachment> oldAttachments = attachmentService.listAll();
        List<Post> posts = postService.listAll();

        for (Attachment oldAttachment : oldAttachments) {
            String oldAttachmentPath = oldAttachment.getPath();
            File tmpAttachment = new File(getTmpAttachmentPath(oldAttachmentPath, oldAttachment.getName()));
            if (tmpAttachment.exists()) {
                try {
                    String newAttachmentPath = uploadFile(tmpAttachment);
                    for (Post post : posts) {
                        postService.update(AttachmentUtils.replacePostContent(post, oldAttachmentPath, newAttachmentPath));
                    }
                    attachmentService.remove(oldAttachment);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (!tmpAttachment.delete()) {
                        log.warn("Failed to delete temporary files: {} ", tmpAttachment.getPath());
                    }
                }
            } else {
                log.warn("Can not download or copy file: {}", oldAttachmentPath);
            }
        }
    }

    public void covertByPost() {
        List<Post> posts = postService.listAll();
        for (Post post : posts) {
            String oc = post.getOriginalContent();
            Matcher m = AttachmentUtils.getUrlPattern().matcher(oc);
            while (m.find()) {
                File tmpAttachment = new File(getTmpAttachmentPath(m.group(), AttachmentUtils.getBaseNameFromUrl(m.group())));
                if (tmpAttachment.exists()) {
                    try {
                        String newAttachmentPath = uploadFile(tmpAttachment);
                        post.setOriginalContent(oc.replaceAll(m.group(), newAttachmentPath));
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (!tmpAttachment.delete()) {
                            log.warn("Failed to delete temporary files: {} ", tmpAttachment.getPath());
                        }
                    }
                } else {
                    log.warn("Can not download file: {}", m.group());
                }
            }
            postService.update(post);
        }
    }

    private String getTmpAttachmentPath(String urlStr, String fileBaseName) {

        try {
            if (fileBaseName.split("\\.").length < 2) {
                fileBaseName = fileBaseName + "." + FilenameUtils.getExtension(urlStr);
            }
            String tmpAttachmentPath = workDir + ATTACHMENT_TMP_DIR + fileBaseName;

            if (urlStr.startsWith("http")) {
                AttachmentUtils.downloadFile(urlStr, tmpAttachmentPath);
            } else {
                String oldAttachmentPath = URLDecoder.decode(workDir + urlStr, "utf-8");
                File oldFile = new File(oldAttachmentPath);
                if (oldFile.exists()) {
                    FileUtils.copyFile(oldFile, new File(tmpAttachmentPath));
                }
            }
            return tmpAttachmentPath;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }


    private String uploadFile(File tmpAttachment) throws IOException {
        MultipartFile multipartFile = AttachmentUtils.getMultipartFile(tmpAttachment);
        Attachment attachment = attachmentService.upload(multipartFile);
        return attachment.getPath();
    }
}
