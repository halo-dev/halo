package run.halo.app.utils;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import run.halo.app.model.entity.Attachment;
import run.halo.app.model.entity.Post;

import javax.activation.MimetypesFileTypeMap;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
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
     * Extract the image link in markdown.
     */
    private static final Pattern URL_PATTERN = Pattern.compile("(?<=!\\[.*]\\()(.+)(?=\\))");

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
        MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
        DiskFileItem item = new DiskFileItem(
                "file",
                mimeTypesMap.getContentType(file),
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
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5 * 1000);
        if (conn.getResponseCode() == 200) {
            InputStream inStream = conn.getInputStream();
            byte[] data = readInputStream(inStream);
            File tmpAttachment = new File(downloadPath);
            try (FileOutputStream outStream = new FileOutputStream(tmpAttachment)) {
                outStream.write(data);
            }
        }
        conn.disconnect();
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
     * http://test.com/test.png
     * return tes (When FILE_NAME_LIMIT=3, default 64)
     * <p>
     * http://test.com/你好你好.png
     * return 你好你 (When FILE_NAME_LIMIT=3, default 64)
     * <p>
     * http://test.com/%E4%BD%A0%E5%A5%BD%E4%BD%A0%E5%A5%BD.png
     * return 你好你 (When FILE_NAME_LIMIT=3, default 64)
     *
     * @param url Extracted url
     * @return File Base Name
     */
    public static String getBaseNameFromUrl(String url) {
        String fileBaseName = FilenameUtils.getBasename(url);
        try {
            fileBaseName = URLDecoder.decode(fileBaseName, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (fileBaseName.length() > FILE_NAME_LIMIT) {
            fileBaseName = fileBaseName.substring(0, FILE_NAME_LIMIT);
        }
        return fileBaseName;
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
        Map<String, List<Integer>> map = new HashMap<>();
        for (Post post : posts) {
            Matcher m = URL_PATTERN.matcher(post.getOriginalContent());
            while (m.find()) {
                if (null != map.get(m.group())) {
                    map.get(m.group()).add(post.getId());
                } else {
                    List<Integer> list = new ArrayList<>();
                    list.add(post.getId());
                    map.put(m.group(), list);
                }
            }
        }
        return map;
    }

    /**
     * Extract the path of all attachments,
     * the ket is attachment_path,
     * the value is attachment_id.
     *
     * @param oldAttachments old attachments
     * @return Map<String, Integer> (attachment_path,attachment_id)
     */
    public static Map<String, Integer> getPathInAttachment(List<Attachment> oldAttachments, Integer attachmentTypeId) {
        Map<String, Integer> map = new HashMap<>();
        for (Attachment attachment : oldAttachments) {
            if (-1 == attachmentTypeId || attachment.getType().getValue().equals(attachmentTypeId)) {
                map.put(attachment.getPath(), attachment.getId());
            }
        }
        return map;
    }

    public static String encodeFileBaseName(String url) throws UnsupportedEncodingException {
        return url.substring(0, url.lastIndexOf("/") + 1) + URLEncoder.encode(url.substring(url.lastIndexOf("/") + 1), "utf-8");
    }
}
