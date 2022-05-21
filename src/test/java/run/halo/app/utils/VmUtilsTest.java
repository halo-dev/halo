package run.halo.app.utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        final List<String> cmd = VmUtils.getSameLaunchCommand();
        assertFalse(cmd.isEmpty());
        // In junit test. its size should > 3
        assertTrue(cmd.size() > 3);
    }

    @Test
    void testGetSameLaunchCommand2() {
        final List<String> cmd = VmUtils.getSameLaunchCommand();
        System.out.println(cmd.get(0));
        assertTrue(cmd.get(0).matches(".*java.*"));
    }

    @Test
    void testGetJvmExecutablePath() {
        String actualJvmExecutablePath = VmUtils.getJvmExecutablePath();
        assertTrue(StringUtils.isNotBlank(actualJvmExecutablePath));
        assertDoesNotThrow(() -> {
            new ProcessBuilder(actualJvmExecutablePath).start();
        });
    }

    @Test
    void testGetJvmExecutablePath2() {
        String actualJvmExecutablePath = VmUtils.getJvmExecutablePath();
        assertTrue(actualJvmExecutablePath.matches(".*java.*"));
    }


    @Test
    void testGetVmArguments() {
        final List<String> vmArguments = VmUtils.getVmOptions();
        assertNotNull(vmArguments);
    }
    @Test
    void testGetVmArguments2() {
        final List<String> vmArguments = VmUtils.getVmOptions();
        assertEquals(bean.getInputArguments(), vmArguments);
    }

    @Test
    void testGetClassPath() {
        String actualClassPath = VmUtils.getClassPath();
        assertEquals(bean.getClassPath(), actualClassPath);
    }

    @Test
    void testGetClassPath2() {
        String actualClassPath = VmUtils.getClassPath();
        final String separator = System.getProperty("path.separator");
        final String[] classSource = actualClassPath.split(separator);
        for (String s : classSource) {
            assertTrue(new File(s).exists());
        }
    }

    @Test
    void testGetNonVmPartOfCmd() {
        assertEquals(System.getProperty("sun.java.command"), VmUtils.getNonVmPartOfCmd());
    }
    @Test
    void testGetNonVmPartOfCmd2() {
        final String args = VmUtils.getNonVmPartOfCmd();
        assertTrue(StringUtils.isNotBlank(args));
    }

    @Test
    void testGetRunningJar() {
        final String runningJar = VmUtils.getRunningJar();
        assertTrue(StringUtils.isNotBlank(runningJar));
    }
    @Test
    void testGetRunningJar2() {
        final String runningJar = VmUtils.getRunningJar();
        final File file = new File(runningJar);
        assertTrue(file.exists());
    }

    @Test
    void testGetUserDir() {
        assertEquals(System.getProperty("user.dir"), VmUtils.getUserDir());
    }
    @Test
    void testGetUserDir2() {
        final String userDir = VmUtils.getUserDir();
        assertTrue(new File(userDir).exists());
    }
}

