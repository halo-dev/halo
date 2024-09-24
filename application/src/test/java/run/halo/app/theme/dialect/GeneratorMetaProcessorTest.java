package run.halo.app.theme.dialect;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.ResourceUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import run.halo.app.infra.InitializationStateGetter;
import run.halo.app.theme.ThemeContext;
import run.halo.app.theme.ThemeResolver;

@SpringBootTest
@AutoConfigureWebTestClient
class GeneratorMetaProcessorTest {

    @Autowired
    WebTestClient webClient;

    @MockitoBean
    InitializationStateGetter initializationStateGetter;

    @MockitoBean
    ThemeResolver themeResolver;

    @BeforeEach
    void setUp() throws FileNotFoundException, URISyntaxException {
        when(initializationStateGetter.userInitialized()).thenReturn(Mono.just(true));
        var themeContext = ThemeContext.builder()
            .name("default")
            .path(Path.of(ResourceUtils.getURL("classpath:themes/default").toURI()))
            .active(true)
            .build();
        when(themeResolver.getTheme(any(ServerWebExchange.class)))
            .thenReturn(Mono.just(themeContext));
    }

    @Test
    void requestIndexPage() {
        webClient.get().uri("/")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .consumeWith(System.out::println)
            .xpath("/html/head/meta[@name=\"generator\"][starts-with(@content, \"Halo \")]")
            .exists();
    }

}
