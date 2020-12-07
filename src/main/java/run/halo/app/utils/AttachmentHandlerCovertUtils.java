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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Attachment Handler Covert utilities.
 *
 * @author xcp
 * @date 2020-11-07
 */
public class AttachmentHandlerCovertUtils {

    /**
     * file name length limit, maximum file name length without extension,
     * only works when downloading attachments from the Internet.
     */
    private static final int FILE_NAME_LIMIT = 64;

    /**
     * Extract the image link in markdown. Require url to have extension
     * {0,1000} Image title length is 0-1000
     * {1,1000} Image Url length is 1-1000
     * {1,100} Image extension length is 1-100
     */
    private static final Pattern PICTURE_MD_JDK8 = Pattern.compile("(?<=!\\[.{0,1000}]\\()([^\\[]{1,1000}\\.[^)]{1,100})(?=\\))");

    private static final Integer CONNECT_TIME_OUT = 5 * 1000; // 建立链接超时
    private static final Integer READ_TIME_OUT = 60 * 1000; // 下载超时
    private static final String CHARACTER_SET_JDK8 = "utf-8";

    private static final String[] IMAGE_FORMATS = ".jpg,.png,.gif,.bmp,.webp,.ico,.tiff,.tif,.svg".split(",");

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
        byte[] data = readInputStream(inStream);
        File tmpAttachment = new File(downloadPath);
        try (FileOutputStream outStream = new FileOutputStream(tmpAttachment)) {
            outStream.write(data);
        }

    }

    public static byte[] readInputStream(InputStream inStream) throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        inStream.close();
        return outStream.toByteArray();
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
        int separatorLastIndex = StringUtils.lastIndexOf(url, '/');
        int dotLastIndex = StringUtils.lastIndexOf(url, '.');

        if (separatorLastIndex != -1 && separatorLastIndex < url.length() - 1 && dotLastIndex > separatorLastIndex) {

            String fileBaseName = splitStyleRule(url.substring(separatorLastIndex + 1, dotLastIndex));
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
        return url.substring(lastI + 1);
    }


    /**
     * replaceAll of StringBuilder, like String.replaceAll.
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
     * Extract all image links in the posts,
     * the key is image_path,
     * the value is list of post_id containing the image_path.
     *
     * @param posts all posts
     * @return Map<String, List < Integer>> (image_path, list of post_id)
     */
    public static Map<String, List<Integer>> getPathInPost(List<Post> posts) {
        Matcher m;
        Map<String, List<Integer>> map = new HashMap<>();
        for (Post post : posts) {
            m = PICTURE_MD_JDK8.matcher(post.getOriginalContent());
            while (m.find() && !"".equals(m.group())) {
                if (null != map.get(m.group())) {
                    map.get(m.group()).add(post.getId());
                } else {
                    List<Integer> list = new ArrayList<>();
                    list.add(post.getId());
                    map.put(m.group(), list);
                }
            }

            if (null != post.getThumbnail() && !"".equals(post.getThumbnail())) {
                if (null != map.get(post.getThumbnail())) {
                    map.get(post.getThumbnail()).add(post.getId());
                } else {
                    List<Integer> list = new ArrayList<>();
                    list.add(post.getId());
                    map.put(post.getThumbnail(), list);
                }
            }
        }
        return map;
    }

    /**
     * Extract the path of all attachments,
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
}
