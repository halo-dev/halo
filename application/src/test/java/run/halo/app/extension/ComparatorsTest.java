package run.halo.app.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ComparatorsTest {

    @Nested
    class CompareCreationTimestamp {

        FakeExtension createFake(String name, Instant creationTimestamp) {
            var metadata = new Metadata();
            metadata.setName(name);
            metadata.setCreationTimestamp(creationTimestamp);
            var fake = new FakeExtension();
            fake.setMetadata(metadata);
            return fake;
        }

        @Test
        void desc() {
            var comparator = Comparators.compareCreationTimestamp(false);
            var now = Instant.now();
            var before = now.minusMillis(1);
            var after = now.plusMillis(1);

            var fakeNow = createFake("now", now);
            var fakeBefore = createFake("before", before);
            var fakeAfter = createFake("after", after);

            var sortedFakes = new ArrayList<>(List.of(fakeNow, fakeAfter, fakeBefore));
            sortedFakes.sort(comparator);

            assertEquals(List.of(fakeAfter, fakeNow, fakeBefore), sortedFakes);
        }

        @Test
        void asc() {
            var comparator = Comparators.compareCreationTimestamp(true);
            var now = Instant.now();
            var before = now.minusMillis(1);
            var after = now.plusMillis(1);

            var fakeNow = createFake("now", now);
            var fakeBefore = createFake("before", before);
            var fakeAfter = createFake("after", after);

            var sortedFakes = new ArrayList<>(List.of(fakeNow, fakeAfter, fakeBefore));
            sortedFakes.sort(comparator);

            assertEquals(List.of(fakeBefore, fakeNow, fakeAfter), sortedFakes);
        }
    }

    @Nested
    class CompareName {

        FakeExtension createFake(String name) {
            var metadata = new Metadata();
            metadata.setName(name);
            var fake = new FakeExtension();
            fake.setMetadata(metadata);
            return fake;
        }

        @Test
        void desc() {
            var comparator = Comparators.compareName(false);
            var fake01 = createFake("fake01");
            var fake02 = createFake("fake02");
            var fake03 = createFake("fake03");

            var sortedFakes = new ArrayList<>(List.of(fake02, fake01, fake03));
            sortedFakes.sort(comparator);

            assertEquals(List.of(fake03, fake02, fake01), sortedFakes);
        }

        @Test
        void asc() {
            var comparator = Comparators.compareName(true);
            var fake01 = createFake("fake01");
            var fake02 = createFake("fake02");
            var fake03 = createFake("fake03");

            var sortedFakes = new ArrayList<>(List.of(fake02, fake03, fake01));
            sortedFakes.sort(comparator);

            assertEquals(List.of(fake01, fake02, fake03), sortedFakes);
        }
    }
}