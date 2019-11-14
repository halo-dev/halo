package run.halo.app.service;

import org.json.JSONObject;
import org.springframework.web.multipart.MultipartFile;
import run.halo.app.model.dto.post.BasePostDetailDTO;

import java.io.IOException;
import java.util.List;

/**
 * Backup service interface.
 *
 * @author johnniang
 * @date 2019-04-26
 */
public interface BackupService {

    /**
     * Backup posts and sheets
     *
     * @param file file
     * @return post info
     */
    BasePostDetailDTO importMarkdown(MultipartFile file) throws IOException;


    /**
     * export posts by hexo formatter
     *
     * @return
     */
    JSONObject exportHexoMDs();

    /**
     * Exports the specified articles to the specified dir path.
     *
     * @param posts
     * @param path
     */
    void exportHexoMd(List<JSONObject> posts, String path);
}
