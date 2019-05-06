package run.halo.app.utils;

import cn.hutool.core.text.StrBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.UUID;

/**
 * <pre>
 * 常用工具
 * </pre>
 *
 * @author ryanwang
 * @date : 2017/12/22
 */
@Slf4j
public class HaloUtils {

    /**
     * Time format.
     *
     * @param totalSeconds seconds
     * @return formatted time
     */
    @NonNull
    public static String timeFormat(long totalSeconds) {
        if (totalSeconds <= 0) {
            return "0 second";
        }

        StringBuilder timeBuilder = new StringBuilder();

        long hours = totalSeconds / 3600;
        long minutes = totalSeconds % 3600 / 60;
        long seconds = totalSeconds % 3600 % 60;

        if (hours > 0) {
            if (StringUtils.isNotBlank(timeBuilder)) {
                timeBuilder.append(", ");
            }
            timeBuilder.append(pluralize(hours, "hour", "hours"));
        }

        if (minutes > 0) {
            if (StringUtils.isNotBlank(timeBuilder)) {
                timeBuilder.append(", ");
            }
            timeBuilder.append(pluralize(minutes, "minute", "minutes"));
        }

        if (seconds > 0) {
            if (StringUtils.isNotBlank(timeBuilder)) {
                timeBuilder.append(", ");
            }
            timeBuilder.append(pluralize(seconds, "second", "seconds"));
        }

        return timeBuilder.toString();
    }

    /**
     * Pluralize the times label format.
     *
     * @param times       times
     * @param label       label
     * @param pluralLabel plural label
     * @return pluralized format
     */
    @NonNull
    public static String pluralize(long times, @NonNull String label, @NonNull String pluralLabel) {
        Assert.hasText(label, "Label must not be blank");
        Assert.hasText(pluralLabel, "Plural label must not be blank");

        if (times <= 0) {
            return "no " + pluralLabel;
        }

        if (times == 1) {
            return times + " " + label;
        }

        return times + " " + pluralLabel;
    }

    /**
     * Gets random uuid without dash.
     *
     * @return random uuid without dash
     */
    @NonNull
    public static String randomUUIDWithoutDash() {
        return StringUtils.remove(UUID.randomUUID().toString(), '-');
    }

    /**
     * Initialize url if blank.
     *
     * @param url url can be blank
     * @return initial url
     */
    @NonNull
    public static String initializeUrlIfBlank(@Nullable String url) {
        if (!StringUtils.isBlank(url)) {
            return url;
        }
        return String.valueOf(System.currentTimeMillis());
    }

    /**
     * Normalize url.
     *
     * @param url url must not be blank
     * @return normalized url
     */
    @NonNull
    public static String normalizeUrl(@NonNull String url) {
        Assert.hasText(url, "Url must not be blank");

        StringUtils.removeEnd(url, "html");
        StringUtils.removeEnd(url, "htm");

        return SlugUtils.slugify(url);
    }

    /**
     * Gets machine IP address.
     *
     * @return current machine IP address.
     */
    public static String getMachineIP() {
        InetAddress machineAddress;
        try {
            machineAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            machineAddress = InetAddress.getLoopbackAddress();
        }
        return machineAddress.getHostAddress();
    }

//    /**
//     * 获取备份文件信息
//     *
//     * @param dir dir
//     * @return List
//     */
//    public static List<BackupDto> getBackUps(String dir) {
//        final StrBuilder srcPathStr = new StrBuilder(System.getProperties().getProperty("user.home"));
//        srcPathStr.append("/halo/backup/");
//        srcPathStr.append(dir);
//        final File srcPath = new File(srcPathStr.toString());
//        final File[] files = srcPath.listFiles();
//        final List<BackupDto> backupDtos = new ArrayList<>();
//        BackupDto backupDto;
//        // 遍历文件
//        if (null != files) {
//            for (File file : files) {
//                if (file.isFile()) {
//                    if (StrUtil.equals(file.getName(), ".DS_Store")) {
//                        continue;
//                    }
//                    backupDto = new BackupDto();
//                    backupDto.setFileName(file.getName());
//                    backupDto.setCreateAt(getCreateTime(file.getAbsolutePath()));
//                    backupDto.setFileType(FileUtil.getType(file));
//                    backupDto.setFileSize(parseSize(file.length()));
//                    backupDto.setBackupType(dir);
//                    backupDtos.add(backupDto);
//                }
//            }
//        }
//        return backupDtos;
//    }

//    /**
//     * 转换文件大小
//     *
//     * @param size size
//     * @return String
//     */
//    public static String parseSize(long size) {
//        if (size < CommonParamsEnum.BYTE.getValue()) {
//            return size + "B";
//        } else {
//            size = size / 1024;
//        }
//        if (size < CommonParamsEnum.BYTE.getValue()) {
//            return size + "KB";
//        } else {
//            size = size / 1024;
//        }
//        if (size < CommonParamsEnum.BYTE.getValue()) {
//            size = size * 100;
//            return size / 100 + "." + size % 100 + "MB";
//        } else {
//            size = size * 100 / 1024;
//            return size / 100 + "." + size % 100 + "GB";
//        }
//    }

