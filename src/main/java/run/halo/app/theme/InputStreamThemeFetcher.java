package run.halo.app.theme;

import run.halo.app.exception.ServiceException;
import run.halo.app.handler.theme.config.support.ThemeProperty;
import run.halo.app.utils.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

/**
 * Input stream
 */
public class InputStreamThemeFetcher implements ThemeFetcher {
    @Override
    public boolean support(Object source) {
        return source instanceof InputStream;
    }

    @Override
    public ThemeProperty fetch(Object source) {

        try (var zis = new ZipInputStream((InputStream) source)) {
            final var tempDirectory = FileUtils.createTempDirectory();
        } catch (IOException e) {
            throw new ServiceException("主题上传失败！", e);
        }

        return null;
    }
}
