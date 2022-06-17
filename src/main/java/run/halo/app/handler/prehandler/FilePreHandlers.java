package run.halo.app.handler.prehandler;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author eziosudo
 * @date 2022-06-16
 */
@Component
public class FilePreHandlers {

    private final Set<FilePreHandler> preHandlers = new HashSet<>(16);

    FilePreHandlers(ApplicationContext applicationContext) {
        Collection<FilePreHandler> filePreHandlers =
            applicationContext.getBeansOfType(FilePreHandler.class).values();
        if (!CollectionUtils.isEmpty(filePreHandlers)) {
            preHandlers.addAll(filePreHandlers);
        }
    }

    /**
     * 遍历预处理方法，对输入文件进行预处理
     *
     * @param file 输入文件
     */
    public void doPreProcess(MultipartFile file) {
        for (FilePreHandler filePreHandler : preHandlers) {
            filePreHandler.preProcess(file);
        }
    }

}
