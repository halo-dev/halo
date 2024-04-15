package run.halo.app.infra;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.extension.FakeExtension;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.PageRequest;
import run.halo.app.extension.ReactiveExtensionClient;

@ExtendWith(MockitoExtension.class)
class ReactiveExtensionPaginatedIteratorTest {

    @Mock
    private ReactiveExtensionClient client;

    private ReactiveExtensionPaginatedIterator<FakeExtension> service;

    @BeforeEach
    void setUp() {
        var listOptions = new ListOptions();
        service =
            new ReactiveExtensionPaginatedIterator<>(client, FakeExtension.class,
                listOptions);
        AtomicInteger i = new AtomicInteger(0);
        when(client.listBy(any(), any(), any())).thenAnswer(invocation -> {
            PageRequest pageRequest = invocation.getArgument(2);
            int pageNumber = pageRequest.getPageNumber();
            int totalElements;
            if (i.get() == 0) {
                totalElements = 990;
            } else {
                totalElements = 1000;
            }
            i.incrementAndGet();
            return Mono.just(new ListResult<>(pageNumber, pageRequest.getPageSize(),
                totalElements, generateItems(200)));
        });
    }

    @Test
    public void listTest() {
        StepVerifier.create(service.list())
            .expectNextCount(990)
            .verifyComplete();
    }

    private List<FakeExtension> generateItems(int count) {
        List<FakeExtension> items = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            items.add(new FakeExtension());
        }
        return items;
    }
}