package run.halo.app.theme;

import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import run.halo.app.exception.ServiceException;
import run.halo.app.model.support.ThemeFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static run.halo.app.service.ThemeService.CAN_EDIT_SUFFIX;

/**
 * Theme file scanner.
 *
 * @author johnniang
 */
public enum ThemeFileScanner {

    INSTANCE;

    /**
     * Lists theme folder by absolute path.
     *
     * @param absolutePath absolutePath
     * @return List<ThemeFile> a list of theme files
     */
    @NonNull
    public List<ThemeFile> scan(@NonNull String absolutePath) {
        Assert.hasText(absolutePath, "Absolute path must not be blank");

        return scan(Paths.get(absolutePath));
    }

    /**
     * Lists theme files as tree view.
     *
     * @param rootPath theme root path must not be null
     * @return theme file tree view
     */
    @NonNull
    private List<ThemeFile> scan(@NonNull Path rootPath) {
        Assert.notNull(rootPath, "Root path must not be null");

        // Check file type
        if (!Files.isDirectory(rootPath)) {
            return Collections.emptyList();
        }

        try (Stream<Path> pathStream = Files.list(rootPath)) {
            List<ThemeFile> themeFiles = new LinkedList<>();

            pathStream.forEach(path -> {
                // Build theme file
                ThemeFile themeFile = new ThemeFile();
                themeFile.setName(path.getFileName().toString());
                themeFile.setPath(path.toString());
                themeFile.setIsFile(Files.isRegularFile(path));
                themeFile.setEditable(isEditable(path));

                if (Files.isDirectory(path)) {
                    themeFile.setNode(scan(path));
                }

                // Add to theme files
                themeFiles.add(themeFile);
            });

            // Sort with isFile param
            themeFiles.sort(new ThemeFile());

            return themeFiles;
        } catch (IOException e) {
            throw new ServiceException("Failed to list sub files", e);
        }
    }

    /**
     * Check if the given path is editable.
     *
     * @param path must not be null
     * @return true if the given path is editable; false otherwise
     */
    private boolean isEditable(@NonNull Path path) {
        Assert.notNull(path, "Path must not be null");

        boolean isEditable = Files.isReadable(path) && Files.isWritable(path);

        if (!isEditable) {
            return false;
        }

        // Check suffix
        for (String suffix : CAN_EDIT_SUFFIX) {
            if (path.toString().endsWith(suffix)) {
                return true;
            }
        }

        return false;
    }
}
