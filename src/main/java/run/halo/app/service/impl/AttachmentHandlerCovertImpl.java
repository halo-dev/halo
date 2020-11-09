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

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class AttachmentHandlerCovertImpl implements AttachmentHandlerCovertService {
    private final AttachmentService attachmentService;

    private final PostService postService;

    private final String workDir;

    private static final String ATTACHMENTTMPDIR = "attachment_tmp/";

    private static final Pattern r = Pattern.compile("(?<=!\\[.*]\\()(.+)(?=\\))");

    public AttachmentHandlerCovertImpl(PostService postService, AttachmentService attachmentService, HaloProperties haloProperties) {
        this.postService = postService;
        this.attachmentService = attachmentService;
        this.workDir = FileHandler.normalizeDirectory(haloProperties.getWorkDir());
    }

    public void covertByAttachment() {
        Matcher m;
        List<Attachment> oldAttachments = attachmentService.listAll();
        List<Post> posts = postService.listAll();
        StringBuilder strBuilder = new StringBuilder();

        for (Attachment oldAttachment : oldAttachments) {
            String oldAttachmentPath = oldAttachment.getPath();
            log.info("Old Attachment Path: {}", oldAttachmentPath);
            File tmpAttachment = new File(getTmpAttachmentPath(oldAttachmentPath, oldAttachment.getName()));
            if (tmpAttachment.exists()) {
                try {
                    String newAttachmentPath = uploadFile(tmpAttachment);
                    log.info("New Attachment Path: {}", newAttachmentPath);
                    for (Post post : posts) {
                        strBuilder.append(post.getOriginalContent());
                        m = r.matcher(post.getOriginalContent());
                        while (m.find()) {
                            if (m.group().contains(oldAttachmentPath)) {
                                int index = strBuilder.indexOf(m.group());
                                while (index != -1) {
                                    strBuilder.replace(index, index + m.group().length(), newAttachmentPath);
                                    index += newAttachmentPath.length();
                                    index = strBuilder.indexOf(m.group(), index);
                                }
                            }
                        }
                        post.setOriginalContent(strBuilder.toString());
                        strBuilder.delete(0, strBuilder.length());
                        postService.update(post);
                    }
                    attachmentService.remove(oldAttachment);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (!tmpAttachment.delete()) {
                        log.info("Failed to delete temporary files: {} ", tmpAttachment.getPath());
                    }
                }
            }
        }
    }

    public void covertByPost() {
        List<Post> posts = postService.listAll();

        for (Post post : posts) {
            String oc = post.getOriginalContent();
            Matcher m = r.matcher(oc);
            while (m.find()) {
                String fileName = m.group().split("/")[m.group().split("/").length - 1];
                File tmpAttachment = new File(getTmpAttachmentPath(m.group(), fileName));
                if (tmpAttachment.exists()) {
                    try {
                        String newAttachmentPath = uploadFile(tmpAttachment);
                        post.setOriginalContent(oc.replaceAll(m.group(), newAttachmentPath));
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (!tmpAttachment.delete()) {
                            log.info("Failed to delete temporary files: {} ", tmpAttachment.getPath());
                        }
                    }

                }
            }
            postService.update(post);
        }
    }

    private String getTmpAttachmentPath(String urlStr, String fileBaseName) {
        String tmpAttachmentPath = workDir + ATTACHMENTTMPDIR + fileBaseName;
        if (tmpAttachmentPath.split("\\.").length < 2) {
            tmpAttachmentPath = tmpAttachmentPath + "." + urlStr.split("\\.")[urlStr.split("\\.").length - 1];
        }
        try {
            if (urlStr.startsWith("http")) {
                AttachmentUtils.downloadFile(urlStr, tmpAttachmentPath);
            } else {
                String oldAttachmentPath = URLDecoder.decode(workDir + urlStr, "utf-8");
                File oldFile = new File(oldAttachmentPath);
                if (oldFile.exists()) {
                    FileUtils.copyFile(oldFile, new File(tmpAttachmentPath));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tmpAttachmentPath;
    }

    private String uploadFile(File tmpAttachment) throws IOException {
        MultipartFile multipartFile = AttachmentUtils.getMultipartFile(tmpAttachment);
        Attachment attachment = attachmentService.upload(multipartFile);
        return attachment.getPath();
    }
}
