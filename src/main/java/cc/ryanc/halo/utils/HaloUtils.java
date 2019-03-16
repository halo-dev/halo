package cc.ryanc.halo.utils;

import cc.ryanc.halo.model.support.Theme;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.StrUtil;
import com.qiniu.common.Zone;
import io.github.biezhi.ome.OhMyEmail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import static cc.ryanc.halo.model.support.HaloConst.OPTIONS;

/**
 * <pre>
 * 常用工具
 * </pre>
 *
 * @author : RYAN0UP
 * @date : 2017/12/22
 */
@Slf4j
public class HaloUtils {

    public final static int DEFAULT_PAGE_SIZE = 10;

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
//     * Gets default page size.
//     *
//     * @return default page size
//     */
//    public static int getDefaultPageSize() {
//        if (StrUtil.isNotBlank(OPTIONS.get(BlogPropertiesEnum.INDEX_POSTS.getProp()))) {
//            return Integer.parseInt(OPTIONS.get(BlogPropertiesEnum.INDEX_POSTS.getProp()));
//        }
//
//        return DEFAULT_PAGE_SIZE;
//    }

    /**
     * Gets default qiniuyun zone.
     *
     * @return qiniuyun zone
     */
    @NonNull
    public static Zone getDefaultQiniuZone() {
        // Get zone from setting
        String qiniuZone = OPTIONS.get("qiniu_zone");

        if (StrUtil.isBlank(qiniuZone)) {
            return Zone.autoZone();
        }

        Zone zone;

        switch (qiniuZone) {
            case "z0":
                zone = Zone.zone0();
                break;
            case "z1":
                zone = Zone.zone1();
                break;
            case "z2":
                zone = Zone.zone2();
                break;
            case "na0":
                zone = Zone.zoneNa0();
                break;
            case "as0":
                zone = Zone.zoneAs0();
                break;
            default:
                // Default is detecting zone automatically
                zone = Zone.autoZone();
        }

        return zone;
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
        final Path path = Paths.get(srcPath);
        final BasicFileAttributeView basicview = Files.getFileAttributeView(path, BasicFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
        BasicFileAttributes attr;
        try {
            attr = basicview.readAttributes();
            return new Date(attr.creationTime().toMillis());
        } catch (Exception e) {
            e.printStackTrace();
        }
        final Calendar cal = Calendar.getInstance();
        cal.set(1970, 0, 1, 0, 0, 0);
        return cal.getTime();
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
            log.error("Failed to get read image file", e);
        }
        return "";
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
            log.error("Failed to export file", e);
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
     * 配置邮件
     *
     * @param smtpHost smtpHost
     * @param userName 邮件地址
     * @param password password
     */
    public static void configMail(String smtpHost, String userName, String password) {
        Assert.hasText(smtpHost, "SMTP host config must not be blank");
        Assert.hasText(userName, "Email username must not be blank");
        Assert.hasText(password, "Email password must not be blank");

        final Properties properties = OhMyEmail.defaultConfig(false);
        properties.setProperty("mail.smtp.host", smtpHost);
        OhMyEmail.config(properties, userName, password);
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
            log.error("Failed to create baidu post", e);
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
