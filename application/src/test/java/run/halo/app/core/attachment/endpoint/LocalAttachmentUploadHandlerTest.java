package run.halo.app.core.attachment.endpoint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import run.halo.app.core.attachment.AttachmentRootGetter;
import run.halo.app.core.extension.attachment.Policy;
import run.halo.app.core.extension.attachment.endpoint.UploadOption;
import run.halo.app.extension.ConfigMap;

@ExtendWith(MockitoExtension.class)
class LocalAttachmentUploadHandlerTest {

    @InjectMocks
    LocalAttachmentUploadHandler uploadHandler;

    @Mock
    AttachmentRootGetter attachmentRootGetter;

    @TempDir
    Path tempDir;

    @Test
    void shouldUploadWithRandomFilename() {
        assertNotNull(uploadHandler);
        var dataBufferFactory = new DefaultDataBufferFactory();
        var dataBuffer = dataBufferFactory.allocateBuffer(1024);
        dataBuffer.write("fake content".getBytes(StandardCharsets.UTF_8));
        var content = Flux.<DataBuffer>just(dataBuffer);

        var policy = new Policy();
        var policySpec = new Policy.PolicySpec();
        policy.setSpec(policySpec);
        policySpec.setTemplateName("local");

        var configMap = new ConfigMap();
        configMap.setData(Map.of("default", """
            {
              "alwaysRandomizeFilename": true,
              "randomLength": 10
            }
            """));

        var uploadOption =
            UploadOption.from("halo.png", content, MediaType.IMAGE_PNG, policy, configMap);

        when(attachmentRootGetter.get()).thenReturn(tempDir);
        uploadHandler.upload(uploadOption)
            .as(StepVerifier::create)
            .assertNext(attachment -> {
                var displayName = attachment.getSpec().getDisplayName();
                assertTrue(displayName.startsWith("halo-"));
                assertTrue(displayName.endsWith(".png"));
                // halo-xxxxxx.png
                assertEquals(19, displayName.length());
                // fake-content
                assertEquals(12L, attachment.getSpec().getSize());
            })
            .verifyComplete();
    }
}