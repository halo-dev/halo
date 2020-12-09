package run.halo.app.utils;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import run.halo.app.model.entity.Attachment;
import run.halo.app.model.entity.Post;
import run.halo.app.model.enums.AttachmentType;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Attachment Handler Covert utilities.
 *
 * @author xcp
 * @date 2020-11-07
 */
public class AttachmentHandlerCovertUtils {

    /**
     * 文件名长度限制，只在从网络上抓取图片时使用
     * <p>
     * file name length limit, maximum file name length without extension,
     * only works when downloading attachments from the Internet.
     */
    private static final int FILE_NAME_LIMIT = 64;

    private static final Integer CONNECT_TIME_OUT = 5 * 1000; // 建立链接超时 毫秒
    private static final Integer READ_TIME_OUT = 60 * 1000; // 下载超时 毫秒
    private static final String CHARACTER_SET_JDK8 = "utf-8";

    // 图片链接应该为以下后缀或以下后缀 + style-rule
    private static final String[] IMAGE_FORMATS = ".jpg,.jpeg,.png,.gif,.bmp,.webp,.ico,.tiff,.tif,.svg,.emf".split(",");

    private AttachmentHandlerCovertUtils() {
    }

    /**
     * Covert File to MultipartFile.
     *
     * @param file need to covert
     * @return new multipartFile
     * @throws IOException covert failed
     */
    public static MultipartFile getMultipartFile(File file) throws IOException {
        String url = file.getAbsolutePath();
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String mime = fileNameMap.getContentTypeFor("file://" + url);
        DiskFileItem item = new DiskFileItem(
                "file",
                mime,
                true, file.getName(),
                (int) file.length(),
                file.getParentFile()
        );

        try (OutputStream os = item.getOutputStream()) {
            os.write(FileUtils.readFileToByteArray(file));
        }
        return new CommonsMultipartFile(item);
    }


    /**
     * Download File form url.
     *
     * @param urlStr       download url
     * @param downloadPath the path to save file
     * @throws IOException download failed
     */
    public static void downloadFile(String urlStr, String downloadPath) throws IOException {

        URL url = new URL(urlStr);
        URLConnection conn = url.openConnection();
        conn.setConnectTimeout(CONNECT_TIME_OUT); // 建立链接超时
        conn.setReadTimeout(READ_TIME_OUT); // 下载超时
        conn.setRequestProperty("User-Agent", "Mozilla");
        conn.setRequestProperty("Accept", "image/*");
        InputStream inStream = conn.getInputStream();

        File tmpAttachment = new File(downloadPath);

        try (FileOutputStream outStream = new FileOutputStream(tmpAttachment)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            inStream.close();
        }
    }

    /**
     * Extract the file_base_name from Url,
     * it will be truncated when the length of the file_base_name is greater than FILE_NAME_LIMIT.
     * <p>
     * http://test.com/test.png
     * return test
     * <p>
     * http://test.com/test.png-stylerule
     * return tes (When FILE_NAME_LIMIT=3, default 64)
     * <p>
     * http://test.com/你好你好.png
     * return 你好你 (When FILE_NAME_LIMIT=3, default 64)
     * <p>
     * http://test.com/%E4%BD%A0%E5%A5%BD%E4%BD%A0%E5%A5%BD.png-stylerule
     * return 你好你 (When FILE_NAME_LIMIT=3, default 64)
     *
     * @param url Extracted url
     * @return File Base Name
     */
    public static String getBaseNameFromUrl(String url) throws UnsupportedEncodingException {
        url = splitStyleRule(url);
        int separatorLastIndex = StringUtils.lastIndexOf(url, '/');
        int dotLastIndex = StringUtils.lastIndexOf(url, '.');
        if (separatorLastIndex != -1 && separatorLastIndex < url.length() - 1 && dotLastIndex > separatorLastIndex) {
            String fileBaseName = url.substring(separatorLastIndex + 1, dotLastIndex);
            fileBaseName = URLDecoder.decode(fileBaseName, CHARACTER_SET_JDK8);
            if (fileBaseName.length() > FILE_NAME_LIMIT) {
                fileBaseName = fileBaseName.substring(0, FILE_NAME_LIMIT);
            }
            return fileBaseName;
        }
        return StringUtils.EMPTY;
    }

    public static String getImageExtension(String url) {
        int lastI = -1;
        for (String imageFormat : IMAGE_FORMATS) {
            int tmpI = url.lastIndexOf(imageFormat);
            if (tmpI > lastI) {
                lastI = tmpI;
            }
        }
        return lastI != -1 ? url.substring(lastI + 1) : "";
    }


    /**
     * replaceAll of StringBuilder, like "String.replaceAll".
     *
     * @param stringBuilder stringBuilder
     * @param oldStr        old string
     * @param newStr        new string
     */
    public static void strBuilderReplaceAll(StringBuilder stringBuilder, String oldStr, String newStr) {
        int index = stringBuilder.indexOf(oldStr);
        while (index != -1 && !oldStr.equals(newStr)) {
            stringBuilder.replace(index, index + oldStr.length(), newStr);
            index += newStr.length();
            index = stringBuilder.indexOf(oldStr, index);
        }
    }

