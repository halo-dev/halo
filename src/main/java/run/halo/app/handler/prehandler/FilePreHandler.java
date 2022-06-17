package run.halo.app.handler.prehandler;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author eziosudo
 * @date 2022-06-16
 */
@Component
public interface FilePreHandler {

    MultipartFile preProcess(MultipartFile file);

}
