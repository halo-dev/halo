package run.halo.app.extension.index;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.data.domain.Sort;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.index.query.Queries;
import run.halo.app.extension.router.selector.FieldSelector;

class DefaultIndexEngineOrderingTest {

    private static final int SIZE = 3_000;

    private final Map<String, Fake> fakes = new HashMap<>();

    private DefaultIndexEngine engine;

    @BeforeEach
    void setUp() {
        var manager = new DefaultIndicesManager();
        manager.add(
                Fake.class,
                List.of(
                        IndexSpecs.<Fake, String>single("spec.owner", String.class)
                                .indexFunc(Fake::getStringValue)
                                .build(),
                        IndexSpecs.<Fake, String>multi("spec.tags", String.class)
                                .indexFunc(Fake::getStringValues)
                                .build()));
        engine = new DefaultIndexEngine(ApplicationConversionService.getSharedInstance());
        engine.setIndicesManager(manager);

        var items = new ArrayList<Fake>(SIZE);
        for (var i = 0; i < SIZE; i++) {
            var fake = new Fake();
            fake.setMetadata(new Metadata());
            fake.getMetadata().setName("fake-%04d".formatted(i));
            fake.getMetadata().setCreationTimestamp(Instant.ofEpochSecond(i % 13));
            // Leave every tenth owner empty to mix null keys into the ordering.
            fake.setStringValue(i % 10 == 0 ? null : "owner-" + (i % 37));
            var tags = new HashSet<String>();
            // Leave some entities without tags to mix empty multi-value keys into the ordering.
            if (i % 17 != 0) {
                tags.add("t" + (i % 5));
                tags.add("t" + (i % 11));
            }
            fake.setStringValues(tags);
            fakes.put(fake.getMetadata().getName(), fake);
            items.add(fake);
        }
        engine.insert(items);
    }

    @Test
    void shouldOrderAllCandidatesBySingleKey() {
        for (var sort : List.of(Sort.by("spec.owner"), Sort.by(Sort.Direction.DESC, "spec.owner"))) {
            var expected = expectedOrder(allNames(), ordersOf(sort));
            assertThat(toList(engine.retrieveAll(Fake.class, options(), sort))).containsExactlyElementsOf(expected);
        }
    }

    @Test
    void shouldOrderNarrowCandidatesIncludingNullKeys() {
        var names = List.of("fake-0000", "fake-0001", "fake-0002", "fake-0010", "fake-0021");
        var options = new ListOptions();
        options.setFieldSelector(FieldSelector.of(Queries.in("metadata.name", names)));
        for (var sort : List.of(Sort.by("spec.owner"), Sort.by(Sort.Direction.DESC, "spec.owner"))) {
            var expected = expectedOrder(names, ordersOf(sort));
            assertThat(toList(engine.retrieveAll(Fake.class, options, sort))).containsExactlyElementsOf(expected);
        }
    }

    @Test
    void shouldOrderByMultipleSortOrders() {
        var sort = Sort.by("metadata.creationTimestamp").and(Sort.by(Sort.Direction.DESC, "metadata.name"));
        var expected = expectedOrder(allNames(), ordersOf(sort));
        assertThat(toList(engine.retrieveAll(Fake.class, options(), sort))).containsExactlyElementsOf(expected);

        var page = engine.retrieve(Fake.class, options(), PageRequestImpl.of(2, 20, sort));
        assertThat(page.getTotal()).isEqualTo(SIZE);
        assertThat(page.getItems()).containsExactlyElementsOf(expected.subList(20, 40));
    }

    @Test
    void shouldOrderByMultiValueKey() {
        for (var sort : List.of(Sort.by("spec.tags"), Sort.by(Sort.Direction.DESC, "spec.tags"))) {
            var expected = expectedOrder(allNames(), ordersOf(sort));
            assertThat(toList(engine.retrieveAll(Fake.class, options(), sort))).containsExactlyElementsOf(expected);
        }
    }

