package run.halo.app.extension;

import static org.assertj.core.api.Assertions.*;

import io.r2dbc.spi.ConnectionFactories;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.repository.support.R2dbcRepositoryFactory;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import run.halo.app.extension.index.IndexEngineTestSupport;
import run.halo.app.extension.index.IndexSnapshotWriteBarrier;
import run.halo.app.extension.index.IndexSpecs;
import run.halo.app.extension.index.query.Queries;
import run.halo.app.extension.store.ExtensionStoreRepository;
import run.halo.app.extension.store.ReactiveExtensionStoreClientImpl;

class IndexSnapshotWriteContractIntegrationTest {
    @Test
    void indexConstraintFailuresMustRollBackDatabaseWrites() throws Exception {
        var factory = ConnectionFactories.get("r2dbc:h2:mem:///unique-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        var template = new R2dbcEntityTemplate(factory);
        template.getDatabaseClient()
                .sql("CREATE TABLE \"extensions\" (name VARCHAR(255) PRIMARY KEY, data BLOB, version BIGINT)")
                .then()
                .block();
        var repository = new R2dbcRepositoryFactory(template).getRepository(ExtensionStoreRepository.class);
        var writes = new IndexSnapshotWriteBarrier();
        var store = new ReactiveExtensionStoreClientImpl(repository, template, writes);
        var engine = IndexEngineTestSupport.newIndexEngine();
        var schemes = new DefaultSchemeManager(
                engine, event -> {}, org.mockito.Mockito.mock(org.springframework.beans.factory.ObjectProvider.class));
        schemes.register(
                ConfigMap.class,
                specs -> specs.add(IndexSpecs.<ConfigMap, String>single("businessKey", String.class)
                        .indexFunc(config -> config.getData().get("key"))
                        .unique(true)
                        .nullable(false)
                        .build()));
        var client = new ReactiveExtensionClientImpl(
                store,
                new JSONExtensionConverter(schemes),
                schemes,
                Unstructured.OBJECT_MAPPER,
                engine,
                new R2dbcTransactionManager(factory));
        try {
            var first = client.create(resource("first", "same")).block();
            var stale = client.get(ConfigMap.class, "first").block();
            first.setData(Map.of("key", "newer"));
            first = client.update(first).block();
            stale.setData(Map.of("key", "stale"));
            assertThatThrownBy(() -> client.update(stale).block())
                    .isInstanceOf(org.springframework.dao.OptimisticLockingFailureException.class);
            assertThat(writes.isReusable()).isTrue();
            assertThat(engine.retrieveAll(
                            ConfigMap.class,
                            ListOptions.builder()
                                    .andQuery(Queries.equal("businessKey", "newer"))
                                    .build(),
                            null))
                    .containsExactly("first");
            assertThat(engine.retrieveAll(
                            ConfigMap.class,
                            ListOptions.builder()
                                    .andQuery(Queries.equal("businessKey", "stale"))
                                    .build(),
                            null))
                    .isEmpty();
            first.setData(Map.of("key", "same"));
            first = client.update(first).block();
            assertThatThrownBy(() -> client.create(resource("second", "same")).block())
                    .isInstanceOf(org.springframework.dao.DuplicateKeyException.class);
            assertThat(repository.count().block()).isEqualTo(1L);
            assertThat(client.fetch(ConfigMap.class, "second").block()).isNull();
            var other = client.create(resource("other", "different")).block();
            other.setData(Map.of("key", "same"));
            assertThatThrownBy(() -> client.update(other).block())
                    .isInstanceOf(org.springframework.dao.DuplicateKeyException.class);
            assertThat(client.fetch(ConfigMap.class, "other").block().getData()).containsEntry("key", "different");
            assertThat(first.getData()).containsEntry("key", "same");
            var missingKey = resource("missing-key", "temporary");
            missingKey.setData(Map.of());
            assertThatThrownBy(() -> client.create(missingKey).block())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("not nullable");
            assertThat(client.fetch(ConfigMap.class, "missing-key").block()).isNull();
            assertThat(repository.count().block()).isEqualTo(2L);
            // Conservatively avoid persisting any uncertainty left by rolled-back transactions.

        } finally {
            client.destroy();
        }
    }

    private ConfigMap resource(String name, String key) {
        var resource = new ConfigMap();
        var metadata = new Metadata();
        metadata.setName(name);
        resource.setMetadata(metadata);
        resource.setData(Map.of("key", key));
        return resource;
    }
}
