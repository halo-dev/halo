package run.halo.app.utils;

import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Path test.
 *
 * @author johnniang
 * @date 19-5-20
 */
public class PathTest {

    @Test(expected = FileSystemNotFoundException.class)
    public void getPathOfJarFileFailure() throws URISyntaxException {
        String file = "jar:file:/path/to/jar/xxx.jar!/BOOT-INF/classes!/templates/themes";
        URI uri = new URI(file);
        Path path = Paths.get(uri);

        System.out.println("Path: " + path.toString());
    }
}
