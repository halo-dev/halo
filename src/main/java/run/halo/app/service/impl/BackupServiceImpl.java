package run.halo.app.service.impl;

import cn.hutool.core.io.IoUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import run.halo.app.model.dto.post.BasePostDetailDTO;
import run.halo.app.service.BackupService;
import run.halo.app.service.PostService;

import java.io.IOException;

/**
 * Backup service implementation.
 *
 * @author johnniang
 * @date 19-4-26
 */
@Service
public class BackupServiceImpl implements BackupService {

    private final PostService postService;

    public BackupServiceImpl(PostService postService) {
        this.postService = postService;
    }

    @Override
    public BasePostDetailDTO importMarkdowns(MultipartFile file) throws IOException {

        // Read markdown content.
        String markdown = IoUtil.read(file.getInputStream(), "UTF-8");

        // TODO sheet import

        return postService.importMarkdown(markdown, file.getOriginalFilename());
    }
}
