package run.halo.app.utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;

class VmUtilsTest {
    private static RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();

    @Test
    void testGetSameLaunchCommand() {
        String cmd = VmUtils.getSameLaunchCommand();
        assertTrue(StringUtils.isNotBlank(cmd));
        // 测试命令的可执行性
        assertDoesNotThrow(()->{
            final Process exec = Runtime.getRuntime().exec(cmd);
            exec.destroy();
        });
    }

    @Test
    void testGetJvmExecutablePath() {
        String actualJvmExecutablePath = VmUtils.getJvmExecutablePath();
        assertTrue(StringUtils.isNotBlank(actualJvmExecutablePath));
        assertDoesNotThrow(()->{
            new ProcessBuilder(actualJvmExecutablePath).start();
        });
    }


    @Test
    void testGetVmArguments() {
        final List<String> vmArguments = VmUtils.getVmArguments();
        assertEquals(bean.getInputArguments(), vmArguments);
    }
    @Test
    void testGetClassPath() {
        String actualClassPath = VmUtils.getClassPath();
        assertEquals(bean.getClassPath(), actualClassPath);
    }

    @Test
    void testGetNonVmPartOfCmd() {
        assertEquals(System.getProperty("sun.java.command"), VmUtils.getNonVmPartOfCmd());
    }

    @Test
    void testGetRunningJar() {
        final String runningJar = VmUtils.getRunningJar();
        final File file = new File(runningJar);
        assertTrue(file.exists());
    }

    @Test
    void testGetUserDir() {
        assertEquals(System.getProperty("user.dir"), VmUtils.getUserDir());
    }
}

