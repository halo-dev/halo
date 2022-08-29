package run.halo.app.core.extension.attachment.endpoint;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

class LocalAttachmentUploadHandlerTest {

    @Test
    void pathResolveTest() {
        var root = Paths.get("/home/johnniang/halo/attachments");
        var sub = Paths.get("/photos/halo.jpeg");
        Path result = root.resolve(sub);
        System.out.println(result);

        Path relativize = root.relativize(root.resolve("photos/halo.jpeg"));
        System.out.println(relativize);
    }

}