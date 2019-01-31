package cc.ryanc.halo.utils;

import cc.ryanc.halo.model.dto.BackupDto;
import cc.ryanc.halo.model.dto.Theme;
import cc.ryanc.halo.model.enums.CommonParamsEnum;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.StrUtil;
import io.github.biezhi.ome.OhMyEmail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

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

    /**
     * 获取备份文件信息
     *
     * @param dir dir
     * @return List
     */
    public static List<BackupDto> getBackUps(String dir) {
        final StrBuilder srcPathStr = new StrBuilder(System.getProperties().getProperty("user.home"));
        srcPathStr.append("/halo/backup/");
        srcPathStr.append(dir);
        final File srcPath = new File(srcPathStr.toString());
        final File[] files = srcPath.listFiles();
        final List<BackupDto> backupDtos = new ArrayList<>();
        BackupDto backupDto = null;
        // 遍历文件
        if (null != files) {
            for (File file : files) {
                if (file.isFile()) {
                    if (StrUtil.equals(file.getName(), ".DS_Store")) {
                        continue;
                    }
                    backupDto = new BackupDto();
                    backupDto.setFileName(file.getName());
                    backupDto.setCreateAt(getCreateTime(file.getAbsolutePath()));
                    backupDto.setFileType(FileUtil.getType(file));
                    backupDto.setFileSize(parseSize(file.length()));
                    backupDto.setBackupType(dir);
                    backupDtos.add(backupDto);
                }
            }
        }
        return backupDtos;
    }

    /**
     * 转换文件大小
     *
     * @param size size
     * @return String
     */
    public static String parseSize(long size) {
        if (size < CommonParamsEnum.BYTE.getValue()) {
            return size + "B";
        } else {
            size = size / 1024;
        }
        if (size < CommonParamsEnum.BYTE.getValue()) {
            return size + "KB";
        } else {
            size = size / 1024;
        }
        if (size < CommonParamsEnum.BYTE.getValue()) {
            size = size * 100;
            return size / 100 + "." + size % 100 + "MB";
        } else {
            size = size * 100 / 1024;
            return size / 100 + "." + size % 100 + "GB";
        }
    }

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
            final Date createDate = new Date(attr.creationTime().toMillis());
            return createDate;
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
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 获取所有主题
     *
     * @return List
     */
    public static List<Theme> getThemes() {
        final List<Theme> themes = new ArrayList<>();
        try {
            // 获取项目根路径
            final File basePath = new File(ResourceUtils.getURL("classpath:").getPath());
            // 获取主题路径
            final File themesPath = new File(basePath.getAbsolutePath(), "templates/themes");
            final File[] files = themesPath.listFiles();
            if (null != files) {
                Theme theme = null;
                for (File file : files) {
                    if (file.isDirectory()) {
                        if (StrUtil.equals("__MACOSX", file.getName())) {
                            continue;
                        }
                        theme = new Theme();
                        theme.setThemeName(file.getName());
                        File optionsPath = new File(themesPath.getAbsolutePath(),
                                file.getName() + "/module/options.ftl");
                        if (optionsPath.exists()) {
                            theme.setHasOptions(true);
                        } else {
                            theme.setHasOptions(false);
                        }
                        File gitPath = new File(themesPath.getAbsolutePath(), file.getName() + "/.git");
                        if (gitPath.exists()) {
                            theme.setHasUpdate(true);
                        } else {
                            theme.setHasUpdate(false);
                        }
                        themes.add(theme);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Themes scan failed：{}", e.getMessage());
        }
        return themes;
    }

    /**
     * 获取主题下的模板文件名
     *
     * @param theme theme
     * @return List
     */
    public static List<String> getTplName(String theme) {
        final List<String> tpls = new ArrayList<>();
        try {
            // 获取项目根路径
            final File basePath = new File(ResourceUtils.getURL("classpath:").getPath());
            // 获取主题路径
            final File themesPath = new File(basePath.getAbsolutePath(), "templates/themes/" + theme);
            final File modulePath = new File(themesPath.getAbsolutePath(), "module");
            final File[] baseFiles = themesPath.listFiles();
            final File[] moduleFiles = modulePath.listFiles();
            if (null != moduleFiles) {
                for (File file : moduleFiles) {
                    if (file.isFile() && file.getName().endsWith(".ftl")) {
                        tpls.add("module/" + file.getName());
                    }
                }
            }
            if (null != baseFiles) {
                for (File file : baseFiles) {
                    if (file.isFile() && file.getName().endsWith(".ftl")) {
                        tpls.add(file.getName());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to get theme template: {}", e.getMessage());
        }
        return tpls;
    }

    /**
     * 获取定制模板 格式 page_xxx
     *
     * @return List
     */
    public static List<String> getCustomTpl(String theme) {
        final List<String> tpls = new ArrayList<>();
        try {
            final File basePath = new File(ResourceUtils.getURL("classpath:").getPath());
            // 获取主题路径
            final File themePath = new File(basePath.getAbsolutePath(), "templates/themes/" + theme);
            final File[] themeFiles = themePath.listFiles();
            if (null != themeFiles && themeFiles.length > 0) {
                for (File file : themeFiles) {
                    String[] split = StrUtil.removeSuffix(file.getName(), ".ftl").split("_");
                    if (split.length == 2 && "page".equals(split[0])) {
                        tpls.add(StrUtil.removeSuffix(file.getName(), ".ftl"));
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return tpls;
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
            e.printStackTrace();
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
            e.printStackTrace();
        } finally {
            try {
                if (null != out) {
                    out.close();
                }
                if (null != in) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result.toString();
    }

}
