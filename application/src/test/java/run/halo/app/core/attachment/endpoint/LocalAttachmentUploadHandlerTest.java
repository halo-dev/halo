package run.halo.app.core.attachment.endpoint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import run.halo.app.core.attachment.AttachmentRootGetter;
import run.halo.app.core.extension.attachment.Attachment;
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

    static Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

    @BeforeEach
    void setUp() {
        uploadHandler.setClock(clock);
    }

    public static Stream<Arguments> testUploadWithRenameStrategy() {
        return Stream.of(arguments(
                "Random file name with length 10",
                """
                    {
                      "alwaysRenameFilename": true,
                      "renameStrategy": {
                        "method": "RANDOM",
                        "randomLength": 10
                      }
                    }
                    """,
                (Consumer<Attachment>) attachment -> {
                    var displayName = attachment.getSpec().getDisplayName();
                    assertTrue(displayName.startsWith("halo-"));
                    assertTrue(displayName.endsWith(".png"));
                    // halo-xxxxxx.png
                    assertEquals(4 + 10 + 5, displayName.length());
                    // fake-content
                    assertEquals(12L, attachment.getSpec().getSize());
                }),
            arguments(
                "Random file name with length 10 but without original filename",
                """
                    {
                      "alwaysRenameFilename": true,
                      "renameStrategy": {
                        "method": "RANDOM",
                        "randomLength": 10,
                        "excludeOriginalFilename": true
                      }
                    }
                    """,
                (Consumer<Attachment>) attachment -> {
                    var displayName = attachment.getSpec().getDisplayName();
                    assertFalse(displayName.startsWith("halo-"));
                    assertTrue(displayName.endsWith(".png"));
                    // halo-xxxxxx.png
                    assertEquals(10 + 4, displayName.length());
                    // fake-content
                    assertEquals(12L, attachment.getSpec().getSize());
                }),
            arguments(
                "Rename filename with UUID but exclude original filename",
                """
                    {
                      "alwaysRenameFilename": true,
                      "renameStrategy": {
                        "method": "UUID",
                        "excludeOriginalFilename": true
                      }
                    }
                    """,
                (Consumer<Attachment>) attachment -> {
                    var displayName = attachment.getSpec().getDisplayName();
                    assertFalse(displayName.startsWith("halo-"));
                    assertTrue(displayName.endsWith(".png"));
                    // xxxxxx.png
                    assertEquals(36 + 4, displayName.length());
                    // fake-content
                    assertEquals(12L, attachment.getSpec().getSize());
                }
            ),
            arguments(
                "Rename filename with UUID",
                """
                    {
                      "alwaysRenameFilename": true,
                      "renameStrategy": {
                        "method": "UUID",
                        "excludeOriginalFilename": false
                      }
                    }
                    """,
                (Consumer<Attachment>) attachment -> {
                    var displayName = attachment.getSpec().getDisplayName();
                    assertTrue(displayName.startsWith("halo-"));
                    assertTrue(displayName.endsWith(".png"));
                    // xxxxxx.png
                    assertEquals(5 + 36 + 4, displayName.length());
                    // fake-content
                    assertEquals(12L, attachment.getSpec().getSize());
                }
            ),
            arguments(
                "Rename filename with timestamp but without original filename",
                """
                    {
                        "alwaysRenameFilename": true,
                        "renameStrategy": {
                            "method": "TIMESTAMP",
                            "excludeOriginalFilename": true
                        }
                    }
                    """,
                (Consumer<Attachment>) attachment -> {
                    var expect = clock.instant().toEpochMilli() + ".png";
                    assertEquals(expect, attachment.getSpec().getDisplayName());
                }
            ),
            arguments(
                "Rename filename with timestamp",
                """
                    {
                        "alwaysRenameFilename": true,
                        "renameStrategy": {
                            "method": "TIMESTAMP"
                        }
                    }
                    """,
                (Consumer<Attachment>) attachment -> {
                    var expect = "halo-" + clock.instant().toEpochMilli() + ".png";
                    assertEquals(expect, attachment.getSpec().getDisplayName());
                }
            )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void testUploadWithRenameStrategy(String name, String config, Consumer<Attachment> assertion) {
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
        configMap.setData(Map.of("default", config));

        var uploadOption =
            UploadOption.from("halo.png", content, MediaType.IMAGE_PNG, policy, configMap);

        when(attachmentRootGetter.get()).thenReturn(tempDir);
        uploadHandler.upload(uploadOption)
            .as(StepVerifier::create)
            .assertNext(assertion)
            .verifyComplete();
    }

}