    /**
     * 获取文件创建时间
     *
     * @param srcPath 文件绝对路径
     * @return 时间
     */
    public static Date getCreateTime(String srcPath) {
        try {
            BasicFileAttributes basicFileAttributes = Files.readAttributes(Paths.get(srcPath), BasicFileAttributes.class);
            return new Date(basicFileAttributes.creationTime().toMillis());
        } catch (IOException e) {
            throw new RuntimeException("Failed to open the " + srcPath + " file", e);
        }
    }

    /**
     * 获取文件长和宽
     *
     * @param file file
     * @return String
     */
    public static String getImageWh(File file) {
        try {
            final BufferedImage image = ImageIO.read(new FileInputStream(file));
            return image.getWidth() + "x" + image.getHeight();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get read image file", e);
        }
    }

    /**
     * 导出为文件
     *
     * @param data     内容
     * @param filePath 保存路径
     * @param fileName 文件名
     */
    public static void postToFile(String data, String filePath, String fileName) throws IOException {
        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;
        try {
            final File file = new File(filePath);
            if (!file.exists()) {
                file.mkdirs();
            }
            fileWriter = new FileWriter(file.getAbsoluteFile() + "/" + fileName, true);
            bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(data);
        } catch (Exception e) {
            throw new RuntimeException("Failed to export file", e);
        } finally {
            if (null != bufferedWriter) {
                bufferedWriter.close();
            }
            if (null != fileWriter) {
                fileWriter.close();
            }
        }
    }

    /**
     * 百度主动推送
     *
     * @param blogUrl 博客地址
     * @param token   百度推送token
     * @param urls    文章路径
     * @return String
     */
    public static String baiduPost(String blogUrl, String token, String urls) {
        Assert.hasText(blogUrl, "blog url must not be blank");
        Assert.hasText(token, "token must not be blank");
        Assert.hasText(urls, "urls must not be blank");

        final StrBuilder url = new StrBuilder("http://data.zz.baidu.com/urls?site=");
        url.append(blogUrl);
        url.append("&token=");
        url.append(token);

        final StrBuilder result = new StrBuilder();
        PrintWriter out = null;
        BufferedReader in = null;
        try {
            // 建立URL之间的连接
            final URLConnection conn = new URL(url.toString()).openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("Host", "data.zz.baidu.com");
            conn.setRequestProperty("User-Agent", "curl/7.12.1");
            conn.setRequestProperty("Content-Length", "83");
            conn.setRequestProperty("Content-Type", "text/plain");

            // 发送POST请求必须设置如下两行
            conn.setDoInput(true);
            conn.setDoOutput(true);

            // 获取conn对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            out.print(urls.trim());
            // 进行输出流的缓冲
            out.flush();
            // 通过BufferedReader输入流来读取Url的响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to push posts to baidu", e);
        } finally {
            try {
                if (null != out) {
                    out.close();
                }
                if (null != in) {
                    in.close();
                }
            } catch (IOException ex) {
                // Ignore this exception
            }
        }
        return result.toString();
    }

}
