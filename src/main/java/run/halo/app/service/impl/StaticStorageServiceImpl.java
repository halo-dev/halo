package run.halo.app.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;
import run.halo.app.config.properties.HaloProperties;
import run.halo.app.exception.FileOperationException;
import run.halo.app.exception.ServiceException;
import run.halo.app.model.support.StaticFile;
import run.halo.app.service.StaticStorageService;
import run.halo.app.utils.FileUtils;

import javax.activation.MimetypesFileTypeMap;
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

    public StaticStorageServiceImpl(HaloProperties haloProperties) throws IOException {
        staticDir = Paths.get(haloProperties.getWorkDir(), STATIC_FOLDER);
        FileUtils.createIfAbsent(staticDir);
        this.haloProperties = haloProperties;
    }

    @Override
    public List<StaticFile> listStaticFolder() {
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
                staticFile.setMimeType(MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(path.toFile()));
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

    @Override
    public void delete(String relativePath) {
        Assert.notNull(relativePath, "Relative path must not be null");

        Path path = Paths.get(staticDir.toString(), relativePath);
        System.out.println(path.toString());

        try {
            if (path.toFile().isDirectory()) {
                FileUtils.deleteFolder(path);
            } else {
                Files.deleteIfExists(path);
            }
        } catch (IOException e) {
            throw new FileOperationException("文件 " + relativePath + " 删除失败", e);
        }
    }

    @Override
    public void createFolder(String basePath, String folderName) {
        Assert.notNull(folderName, "Folder name path must not be null");

        Path path;

        if (StringUtils.isEmpty(basePath)) {
            path = Paths.get(staticDir.toString(), folderName);
        } else {
            path = Paths.get(staticDir.toString(), basePath, folderName);
        }

        if (path.toFile().exists()) {
            throw new FileOperationException("目录 " + path.toString() + " 已存在").setErrorData(path);
        }

        try {
            FileUtils.createIfAbsent(path);
        } catch (IOException e) {
            throw new FileOperationException("目录 " + path.toString() + " 创建失败", e);
        }
    }

    @Override
    public void update(String basePath, MultipartFile file) {
        Assert.notNull(file, "Multipart file must not be null");

        Path uploadPath;

        if (StringUtils.isEmpty(basePath)) {
            uploadPath = Paths.get(staticDir.toString(), file.getOriginalFilename());
        } else {
            uploadPath = Paths.get(staticDir.toString(), basePath, file.getOriginalFilename());
        }

        if (uploadPath.toFile().exists()) {
            throw new FileOperationException("文件 " + file.getOriginalFilename() + " 已存在").setErrorData(uploadPath);
        }

        try {
            Files.createFile(uploadPath);
            file.transferTo(uploadPath);
        } catch (IOException e) {
            throw new ServiceException("上传文件失败").setErrorData(uploadPath);
        }
    }
}
