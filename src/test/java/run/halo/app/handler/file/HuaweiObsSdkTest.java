package run.halo.app.handler.file;

import com.obs.services.internal.ObsProperties;
import com.obs.services.internal.RestConnectionService;
import com.obs.services.internal.ServiceException;
import com.obs.services.model.HttpMethodEnum;
import java.util.Map;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class HuaweiObsSdkTest {

    /**
     * See
     * <a href="https://github.com/halo-dev/halo/issues/1909">
     * https://github.com/halo-dev/halo/issues/1909
     * </a>
     * for more.
     */
    @Test
    void shouldSetUpConnectionCorrectly() {
        var connSvc = new RestConnectionService() {

            {
                // We have to initialize the obsProperties, or we will get a NPE while setting up
                // connection.
                obsProperties = new ObsProperties();
            }

            @Override
            public Request.Builder setupConnection(HttpMethodEnum method, String bucketName,
                String objectKey,
                Map<String, String> requestParameters,
                RequestBody body) throws ServiceException {
                return super.setupConnection(method, bucketName, objectKey, requestParameters,
                    body);
            }
        };
        var builder = connSvc.setupConnection(HttpMethodEnum.GET, "fake-bucket-name",
            "fake-object-key", Map.of(), null);
        Assertions.assertNotNull(builder);
    }

}