    /**
     * 提取所有post中的所有图片链接，并且组装为map。
     * key为图片的url， value为post id 集合。
     * <p>
     * Extract all image links in the posts,
     * the key is image_path,
     * the value is Set of post_id containing the image_path.
     *
     * @param posts all posts
     * @return Map<String, List < Integer>> (image_path, list of post_id)
     */
    public static Map<String, Set<Integer>> getPathInPost(List<Post> posts) {

        Map<String, Set<Integer>> map = new HashMap<>();
        for (Post post : posts) {
            List<String> urls = getImageUrl(post.getOriginalContent());
            for (String url : urls) {
                if (null != map.get(url)) {
                    map.get(url).add(post.getId());
                } else {
                    Set<Integer> list = new HashSet<>();
                    list.add(post.getId());
                    map.put(url, list);
                }
            }

            if (null != post.getThumbnail() && !"".equals(post.getThumbnail())) {
                if (null != map.get(post.getThumbnail())) {
                    map.get(post.getThumbnail()).add(post.getId());
                } else {
                    Set<Integer> list = new HashSet<>();
                    list.add(post.getId());
                    map.put(post.getThumbnail(), list);
                }
            }
        }
        return map;
    }

    /**
     * 提取源handler的附件并组装为map。
     * key为附件的url，value为附件id
     * <p>
     * Extract the path of source attachmentType,
     * the key is attachment_path,
     * the value is attachment_id.
     *
     * @param oldAttachments old attachments
     * @return Map<String, Integer> (attachment_path,attachment_id)
     */
    public static Map<String, Integer> getPathInAttachment(List<Attachment> oldAttachments, AttachmentType attachmentType) {
        Map<String, Integer> map = new HashMap<>();
        for (Attachment attachment : oldAttachments) {
            if (attachment.getType() == attachmentType) {
                map.put(attachment.getPath(), attachment.getId());
            }
        }
        return map;
    }

    /**
     * encode url中的文件名，必要时进行decode再encode
     *
     * @param url urlString
     * @return urlString, conforming to the url specification
     * @throws UnsupportedEncodingException default utf-8
     */
    public static String encodeFileBaseName(Boolean needDecode, String url) throws UnsupportedEncodingException {
        String baseUrl = url.substring(0, url.lastIndexOf("/") + 1);
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        if (Boolean.TRUE.equals(needDecode)) {
            fileName = URLDecoder.decode(fileName, CHARACTER_SET_JDK8);
        }

        return baseUrl + URLEncoder.encode(fileName, CHARACTER_SET_JDK8).replace("+", "%20");
    }

    /**
     * 切掉图片处理策略，用来下载原图
     * <p>
     * split Style Rule to download Original image
     *
     * @param url 带图片处理策略的url
     * @return 原图url
     */
    public static String splitStyleRule(String url) {
        int lastI = -1;
        String extension = "";
        for (String imageFormat : IMAGE_FORMATS) {
            int tmpI = url.lastIndexOf(imageFormat);
            if (tmpI > lastI) {
                lastI = tmpI;
                extension = imageFormat;
            }
        }
        if (lastI != -1) {
            url = url.substring(0, lastI) + extension;
        }
        return url;
    }

    /**
     * 提取md源码中的图片链接
     * <p>
     * Extract image links in markdown source code
     *
     * @param md post.getOriginalContent
     * @return image url in post
     */
    public static List<String> getImageUrl(String md) {
        ArrayList<String> urls = new ArrayList<>();
        int i = md.indexOf("![");
        while (i != -1 && i < md.length()) {
            int nextI = md.indexOf("![", i + 2);
            int p = searchSquareBrackets(i, nextI, md);
            String url = searchSoundBrackets(p, nextI, md);
            if (null != url) {
                urls.add(url);
            }
            i = nextI;
        }
        return urls;
    }

    private static int searchSquareBrackets(int i, int nextI, String md) {
        int p = i + 2;
        int m = 0;
        while ((p < nextI || nextI == -1) && p < md.length()) {
            if (md.charAt(p) == '[') {
                m++;
            } else if (md.charAt(p) == ']') {
                if (m == 0) {
                    break;
                } else {
                    m--;
                }
            }
            p++;
        }
        return p;
    }

    private static String searchSoundBrackets(int p, int nextI, String md) {
        p++;
        int s = -1;
        int e = -1;
        int m = 0;
        if ((p < nextI || nextI == -1) && p < md.length() && md.charAt(p) == '(') {
            p++;
            s = p;
            while ((p < nextI || nextI == -1) && p < md.length()) {
                if (md.charAt(p) == '(') {
                    m++;
                } else if (md.charAt(p) == ')') {
                    if (m == 0) {
                        e = p;
                        break;
                    } else {
                        m--;
                    }
                }
                p++;
            }
        }

        if (s != -1 && e != -1 && e > s) {
            return md.substring(s, e);
        }

        return null;
    }
}
