package run.halo.app.theme.finders.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.PageRequest;
import run.halo.app.extension.Ref;
import run.halo.app.theme.finders.CommentPublicQueryService;

@ExtendWith(MockitoExtension.class)
class CommentFinderImplTest {
    @Mock
    private CommentPublicQueryService service;

    @InjectMocks
    private CommentFinderImpl finder;

    @ParameterizedTest
    @NullAndEmptySource
    void listWithDefaults(Map<String, Object> params) {
        when(service.list(isNull(), any(PageRequest.class))).thenReturn(Mono.just(ListResult.emptyResult()));

        finder.list(params).as(StepVerifier::create).expectNextCount(1).verifyComplete();

        var page = ArgumentCaptor.forClass(PageRequest.class);
        verify(service).list(isNull(), page.capture());
        assertPage(page.getValue(), 1, 10, Sort.unsorted());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void listReplyWithDefaults(Map<String, Object> params) {
        when(service.listReply(eq("comment"), any(PageRequest.class))).thenReturn(Mono.just(ListResult.emptyResult()));

        finder.listReply("comment", params)
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();

        var page = ArgumentCaptor.forClass(PageRequest.class);
        verify(service).listReply(eq("comment"), page.capture());
        assertPage(page.getValue(), 1, 10, Sort.unsorted());
    }

    @Test
    void listWithRefAndSort() {
        when(service.list(any(Ref.class), any(PageRequest.class))).thenReturn(Mono.just(ListResult.emptyResult()));
        var ref = Map.of("group", "content.halo.run", "version", "v1alpha1", "kind", "Post", "name", "post");

        finder.list(Map.of(
                        "ref",
                        ref,
                        "page",
                        2,
                        "size",
                        3,
                        "sort",
                        List.of("spec.creationTime,desc", "metadata.name,asc")))
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();

        var capturedRef = ArgumentCaptor.forClass(Ref.class);
        var page = ArgumentCaptor.forClass(PageRequest.class);
        verify(service).list(capturedRef.capture(), page.capture());
        assertThat(capturedRef.getValue())
                .extracting(Ref::getGroup, Ref::getVersion, Ref::getKind, Ref::getName)
                .containsExactly("content.halo.run", "v1alpha1", "Post", "post");
        assertPage(
                page.getValue(), 2, 3, Sort.by(Sort.Order.desc("spec.creationTime"), Sort.Order.asc("metadata.name")));
    }

    @Test
    void listReplyWithSort() {
        when(service.listReply(eq("comment"), any(PageRequest.class))).thenReturn(Mono.just(ListResult.emptyResult()));

        finder.listReply("comment", Map.of("page", 2, "size", 3, "sort", List.of("spec.creationTime,desc")))
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();

        var page = ArgumentCaptor.forClass(PageRequest.class);
        verify(service).listReply(eq("comment"), page.capture());
        assertPage(page.getValue(), 2, 3, Sort.by(Sort.Order.desc("spec.creationTime")));
    }

    private void assertPage(PageRequest page, int number, int size, Sort sort) {
        assertThat(page.getPageNumber()).isEqualTo(number);
        assertThat(page.getPageSize()).isEqualTo(size);
        assertThat(page.getSort()).isEqualTo(sort);
    }
}
