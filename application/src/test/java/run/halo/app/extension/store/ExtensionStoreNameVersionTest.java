package run.halo.app.extension.store;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;

@ExtendWith(MockitoExtension.class)
class ExtensionStoreNameVersionTest {

    @Test
    void nameVersionQueryShouldDeclareNamedParameter() throws Exception {
        // the named parameter is required for the @Query binding to work with compiled classes
        // that lack the -parameters flag
        var method = ExtensionStoreRepository.class.getMethod("findAllNameVersionByNameLike", String.class);
        var param = method.getParameters()[0].getAnnotation(Param.class);
        assertThat(param).isNotNull();
        assertThat(param.value()).isEqualTo("nameLike");
    }

    @Test
    void reactiveClientShouldDelegateToRepository() {
        var repository = Mockito.mock(ExtensionStoreRepository.class);
        var entityOperations = Mockito.mock(org.springframework.data.r2dbc.core.R2dbcEntityOperations.class);
        Mockito.when(repository.findAllNameVersionByNameLike("/registry/posts/%"))
                .thenReturn(Flux.just(new NameVersion("/registry/posts/a", 1L)));
        var client = new ReactiveExtensionStoreClientImpl(repository, entityOperations);

        var result = client.listNameVersionsByNamePrefix("/registry/posts")
                .collectList()
                .block();

        assertThat(result).containsExactly(new NameVersion("/registry/posts/a", 1L));
    }

    @Test
    void blockingClientShouldReturnList() {
        var reactive = Mockito.mock(ReactiveExtensionStoreClient.class);
        Mockito.when(reactive.listNameVersionsByNamePrefix("/registry/posts"))
                .thenReturn(Flux.just(new NameVersion("/registry/posts/a", 3L)));
        var client = new ExtensionStoreClientJPAImpl(reactive);

        List<NameVersion> result = client.listNameVersionsByNamePrefix("/registry/posts");

        assertThat(result).containsExactly(new NameVersion("/registry/posts/a", 3L));
    }
}
