package run.halo.app.infra.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;
import org.springframework.web.reactive.function.server.ServerRequest;

/**
 * @author guqing
 * @date 2022-04-12
 */
@Slf4j
public class HaloUtils {

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

    /**
     * Gets user-agent from server request.
     *
     * @param request server request
     * @return user-agent string if found, otherwise "unknown"
     */
    public static String userAgentFrom(ServerRequest request) {
        HttpHeaders httpHeaders = request.headers().asHttpHeaders();
        // https://en.wikipedia.org/wiki/User_agent
        String userAgent = httpHeaders.getFirst(HttpHeaders.USER_AGENT);
        if (StringUtils.isBlank(userAgent)) {
            // https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Sec-CH-UA
            userAgent = httpHeaders.getFirst("Sec-CH-UA");
        }
        return StringUtils.defaultString(userAgent, "unknown");
    }

    public static String getDayText(Instant instant) {
        Assert.notNull(instant, "Instant must not be null");
        int dayValue = instant.atZone(ZoneId.systemDefault()).getDayOfMonth();
        return StringUtils.leftPad(String.valueOf(dayValue), 2, '0');
    }

    public static String getMonthText(Instant instant) {
        Assert.notNull(instant, "Instant must not be null");
        int monthValue = instant.atZone(ZoneId.systemDefault()).getMonthValue();
        return StringUtils.leftPad(String.valueOf(monthValue), 2, '0');
    }

    public static String getYearText(Instant instant) {
        Assert.notNull(instant, "Instant must not be null");
        return String.valueOf(instant.atZone(ZoneId.systemDefault()).getYear());
    }
}
