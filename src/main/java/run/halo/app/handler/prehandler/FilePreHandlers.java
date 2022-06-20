package run.halo.app.handler.prehandler;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * @author eziosudo
 * @date 2022-06-16
 */
@Component
public class FilePreHandlers {

    private final List<FilePreHandler> preHandlers;

    FilePreHandlers(ApplicationContext applicationContext) {
        preHandlers =
            applicationContext.getBeanProvider(FilePreHandler.class)
                .orderedStream()
                .collect(Collectors.toList());
    }

    /**
     * 遍历预处理方法，对输入文件进行预处理
     *
     * @param bytes 输入文件字节流
     * @return 输出预处理后的字节流
     */
    public byte[] process(byte[] bytes) {
        for (FilePreHandler filePreHandler : preHandlers) {
            bytes = filePreHandler.preProcess(bytes);
        }
        return bytes;
    }

}
