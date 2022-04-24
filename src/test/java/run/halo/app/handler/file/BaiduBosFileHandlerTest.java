package run.halo.app.handler.file;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import run.halo.app.model.support.UploadResult;

/**
 * BaiduBosFileHandler Tester.
 *
 * @author <Authors name>
 * @since <pre>4�� 24, 2022</pre>
 * @version 1.0
 */
public class BaiduBosFileHandlerTest {


    /**
     *
     * Method: upload(MultipartFile file)
     *
     */
    @Test
    public void testUpload() throws Exception {
        String protocol = "https://";
        String endPoint = "gz.bcebos.com";
        String bucketName = "koroblog";
        String domain1 = "https://file.korostudio.cn";
        String source = StringUtils.join(protocol, bucketName, "." + endPoint);
        FilePathDescriptor pathDescriptor1 = new FilePathDescriptor.Builder()
            .setBasePath(domain1)
            .setSubPath(source)
            .setOriginalName("blhx-background_and_shouhou.png")
            .build();
        UploadResult uploadResult = new UploadResult();
        uploadResult.setFilename(pathDescriptor1.getFullName());
        String fullPath1 = pathDescriptor1.getFullPath();
        assertEquals("https://file.korostudio.cn/blhx-background_and_shouhou.png",fullPath1);
        String domain2 = "";
        FilePathDescriptor pathDescriptor2 = new FilePathDescriptor.Builder()
            .setBasePath(domain2)
            .setSubPath(source)
            .setOriginalName("blhx-background_and_shouhou.png")
            .build();
        String fullPath2 = pathDescriptor2.getFullPath();
        assertEquals("https://koroblog.gz.bcebos.com/blhx-background_and_shouhou.png",fullPath2);
    }


}