    @Test
    void shouldPageThroughOrderedCandidates() {
        var sort = Sort.by("metadata.creationTimestamp").and(Sort.by("metadata.name"));
        var expected = expectedOrder(allNames(), ordersOf(sort));

        var firstPage = engine.retrieve(Fake.class, options(), PageRequestImpl.of(1, 20, sort));
        assertThat(firstPage.getTotal()).isEqualTo(SIZE);
        assertThat(firstPage.getItems()).containsExactlyElementsOf(expected.subList(0, 20));

        var deepPage = engine.retrieve(Fake.class, options(), PageRequestImpl.of(100, 20, sort));
        assertThat(deepPage.getItems()).containsExactlyElementsOf(expected.subList(1_980, 2_000));

        var lastPage = engine.retrieve(Fake.class, options(), PageRequestImpl.of(150, 20, sort));
        assertThat(lastPage.getItems()).containsExactlyElementsOf(expected.subList(2_980, 3_000));

        var beyondLastPage = engine.retrieve(Fake.class, options(), PageRequestImpl.of(151, 20, sort));
        assertThat(beyondLastPage.getItems()).isEmpty();
    }

    @Test
    void shouldPageUnsortedByPrimaryKey() {
        var expected = allNames();
        expected.sort(Comparator.naturalOrder());
        var page = engine.retrieve(Fake.class, options(), PageRequestImpl.of(2, 25, Sort.unsorted()));
        assertThat(page.getItems()).containsExactlyElementsOf(expected.subList(25, 50));
    }

    @Test
    void shouldReturnTopNInOrder() {
        var sort = Sort.by(Sort.Direction.DESC, "metadata.creationTimestamp");
        var expected = expectedOrder(allNames(), ordersOf(sort));
        assertThat(engine.retrieveTopN(Fake.class, options(), sort, 30))
                .containsExactlyElementsOf(expected.subList(0, 30));
    }

    @Test
    void shouldTrimTheLastPartiallyFilledPage() {
        var sort = Sort.by("spec.owner");
        var expected = expectedOrder(allNames(), ordersOf(sort));
        var lastPage = engine.retrieve(Fake.class, options(), PageRequestImpl.of(82, 37, sort));
        assertThat(lastPage.getItems()).containsExactlyElementsOf(expected.subList(2_997, 3_000));
        var beyondLastPage = engine.retrieve(Fake.class, options(), PageRequestImpl.of(83, 37, sort));
        assertThat(beyondLastPage.getItems()).isEmpty();
    }

    private ListOptions options() {
        var options = new ListOptions();
        options.setFieldSelector(FieldSelector.of(Queries.isNull("metadata.deletionTimestamp")));
        return options;
    }

    private List<String> allNames() {
        return new ArrayList<>(fakes.keySet());
    }

    private static List<String> toList(Iterable<String> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false).toList();
    }

    private static List<Order> ordersOf(Sort sort) {
        var orders = new ArrayList<Order>();
        for (var order : sort) {
            orders.add(new Order(order.getProperty(), order.isDescending(), keyOf(order.getProperty())));
        }
        return orders;
    }

    private static Function<Fake, Object> keyOf(String property) {
        return switch (property) {
            case "metadata.name" -> fake -> fake.getMetadata().getName();
            case "metadata.creationTimestamp" -> fake -> fake.getMetadata().getCreationTimestamp();
            case "spec.owner" -> Fake::getStringValue;
            case "spec.tags" ->
                fake -> fake.getStringValues().isEmpty() ? null : Collections.min(fake.getStringValues());
            default -> throw new IllegalArgumentException("Unsupported property: " + property);
        };
    }

    private List<String> expectedOrder(Collection<String> candidates, List<Order> orders) {
        var sorted = new ArrayList<>(candidates);
        Comparator<String> comparator = (left, right) -> 0;
        for (var order : orders) {
            comparator = comparator.thenComparing((left, right) -> {
                var leftKey = order.key().apply(fakes.get(left));
                var rightKey = order.key().apply(fakes.get(right));
                return order.descending() ? compareKeys(rightKey, leftKey) : compareKeys(leftKey, rightKey);
            });
        }
        comparator = comparator.thenComparing(Comparator.naturalOrder());
        sorted.sort(comparator);
        return sorted;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static int compareKeys(Object left, Object right) {
        if (left == null) {
            return right == null ? 0 : -1;
        }
        if (right == null) {
            return 1;
        }
        return ((Comparable) left).compareTo(right);
    }

    private record Order(String property, boolean descending, Function<Fake, Object> key) {}
}
