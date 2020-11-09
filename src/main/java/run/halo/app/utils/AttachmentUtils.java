package run.halo.app.utils;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.activation.MimetypesFileTypeMap;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class AttachmentUtils {

    private AttachmentUtils() {

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
}
