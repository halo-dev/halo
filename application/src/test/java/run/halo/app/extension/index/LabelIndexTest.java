package run.halo.app.extension.index;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.extension.Metadata;

@ExtendWith(MockitoExtension.class)
class LabelIndexTest {

    @InjectMocks
    LabelIndex<Fake> index;

    @Nested
    class IndexOperationTests {

        @Test
        void shouldInsertCorrectly() {
            var fake = createFake("fake");
            fake.getMetadata().setLabels(Map.of("k1", "v1", "k2", "v2"));

            var op = index.prepareInsert(fake);
            op.prepare();
            op.commit();

            assertEquals(Set.of("fake"), index.equal("k1", "v1"));
            assertEquals(Set.of("fake"), index.exists("k1"));
            assertTrue(index.equal("k1", "non-existent").isEmpty());
        }

        @Test
        void shouldUpdateAndRollbackCorrectly() throws Exception {
            var fake = createFake("fake");
            fake.getMetadata().setLabels(Map.of("k1", "v1"));
            // insert
            var insert = index.prepareInsert(fake);
            insert.prepare();
            insert.commit();

            // update to v2
            fake.getMetadata().setLabels(Map.of("k1", "v2"));
            var update = index.prepareUpdate(fake);
            update.prepare();
            update.commit();

            assertTrue(index.equal("k1", "v1").isEmpty());
            assertEquals(Set.of("fake"), index.equal("k1", "v2"));

            // rollback should restore previous state (v1)
            update.rollback();
            assertEquals(Set.of("fake"), index.equal("k1", "v1"));
            assertTrue(index.equal("k1", "v2").isEmpty());
        }

        @Test
        void shouldDeleteAndRollbackCorrectly() throws Exception {
            var fake = createFake("fake");
            fake.getMetadata().setLabels(Map.of("k1", "v1"));
            insert(fake);

            var delete = index.prepareDelete("fake");
            delete.prepare();
            delete.commit();

            assertTrue(index.equal("k1", "v1").isEmpty());
            // rollback restores it
            delete.rollback();
            assertEquals(Set.of("fake"), index.equal("k1", "v1"));
        }

        @Test
        void shouldTreatEmptyLabelsAsEmptySet() throws Exception {
            var fake = createFake("empty");
            fake.getMetadata().setLabels(Map.of());
            var op = index.prepareInsert(fake);
            op.prepare();
            op.commit();

            // no entries for any label key
            assertTrue(index.exists("any").isEmpty());
            assertTrue(index.equal("any", "any").isEmpty());
        }

        @Test
        void shouldCloseIndexCorrectly() throws IOException {
            var fake = createFake("f");
            fake.getMetadata().setLabels(Map.of("k", "v"));
            insert(fake);

            index.close();

            assertTrue(index.exists("k").isEmpty());
            assertTrue(index.equal("k", "v").isEmpty());
        }
    }

    @Nested
    class QueryTests {

        @BeforeEach
        void setUp() {
            index = new LabelIndex<>(); // fresh
            var f1 = createFake("f1");
            f1.getMetadata().setLabels(Map.of("k1", "v1"));

            var f2 = createFake("f2");
            f2.getMetadata().setLabels(Map.of("k1", "v2"));

            var f3 = createFake("f3");
            f3.getMetadata().setLabels(Map.of("k1", "v3"));

            insert(f1);
            insert(f2);
            insert(f3);
        }

        @Test
        void existsQuery() {
            assertEquals(Set.of("f1", "f2", "f3"), index.exists("k1"));
            assertTrue(index.exists("non-existent").isEmpty());
        }

        @Test
        void equalAndNotEqualQuery() {
            assertEquals(Set.of("f1"), index.equal("k1", "v1"));
            assertEquals(Set.of("f2", "f3"), index.notEqual("k1", "v1"));
        }

        @Test
        void inAndNotInQuery() {
            var inResult = index.in("k1", List.of("v1", "v3", "non-existent"));
            assertEquals(Set.of("f1", "f3"), inResult);

            var notInResult = index.notIn("k1", List.of("v1", "non-existent"));
            assertEquals(Set.of("f2", "f3"), notInResult);
        }
    }

    void insert(Fake fake) {
        var op = index.prepareInsert(fake);
        op.prepare();
        op.commit();
    }

    Fake createFake(String name) {
        var fake = new Fake();
        fake.setMetadata(new Metadata());
        fake.getMetadata().setName(name);
        return fake;
    }

    @GVK(
        group = "fake.halo.app",
        version = "v1",
        kind = "Fake",
        singular = "fake",
        plural = "fakes"
    )
    @Data
    @EqualsAndHashCode(callSuper = true)
    static class Fake extends AbstractExtension {
        // ...existing code...
    }
}