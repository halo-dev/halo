package run.halo.app.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import run.halo.app.config.properties.HaloProperties;
import run.halo.app.exception.ServiceException;
import run.halo.app.model.support.StaticFile;
import run.halo.app.service.StaticStorageService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

/**
 * StaticStorageService implementation class.
 *
 * @author ryanwang
 * @date 2019-12-06
 */
@Service
public class StaticStorageServiceImpl implements StaticStorageService {

    private final Path staticDir;

    private final HaloProperties haloProperties;

    public StaticStorageServiceImpl(HaloProperties haloProperties) {
        staticDir = Paths.get(haloProperties.getWorkDir(), STATIC_FOLDER);
        this.haloProperties = haloProperties;
    }

    @Override
    public List<StaticFile> listStaticFolder() {
        System.out.println(staticDir);
        return listStaticFileTree(staticDir);
    }

    @Nullable
    private List<StaticFile> listStaticFileTree(@NonNull Path topPath) {
        Assert.notNull(topPath, "Top path must not be null");

        if (!Files.isDirectory(topPath)) {
            return null;
        }

        try (Stream<Path> pathStream = Files.list(topPath)) {
            List<StaticFile> staticFiles = new LinkedList<>();

            pathStream.forEach(path -> {
                StaticFile staticFile = new StaticFile();
                staticFile.setName(path.getFileName().toString());
                staticFile.setPath(path.toString());
                staticFile.setRelativePath(StringUtils.removeStart(path.toString(), staticDir.toString()));
                staticFile.setIsFile(Files.isRegularFile(path));
                try {
                    staticFile.setCreateTime(Files.getLastModifiedTime(path).toMillis());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (Files.isDirectory(path)) {
                    staticFile.setChildren(listStaticFileTree(path));
                }

                staticFiles.add(staticFile);
            });

            staticFiles.sort(new StaticFile());
            return staticFiles;
        } catch (IOException e) {
            throw new ServiceException("Failed to list sub files", e);
        }
    }

}
