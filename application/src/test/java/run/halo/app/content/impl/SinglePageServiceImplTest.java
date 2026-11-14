package run.halo.app.content.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.content.Contributor;
import run.halo.app.content.SinglePageQuery;
import run.halo.app.content.Stats;
import run.halo.app.core.counter.CounterService;
import run.halo.app.core.extension.Counter;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.core.user.service.UserService;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.ReactiveExtensionClient;

/** Tests for {@link SinglePageServiceImpl} list assembly. */
@ExtendWith(MockitoExtension.class)
class SinglePageServiceImplTest {

    @Mock
    ReactiveExtensionClient client;

    @Mock
    CounterService counterService;

    @Mock
    UserService userService;

    SinglePageServiceImpl singlePageService;

    @BeforeEach
    void setUp() {
        singlePageService = new SinglePageServiceImpl(client, counterService, userService);
    }

    @Test
    void shouldAssembleListedSinglePagesWithBatchQueries() {
        var page1 = singlePage("page-1", "user-1");
        page1.getStatusOrDefault().setContributors(List.of("user-2", "user-1", "user-2"));
        var page2 = singlePage("page-2", " ");

        var pageRequest = PageRequestImpl.of(1, 20);
        var query = mock(SinglePageQuery.class);
        when(query.toListOptions()).thenReturn(new ListOptions());
        when(query.toPageRequest()).thenReturn(pageRequest);
        when(client.listBy(eq(SinglePage.class), any(ListOptions.class), eq(pageRequest)))
                .thenReturn(Mono.just(new ListResult<>(1, 20, 2, List.of(page1, page2))));
        when(counterService.getByNames(anyCollection()))
                .thenReturn(Flux.just(counter("singlepages.content.halo.run/page-1", 7, 2, 3, 2)));
        when(userService.getUsersOrGhosts(anyList())).thenAnswer(invocation -> {
            List<String> names = invocation.getArgument(0);
            return Flux.fromIterable(names).map(PostServiceImplTest::userByName);
        });

        var result = singlePageService.list(query).block();

        assertThat(result).isNotNull();
        assertThat(result.getItems()).hasSize(2);

        var listedPage1 = result.getItems().get(0);
        assertThat(listedPage1.getPage()).isSameAs(page1);
        assertThat(listedPage1.getContributors())
                .extracting(Contributor::getName)
                .containsExactly("user-2", "user-1", "user-2");
        assertThat(listedPage1.getOwner().getName()).isEqualTo("user-1");
        assertThat(listedPage1.getStats().getVisit()).isEqualTo(7);
        assertThat(listedPage1.getStats().getTotalComment()).isEqualTo(3);

        var listedPage2 = result.getItems().get(1);
        assertThat(listedPage2.getContributors()).isEmpty();
        assertThat(listedPage2.getOwner().getName()).isEqualTo(UserService.GHOST_USER_NAME);
        assertThat(listedPage2.getStats()).isEqualTo(Stats.empty());

        verify(counterService, times(1)).getByNames(anyCollection());
        verify(counterService, never()).getByName(anyString());
        verify(userService, times(1)).getUsersOrGhosts(anyList());
        verify(userService, never()).getUserOrGhost(anyString());
    }

    @Test
    void shouldNotQueryRelatedResourcesWhenPageIsEmpty() {
        var pageRequest = PageRequestImpl.of(1, 20);
        var query = mock(SinglePageQuery.class);
        when(query.toListOptions()).thenReturn(new ListOptions());
        when(query.toPageRequest()).thenReturn(pageRequest);
        when(client.listBy(eq(SinglePage.class), any(ListOptions.class), eq(pageRequest)))
                .thenReturn(Mono.just(ListResult.emptyResult()));

        var result = singlePageService.list(query).block();

        assertThat(result).isNotNull();
        assertThat(result.getItems()).isEmpty();
        verify(counterService, never()).getByNames(anyCollection());
        verify(userService, never()).getUsersOrGhosts(anyList());
    }

    static SinglePage singlePage(String name, String owner) {
        var singlePage = new SinglePage();
        var metadata = new Metadata();
        metadata.setName(name);
        singlePage.setMetadata(metadata);
        var spec = new SinglePage.SinglePageSpec();
        spec.setOwner(owner);
        singlePage.setSpec(spec);
        return singlePage;
    }

    static Counter counter(String name, int visit, int upvote, int totalComment, int approvedComment) {
        return PostServiceImplTest.counter(name, visit, upvote, totalComment, approvedComment);
    }
}
