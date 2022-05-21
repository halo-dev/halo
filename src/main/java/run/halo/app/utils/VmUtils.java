package run.halo.app.utils;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;
import run.halo.app.exception.ServiceException;

/**
 * The utils to get some info of JVM.
 *
 * @author Chen_Kunqiu
 */
@Component
public class VmUtils {
    private static final RuntimeMXBean RUNTIME_MX_BEAN = ManagementFactory.getRuntimeMXBean();
    public static final Path CURR_JAR;
    public static final Path CURR_JAR_DIR;
    private static ApplicationArguments appArgs;

    static {
        String path =
            VmUtils.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        // As the special package form of Spring, the path would start with "file:"
        if (path.startsWith("file:")) {
            path = path.substring(5);
        }
        // If not containing ".jar", it is test scenario.
        final int idx = path.contains(".jar") ? path.indexOf(".jar") + 4 : path.length();

        CURR_JAR = new File(path.substring(0, idx)).toPath();
        CURR_JAR_DIR = CURR_JAR.getParent();
    }

    @Autowired
    private void setAppArgs(ApplicationArguments appArgs) {
        VmUtils.appArgs = appArgs;
    }

    /**
     * Get the command to launch the halo jar.
     *
     * <p>Get the same command as the command to launch this Java program.
     * Use this command to restart the Java program after halo updates.
     *
     * @return the full command
     */
    public static List<String> getSameLaunchCommand() {
        List<String> cmd = new ArrayList<>();
        cmd.add(getJvmExecutablePath());
        cmd.addAll(getVmOptions());
        cmd.add("-classpath");
        cmd.add(getClassPath());
        cmd.add("-jar");
        cmd.add(getRunningJar());
        cmd.addAll(getProgramArgs());
        return cmd;
    }

    /**
     * Get the new command to launch halo jar with new version.
     *
     *  <p>Get the same new launch command as the command to launch this Java program
     * except for the Java target to launch.
     * Use this command to restart the Java program after halo updates.
     *
     * @return the full command
     */
    public static List<String> getNewLaunchCommand(String newTarget) {
        List<String> cmd = new ArrayList<>();
        cmd.add(getJvmExecutablePath());
        cmd.addAll(getVmOptions());
        cmd.add("-classpath");
        String classPath = getClassPath();
        final String nonVmPartOfCmd = getNonVmPartOfCmd();
        final Path fileName = CURR_JAR.getFileName();
        if (fileName == null) {
            throw new ServiceException("无法获取当前运行的JAR");
        }
        final String jarName = fileName.toString();

        final int endIdx = nonVmPartOfCmd.indexOf(jarName) + jarName.length();
        /*
         * Since cannot determine whether user use relative or absolute path to launch halo.jar,
         * and CURR_JAR is absolute path,
         * use this approach to obtain the real form of halo jar's path when launching.
         */
        String originalJar = nonVmPartOfCmd.substring(0, endIdx);
        // replace the class path
        classPath = classPath.replace(originalJar, newTarget);
        cmd.add(classPath);
        cmd.add("-jar");
        cmd.add(newTarget);
        cmd.addAll(getProgramArgs());
        return cmd;
    }

    /**
     * Get the absolute path of java / javaw.
     *
     * <p>Get the full path of the JVM executable which runs the current Java program.
     * As the JVM is not always specified as JAVA_HOME, cannot get it simply by
     * {@code $JAVA_HOME/bin/java}
     *
     * @return the full path of current JVM
     */
    public static String getJvmExecutablePath() {
        // need JAVA 9+
        return ProcessHandle.current()
            .info()
            .command()
            .orElseThrow();
    }

    /**
     * Get the VM arguments passed to JVM.
     *
     * <p>For example,<br>
     * {@code java -jar -Da=1 Test.jar b=2} <br>
     * --> <br>
     * {@code -Da=1}
     *
     * @return the VM arguments
     */
    public static List<String> getVmOptions() {
        return RUNTIME_MX_BEAN.getInputArguments();
    }


    /**
     * Get the program arguments passed to JVM.
     *
     * <p>For example, <br>
     * {@code java -jar -Da=1 Test.jar b=2} <br>
     * --> <br>
     * {@code b=2}
     *
     * @return the VM arguments
     */
    public static List<String> getProgramArgs() {
        return Arrays.asList(appArgs.getSourceArgs());
    }

    /**
     * Get the class path which the JVM relies on.
     *
     * @return the class path
     */
    public static String getClassPath() {
        return RUNTIME_MX_BEAN.getClassPath();
    }

    /**
     * Get the program arguments including the target class or jar.
     *
     * <p>For example, <br>
     * {@code java -jar -Da=1 Test.jar b=2} <br>
     *  --> <br>
     * {@code Test.jar b=2}
     *
     * @return the program arguments including the target class or jar
     */
    public static String getNonVmPartOfCmd() {
        return System.getProperty("sun.java.command");
    }

    /**
     * Get the full path of the running jar.
     *
     * <p>If running class file without jar, then return the directory contains the root package.
     *
     * @return the full path of running jar.
     */
    public static String getRunningJar() {
        return CURR_JAR.toString();
    }

    /**
     * Get the full path of the directory of the running jar.
     *
     * @return the full path of the directory of the running jar.
     */
    public static String getRunningJarDir() {
        return CURR_JAR_DIR.toString();
    }

    /**
     * Get the work directory of JVM.
     *
     * @return work directory (aka. user dir)
     */
    public static String getUserDir() {
        return System.getProperty("user.dir");
    }



}
