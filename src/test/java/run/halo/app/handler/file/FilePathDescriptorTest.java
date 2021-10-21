package run.halo.app.handler.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static run.halo.app.model.support.HaloConst.FILE_SEPARATOR;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * File path descriptor test case.
 *
 * @author guqing
 * @since 2021-10-21
 */
public class FilePathDescriptorTest {

    private FilePathDescriptor.Builder descriptorBuilder;

    @BeforeEach
    void setUp() {
        descriptorBuilder = new FilePathDescriptor.Builder()
            .setBasePath("/home/halo")
            .setSubPath("2021/10/")
            .setSeparator(FILE_SEPARATOR)
            .setAutomaticRename(false)
            .setRenamePredicate(builder -> true)
            .setOriginalName("hello.jpg");
    }

    @Test
    public void build() {
        FilePathDescriptor descriptor = descriptorBuilder.build();
        assertEquals("/home/halo/2021/10/hello.jpg", descriptor.getFullPath());
        assertEquals("2021/10/hello.jpg", descriptor.getRelativePath());
    }

    @Test
    public void autoRename() {
        FilePathDescriptor descriptor = descriptorBuilder.setAutomaticRename(true).build();
        assertNotEquals("/home/halo/2021/10/hello.jpg", descriptor.getFullPath());
        assertNotEquals("2021/10/hello.jpg", descriptor.getRelativePath());
    }

    @Test
    public void autoRenameWithPredicate() {
        FilePathDescriptor descriptor1 = descriptorBuilder.setAutomaticRename(true)
            .setRenamePredicate(builder -> false).build();
        assertEquals("/home/halo/2021/10/hello.jpg", descriptor1.getFullPath());
        assertEquals("2021/10/hello.jpg", descriptor1.getRelativePath());

        FilePathDescriptor descriptor2 = descriptorBuilder.setAutomaticRename(true)
            .setRenamePredicate(builder -> true).build();
        assertNotEquals("/home/halo/2021/10/hello.jpg", descriptor2.getFullPath());
        assertNotEquals("2021/10/hello.jpg", descriptor2.getRelativePath());
    }

    @Test
    public void separator() {
        FilePathDescriptor descriptor = descriptorBuilder.setSeparator("->").build();
        assertEquals("/home/halo->2021/10->hello.jpg", descriptor.getFullPath());
        assertEquals("2021/10->hello.jpg", descriptor.getRelativePath());
    }

    @Test
    public void nameSuffix() {
        FilePathDescriptor descriptor = descriptorBuilder.setNameSuffix("_thumbnail").build();
        assertEquals("/home/halo/2021/10/hello_thumbnail.jpg", descriptor.getFullPath());
        assertEquals("hello_thumbnail", descriptor.getName());
        assertEquals("hello_thumbnail.jpg", descriptor.getFullName());
    }

    @Test
    public void withoutExtension() {
        FilePathDescriptor descriptor = descriptorBuilder.setOriginalName("hello").build();
        assertEquals("hello", descriptor.getName());
        assertEquals("", descriptor.getExtension());
        assertEquals("hello", descriptor.getFullName());
        assertEquals("2021/10/hello", descriptor.getRelativePath());
    }
}
