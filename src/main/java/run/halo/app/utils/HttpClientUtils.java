package run.halo.app.utils;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.lang.NonNull;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * Http client utilities.
 *
 * @author johnniang
 * @date 3/29/19
 */
public class HttpClientUtils {

    /**
     * Timeout (Default is 5s).
     */
    private final static int TIMEOUT = 5000;

    private HttpClientUtils() {
    }

    /**
     * Creates https client.
     *
     * @param timeout connection timeout (ms)
     * @return https client
     * @throws KeyStoreException        key store exception
     * @throws NoSuchAlgorithmException no such algorithm exception
     * @throws KeyManagementException   key management exception
     */
    @NonNull
    public static CloseableHttpClient createHttpsClient(int timeout) throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = new SSLContextBuilder()
            .loadTrustMaterial(null, (certificate, authType) -> true)
            .build();

        return HttpClients.custom()
            .setSSLContext(sslContext)
            .setSSLHostnameVerifier(new NoopHostnameVerifier())
            .setDefaultRequestConfig(getRequestConfig(timeout))
            .build();
    }

    /**
     * Gets request config.
     *
     * @param timeout connection timeout (ms)
     * @return request config
     */
    private static RequestConfig getRequestConfig(int timeout) {
        return RequestConfig.custom()
            .setConnectTimeout(timeout)
            .setConnectionRequestTimeout(timeout)
            .setSocketTimeout(timeout)
            .build();
    }


    /**
     * Multipart file resource.
     *
     * @author johnniang
     */
    public static class MultipartFileResource extends ByteArrayResource {

        private final String filename;

        public MultipartFileResource(byte[] buf, String filename) {
            super(buf);
            this.filename = filename;
        }

        @Override
        public String getFilename() {
            return this.filename;
        }

    }

}
