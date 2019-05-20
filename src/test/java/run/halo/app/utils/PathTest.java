package run.halo.app.utils;

import com.sun.nio.zipfs.JarFileSystemProvider;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.Collections;

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

//    @Test
//    public void getPathOfJarFileSuccessfully() throws URISyntaxException, IOException {
//        String file = "jar:file:/path/to/jar/xxx.jar!/BOOT-INF/classes!/templates/themes";
//        URI uri = new URI(file);
//        FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
//        Path path = fileSystem.getPath("/BOOT-INF/classes/templates/themes");
//
//        System.out.println("Path: " + path.toString());
//
//        Files.walk(path, 1).forEach(p -> {
//            System.out.println(p.toString());
//        });
//    }
}
