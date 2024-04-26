package run.halo.app.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.extension.FakeExtension;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.PageRequest;
import run.halo.app.extension.ReactiveExtensionClient;

@ExtendWith(MockitoExtension.class)
class ReactiveExtensionPaginatedOperatorImplTest {

    @Mock
    private ReactiveExtensionClient client;

    @InjectMocks
    private ReactiveExtensionPaginatedOperatorImpl service;

    @Nested
    class ListTest {

        @BeforeEach
        void setUp() {
            Instant now = Instant.now();
            var items = new ArrayList<>();
            // Generate 900 items
            for (int j = 0; j < 9; j++) {
                items.addAll(generateItems(100, now));
            }
            // mock new items during the process
            Instant otherNow = now.plusSeconds(1000);
            items.addAll(generateItems(90, otherNow));

            when(client.listBy(any(), any(), any())).thenAnswer(invocation -> {
                PageRequest pageRequest = invocation.getArgument(2);
                int pageNumber = pageRequest.getPageNumber();
                var list = ListResult.subList(items, pageNumber, pageRequest.getPageSize());
                var result = new ListResult<>(pageNumber, pageRequest.getPageSize(),
                    items.size(), list);
                return Mono.just(result);
            });
        }

        @Test
        public void listTest() {
            StepVerifier.create(service.list(FakeExtension.class, new ListOptions()))
                .expectNextCount(900)
                .verifyComplete();
        }
    }

    @Test
    void nextPageTest() {
        var result = new ListResult<FakeExtension>(1, 10, 30, List.of());
        var sort = Sort.by("metadata.creationTimestamp");
        var nextPage = ReactiveExtensionPaginatedOperatorImpl.nextPage(result, sort);
        assertThat(nextPage.getPageNumber()).isEqualTo(2);
        assertThat(nextPage.getPageSize()).isEqualTo(10);
        assertThat(nextPage.getSort()).isEqualTo(sort);
    }

    @Test
    void shouldTakeNextTest() {
        var now = Instant.now();
        var item = new FakeExtension();
        item.setMetadata(new Metadata());
        item.getMetadata().setCreationTimestamp(now);
        var result = ReactiveExtensionPaginatedOperatorImpl.shouldTakeNext(item, now);
        assertThat(result).isTrue();

        item.getMetadata().setCreationTimestamp(now.minusSeconds(1));
        result = ReactiveExtensionPaginatedOperatorImpl.shouldTakeNext(item, now);
        assertThat(result).isTrue();

        item.getMetadata().setCreationTimestamp(now.plusSeconds(1));
        result = ReactiveExtensionPaginatedOperatorImpl.shouldTakeNext(item, now);
        assertThat(result).isFalse();
    }

    private List<FakeExtension> generateItems(int count, Instant creationTimestamp) {
        List<FakeExtension> items = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            var item = new FakeExtension();
            item.setMetadata(new Metadata());
            item.getMetadata().setCreationTimestamp(creationTimestamp);
            items.add(item);
        }
        return items;
    }
}