package run.halo.app.handler.prehandler;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author eziosudo
 * @date 2022-06-19
 */
public class ByteMultipartFile implements MultipartFile {

    private final byte[] imgContent;
    private final String originalFilename;
    private final String name;
    private final String contentType;

    public ByteMultipartFile(byte[] imgContent, String originalFilename, String name,
                             String contentType) {
        this.imgContent = imgContent;
        this.originalFilename = originalFilename;
        this.name = name;
        this.contentType = contentType;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getOriginalFilename() {
        return this.originalFilename;
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public boolean isEmpty() {
        return imgContent == null || imgContent.length == 0;
    }

    @Override
    public long getSize() {
        return imgContent.length;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return imgContent;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(imgContent);
    }

    @Override
    public void transferTo(@NotNull File dest) throws IOException, IllegalStateException {
        FileCopyUtils.copy(imgContent, dest);
    }
}
