package run.halo.app.utils;

import cn.hutool.core.lang.Tuple;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import javax.net.ssl.SSLContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.lang.NonNull;

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
    private static final int TIMEOUT = 5000;

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
    public static CloseableHttpClient createHttpsClient(int timeout)
        throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = new SSLContextBuilder()
            .loadTrustMaterial(null, (certificate, authType) -> true)
            .build();

        return resolveProxySetting(HttpClients.custom())
            .setSSLContext(sslContext)
            .setSSLHostnameVerifier(new NoopHostnameVerifier())
            .setDefaultRequestConfig(getRequestConfig(timeout))
            .build();
    }

    /**
     * resolve system proxy config
     *
     * @param httpClientBuilder the httpClientBuilder
     * @return the argument
     */
    private static HttpClientBuilder resolveProxySetting(
        final HttpClientBuilder httpClientBuilder) {
        final String httpProxyEnv = System.getenv("http_proxy");
        if (StringUtils.isNotBlank(httpProxyEnv)) {
            final Tuple httpProxy = resolveHttpProxy(httpProxyEnv);
            final HttpHost httpHost = HttpHost.create(httpProxy.get(0));
            httpClientBuilder.setProxy(httpHost);
            if (httpProxy.getMembers().length == 3) {
                //set proxy credentials
                final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider
                    .setCredentials(new AuthScope(httpHost.getHostName(), httpHost.getPort()),
                        new UsernamePasswordCredentials(httpProxy.get(1), httpProxy.get(2)));
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }
        }
        return httpClientBuilder;
    }

    /**
     * @param httpProxy http proxy env values
     * @return resolved http proxy values; first is host(@nonNull), second is username(@nullable)
     * , third is password(@nullable)
     */
    private static Tuple resolveHttpProxy(final String httpProxy) {
        final URI proxyUri = URI.create(httpProxy);
        int port = proxyUri.getPort();
        if (port == -1) {
            if (Objects.equals("http", proxyUri.getScheme())) {
                port = 80;
            }
            if (Objects.equals("https", proxyUri.getScheme())) {
                port = 443;
            }
        }
        final String hostUrl = proxyUri.getScheme() + "://" + proxyUri.getHost() + ":" + port;
        final String usernamePassword = proxyUri.getUserInfo();
        if (StringUtils.isNotBlank(usernamePassword)) {
            final String username;
            final String password;
            final int atColon = usernamePassword.indexOf(':');
            if (atColon >= 0) {
                username = usernamePassword.substring(0, atColon);
                password = usernamePassword.substring(atColon + 1);
            } else {
                username = usernamePassword;
                password = null;
            }
            return new Tuple(hostUrl, username, password);
        } else {
            return new Tuple(hostUrl);
        }
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
