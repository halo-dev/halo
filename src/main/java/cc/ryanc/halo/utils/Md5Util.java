package cc.ryanc.halo.utils;

import cn.hutool.core.text.StrBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.security.MessageDigest;

/**
 * <pre>
 *      获取文件hash
 * </pre>
 *
 * @author : Yawn
 * @date : 2018/12/04
 */
public class Md5Util {

    /**
     * 计算文件MD5编码
     *
     * @param file file
     *
     * @return byte
     *
     * @throws Exception Exception
     */
    private static byte[] createChecksum(MultipartFile file) throws Exception {
        final InputStream fis = file.getInputStream();

        final byte[] buffer = new byte[1024];
        final MessageDigest complete = MessageDigest.getInstance("MD5");
        int numRead;

        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);

        fis.close();
        return complete.digest();
    }

    /**
     * 生成文件hash值
     *
     * @param file file
     *
     * @return String
     *
     * @throws Exception Exception
     */
    public static String getMD5Checksum(MultipartFile file) throws Exception {
        final byte[] b = createChecksum(file);
        StrBuilder result = new StrBuilder();

        for (int i = 0; i < b.length; i++) {
            result.append(Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1));
        }
        return result.toString();
    }
}
