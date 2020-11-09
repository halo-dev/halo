package run.halo.app.utils;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import run.halo.app.model.entity.Post;

import javax.activation.MimetypesFileTypeMap;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AttachmentUtils {

    private static final int FILE_NAME_LIMIT = 64;

    private static final Pattern r = Pattern.compile("(?<=!\\[.*]\\()(.+)(?=\\))");

    private AttachmentUtils() {

    }

    public static Pattern getPattern() {
        return r;
    }

    /**
     * Covert File to MultipartFile
     *
     * @param file need to covert
     * @return multipartFile new multipartFile
     * @throws IOException covert failed
     */
    public static MultipartFile getMultipartFile(File file) throws IOException {
        MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
        DiskFileItem item = new DiskFileItem(
                "file"
                , mimeTypesMap.getContentType(file)
                , true
                , file.getName()
                , (int) file.length()
                , file.getParentFile()
        );

        try (OutputStream os = item.getOutputStream()) {
            os.write(FileUtils.readFileToByteArray(file));
        }
        return new CommonsMultipartFile(item);
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
     * Download File form url
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
     * @return FileBaseName
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


    public static Post replacePostContent(Post post, String oldAttachmentPath, String newAttachmentPath) {
        Matcher m = r.matcher(post.getOriginalContent());
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(post.getOriginalContent());
        while (m.find()) {
            if (m.group().contains(oldAttachmentPath)) {
                int index = strBuilder.indexOf(m.group());
                while (index != -1) {
                    strBuilder.replace(index, index + m.group().length(), newAttachmentPath);
                    index += newAttachmentPath.length();
                    index = strBuilder.indexOf(m.group(), index);
                }
            }
        }
        post.setOriginalContent(strBuilder.toString());
        strBuilder.delete(0, strBuilder.length());
        return post;
    }


}
