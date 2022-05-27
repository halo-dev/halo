package run.halo.app.infra.utils;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

/**
 * @author guqing
 * @date 2022-04-12
 */
public class HaloUtils {
    public static boolean isAjaxRequest(HttpServletRequest request) {
        String requestedWith = request.getHeader("x-requested-with");
        return requestedWith == null || requestedWith.equalsIgnoreCase("XMLHttpRequest");
    }

    public static String simpleUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * <p>Read the file under the classpath as a string.</p>
     *
     * @param location the file location relative to classpath
     * @return file content
     */
    public static String readClassPathResourceAsString(String location) {
        ClassPathResource classPathResource = new ClassPathResource(location);
        try (InputStream inputStream = classPathResource.getInputStream()) {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException(
                String.format("Failed to read class path file as string from location [%s]",
                    location), e);
        }
    }
}
