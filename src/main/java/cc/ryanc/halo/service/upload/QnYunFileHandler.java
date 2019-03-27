package cc.ryanc.halo.service.upload;

import cc.ryanc.halo.model.support.UploadResult;
import cc.ryanc.halo.service.OptionService;
import org.springframework.web.multipart.MultipartFile;

/**
 * Qi niu yun file handler.
 *
 * @author johnniang
 * @date 3/27/19
 */
public class QnYunFileHandler implements FileHandler {

    private final OptionService optionService;

    public QnYunFileHandler(OptionService optionService) {
        this.optionService = optionService;
    }

    @Override
    public UploadResult upload(MultipartFile file) {
        return null;
    }

    @Override
    public boolean delete(String key) {
        return false;
    }
}
