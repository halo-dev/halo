package run.halo.app.service.impl;

import net.bytebuddy.implementation.bytecode.Throw;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import run.halo.app.Application;
import run.halo.app.exception.FileOperationException;
import run.halo.app.exception.ForbiddenException;

import java.nio.file.Path;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class StaticStorageServiceImplTest {

    @Autowired
    private StaticStorageServiceImpl staticStorageService;

    @Test
    public void testSaveInvalidPath() {
        Throwable throwableOne = null;

        // path outside of parent
        try {
            staticStorageService.save("../test", "Content");
        } catch (Exception e) {
            throwableOne = e;
        }

        Assert.assertNotNull(throwableOne);
        Assert.assertTrue(throwableOne instanceof ForbiddenException);
        Assert.assertTrue(throwableOne.getMessage().startsWith("你没有权限访问"));

        Throwable throwableTwo = null;

        // path not exist
        try {
            staticStorageService.save("/test/$@@%@#@&", "Content");
        } catch (Exception e) {
            throwableTwo = e;
        }

        Assert.assertNotNull(throwableTwo);
        Assert.assertTrue(throwableTwo instanceof FileOperationException);
        Assert.assertTrue(throwableTwo.getMessage().endsWith("不合法"));
    }
}
