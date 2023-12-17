package run.halo.app.cache;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;

/**
 * Cached response. Refer to
 * <a href="https://github.com/spring-cloud/spring-cloud-gateway/blob/f98aa6d47bf802019f07063f4fd7af6047f15116/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/filter/factory/cache/CachedResponse.java">here</a> }
 */
public record CachedResponse(HttpStatusCode statusCode,
                             HttpHeaders headers,
                             List<ByteBuffer> body,
                             Instant timestamp) {

}
