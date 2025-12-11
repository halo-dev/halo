package run.halo.app.extension.index;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import run.halo.app.extension.Metadata;

@ExtendWith(MockitoExtension.class)
class MultiValueIndexTest {

    @Mock
    MultiValueIndexSpec<Fake, String> spec;

    @InjectMocks
    MultiValueIndex<Fake, String> index;

    @Nested
    class UniqueMultiValueTests {

        @BeforeEach
        void setUp() {
            lenient().when(spec.getName()).thenReturn("metadata.name");
            lenient().when(spec.getKeyType()).thenReturn(String.class);
            lenient().when(spec.isUnique()).thenReturn(true);
            lenient().when(spec.getValues(any(Fake.class))).thenAnswer(invocation -> {
                Fake fake = invocation.getArgument(0);
                return fake.getStringValues();
            });
        }

        @Test
        void shouldCloseIndexCorrectly() throws IOException {
            var fake = createFake("fake");
            fake.setStringValues(Set.of("a", "b"));
            insert(fake);

            index.close();

            assertTrue(index.all().isEmpty());
        }

        @Nested
        class IndexOperationTest {

            @Test
            void shouldGetKeyType() {
                assertEquals(String.class, index.getKeyType());
            }

            @Test
            void shouldInsertCorrectly() {
                var fake = createFake("fake");
                fake.setStringValues(Set.of("s1", "s2"));

                var insert = index.prepareInsert(fake);
                insert.prepare();
                insert.commit();

                assertEquals(Set.of("fake"), index.equal("s1"));
                assertEquals(Set.of("fake"), index.equal("s2"));
                assertEquals(Set.of("s1", "s2"), index.getKeys("fake"));
                assertTrue(index.isNull().isEmpty());
            }

            @Test
            void shouldRollbackInsertCorrectly() {
                var fake = createFake("fake");
                fake.setStringValues(Set.of("s1", "s2"));

                var insert = index.prepareInsert(fake);
                insert.prepare();
                insert.commit();
                insert.rollback();

                assertTrue(index.all().isEmpty());
            }

            @Test
            void shouldUpdateCorrectly() {
                var fake = createFake("fake");
                fake.setStringValues(Set.of("s1"));
                var insert = index.prepareInsert(fake);
                insert.prepare();
                insert.commit();

                fake.setStringValues(Set.of("s2", "s3"));
                var update = index.prepareUpdate(fake);
                update.prepare();
                update.commit();

                assertTrue(index.equal("s1").isEmpty());
                assertEquals(Set.of("fake"), index.equal("s2"));
                assertEquals(Set.of("fake"), index.equal("s3"));
                assertEquals(Set.of("s2", "s3"), index.getKeys("fake"));
            }

            @Test
            void shouldRollbackUpdateCorrectly() {
                var fake = createFake("fake");
                fake.setStringValues(Set.of("s1"));
                var insert = index.prepareInsert(fake);
                insert.prepare();
                insert.commit();

                fake.setStringValues(Set.of("s2"));
                var update = index.prepareUpdate(fake);
                update.prepare();
                update.commit();
                update.rollback();

                assertEquals(Set.of("fake"), index.equal("s1"));
                assertTrue(index.equal("s2").isEmpty());
                assertEquals(Set.of("s1"), index.getKeys("fake"));
            }

            @Test
            void shouldDeleteCorrectly() {
                var fake = createFake("fake");
                fake.setStringValues(Set.of("s1", "s2"));
                insert(fake);

                var delete = index.prepareDelete("fake");
                delete.prepare();
                delete.commit();

                assertTrue(index.all().isEmpty());
            }

            @Test
            void shouldHandleDeleteIfPrimaryKeyNotExist() {
                var fake = createFake("fake");
                fake.setStringValues(Set.of("s1"));
                insert(fake);

                var delete = index.prepareDelete("non-existent-fake");
                delete.prepare();
                delete.commit();

                assertEquals(Set.of("fake"), index.equal("s1"));
            }

            @Test
            void shouldRollbackDeleteCorrectly() {
                var fake = createFake("fake");
                fake.setStringValues(Set.of("s1", "s2"));
                insert(fake);

                var delete = index.prepareDelete("fake");
                delete.prepare();
                delete.commit();
                delete.rollback();

                // rollback restores previous keys
                assertEquals(Set.of("fake"), index.equal("s1"));
                assertEquals(Set.of("fake"), index.equal("s2"));
                assertEquals(Set.of("s1", "s2"), index.getKeys("fake"));
            }

            @Test
            void shouldHandleDuplicateKeysOnInsert() {
                var fake1 = createFake("fake1");
                fake1.setStringValues(Set.of("common"));

                // first insert
                var insert1 = index.prepareInsert(fake1);
                insert1.prepare();
                insert1.commit();

                // second insert with the same key -> duplicate occurs on commit
                var fake2 = createFake("fake2");
                fake2.setStringValues(Set.of("common"));
                var insert2 = index.prepareInsert(fake2);
                insert2.prepare();
                assertThrows(DuplicateKeyException.class, insert2::commit);
            }

            @Test
            void shouldHandleDuplicateKeysOnUpdate() {
                var fake1 = createFake("fake1");
                fake1.setStringValues(Set.of("s1"));
                var insert1 = index.prepareInsert(fake1);
                insert1.prepare();
                insert1.commit();

                var fake2 = createFake("fake2");
                fake2.setStringValues(Set.of("s2"));
                var insert2 = index.prepareInsert(fake2);
                insert2.prepare();
                insert2.commit();

                // Attempt to update fake2's values to contain "s1", which should cause a duplicate
                fake2.setStringValues(Set.of("s1"));
                var update = index.prepareUpdate(fake2);
                update.prepare();
                assertThrows(DuplicateKeyException.class, update::commit);
            }

            @Test
            void shouldHandleEmptyValuesAsNullOnInsert() {
                var fake = createFake("fake");
                fake.setStringValues(Collections.emptySet());

                var insert = index.prepareInsert(fake);
                insert.prepare();
                insert.commit();

                assertEquals(Set.of("fake"), index.isNull());
                assertTrue(index.isNotNull().isEmpty());
                assertEquals(Collections.emptySet(), index.getKeys("fake"));
            }

            @Test
            void shouldHandleEmptyValuesAsNullOnUpdate() {
                var fake = createFake("fake");
                fake.setStringValues(Set.of("s1"));
                insert(fake);

                // update to empty values
                fake.setStringValues(Collections.emptySet());
                var update = index.prepareUpdate(fake);
                update.prepare();
                update.commit();

                assertEquals(Set.of("fake"), index.isNull());
                assertTrue(index.equal("s1").isEmpty());
                assertEquals(Collections.emptySet(), index.getKeys("fake"));
            }
        }

        @Nested
        class IndexQueryTest {

            @BeforeEach
            void setUp() {
                // insert some data for query tests (single-value sets)
                var fake1 = createFake("fake1");
                fake1.setStringValues(Set.of("string1"));

                var fake2 = createFake("fake2");
                fake2.setStringValues(Set.of("string2"));

                var fake3 = createFake("fake3");
                fake3.setStringValues(Set.of("string3"));

                insert(fake1);
                insert(fake2);
                insert(fake3);
            }

            @Test
            void equalQuery() {
                var result1 = index.equal("string1");
                assertEquals(Set.of("fake1"), result1);

                var result2 = index.equal("string2");
                assertEquals(Set.of("fake2"), result2);

                var result3 = index.equal("non-existent-string");
                assertTrue(result3.isEmpty());
            }

            @Test
            void notEqualQuery() {
                var result1 = index.notEqual("string1");
                assertEquals(Set.of("fake2", "fake3"), result1);

                var result2 = index.notEqual("string2");
                assertEquals(Set.of("fake1", "fake3"), result2);

                var result3 = index.notEqual("non-existent-string");
                assertEquals(Set.of("fake1", "fake2", "fake3"), result3);
            }

            @Test
            void allQuery() {
                var result = index.all();
                assertEquals(Set.of("fake1", "fake2", "fake3"), result);
            }

            @Test
            void betweenQuery() {
                var result = index.between("string1", true, "string3", false);
                assertEquals(Set.of("fake1", "fake2"), result);
            }

            @Test
            void notBetweenQuery() {
                var result = index.notBetween("string1", true, "string2", false);
                assertEquals(Set.of("fake2", "fake3"), result);
            }

            @Test
            void inQuery() {
                var result = index.in(Set.of("string1", "string3"));
                assertEquals(Set.of("fake1", "fake3"), result);
            }

            @Test
            void notInQuery() {
                var result = index.notIn(Set.of("string1", "string3"));
                assertEquals(Set.of("fake2"), result);
            }

            @Test
            void lessThanQuery() {
                var result = index.lessThan("string3", false);
                assertEquals(Set.of("fake1", "fake2"), result);
            }

            @Test
            void greaterThanQuery() {
                var result = index.greaterThan("string1", false);
                assertEquals(Set.of("fake2", "fake3"), result);
            }

            @Test
            void isNullQuery() {
                // none of the 3 inserted are null
                assertTrue(index.isNull().isEmpty());
            }

            @Test
            void isNotNullQuery() {
                assertEquals(Set.of("fake1", "fake2", "fake3"), index.isNotNull());
            }

            @Test
            void stringContainsQuery() {
                assertEquals(Set.of("fake1", "fake2", "fake3"), index.stringContains("ing"));
                assertEquals(Set.of("fake2"), index.stringContains("ing2"));
            }

            @Test
            void stringNotContainsQuery() {
                assertEquals(Set.of(), index.stringNotContains("ing"));
                assertEquals(Set.of("fake1", "fake3"), index.stringNotContains("ing2"));
            }

            @Test
            void stringStartsWithQuery() {
                assertEquals(Set.of("fake1", "fake2", "fake3"), index.stringStartsWith("string"));
                assertEquals(Set.of("fake2"), index.stringStartsWith("string2"));
            }

            @Test
            void stringNotStartsWithQuery() {
                assertEquals(Set.of(), index.stringNotStartsWith("string"));
                assertEquals(Set.of("fake1", "fake3"), index.stringNotStartsWith("string2"));
            }

            @Test
            void stringEndsWithQuery() {
                assertEquals(Set.of("fake1"), index.stringEndsWith("ing1"));
                assertEquals(Set.of("fake3"), index.stringEndsWith("ing3"));
            }

            @Test
            void stringNotEndsWithQuery() {
                assertEquals(Set.of("fake2", "fake3"), index.stringNotEndsWith("ing1"));
                assertEquals(Set.of("fake1", "fake2"), index.stringNotEndsWith("ing3"));
            }

        }
    }

    @Nested
    class NonUniqueMultiValueTests {

        @BeforeEach
        void setUp() {
            lenient().when(spec.getName()).thenReturn("metadata.name");
            lenient().when(spec.getKeyType()).thenReturn(String.class);
            lenient().when(spec.isUnique()).thenReturn(false);
            lenient().when(spec.getValues(any(Fake.class))).thenAnswer(invocation -> {
                Fake fake = invocation.getArgument(0);
                return fake.getStringValues();
            });
        }

        @Test
        void shouldInsertCorrectly() {
            var fake = createFake("fake");
            fake.setStringValues(Set.of("s1", "s2"));

            var insert = index.prepareInsert(fake);
            insert.prepare();
            insert.commit();

            assertEquals(Set.of("fake"), index.equal("s1"));
            assertEquals(Set.of("fake"), index.equal("s2"));
            assertEquals(Set.of("s1", "s2"), index.getKeys("fake"));
            assertTrue(index.isNull().isEmpty());
        }

        @Test
        void shouldRollbackInsertCorrectly() {
            var fake = createFake("fake");
            fake.setStringValues(Set.of("s1", "s2"));

            var insert = index.prepareInsert(fake);
            insert.prepare();
            insert.commit();
            insert.rollback();

            assertTrue(index.all().isEmpty());
        }

        @Test
        void shouldUpdateCorrectly() {
            var fake = createFake("fake");
            fake.setStringValues(Set.of("s1"));
            var insert = index.prepareInsert(fake);
            insert.prepare();
            insert.commit();

            fake.setStringValues(Set.of("s2", "s3"));
            var update = index.prepareUpdate(fake);
            update.prepare();
            update.commit();

            assertTrue(index.equal("s1").isEmpty());
            assertEquals(Set.of("fake"), index.equal("s2"));
            assertEquals(Set.of("fake"), index.equal("s3"));
            assertEquals(Set.of("s2", "s3"), index.getKeys("fake"));
        }

        @Test
        void shouldRollbackUpdateCorrectly() {
            var fake = createFake("fake");
            fake.setStringValues(Set.of("s1"));
            var insert = index.prepareInsert(fake);
            insert.prepare();
            insert.commit();

            fake.setStringValues(Set.of("s2"));
            var update = index.prepareUpdate(fake);
            update.prepare();
            update.commit();
            update.rollback();

            assertEquals(Set.of("fake"), index.equal("s1"));
            assertTrue(index.equal("s2").isEmpty());
            assertEquals(Set.of("s1"), index.getKeys("fake"));
        }

        @Test
        void shouldDeleteCorrectly() {
            var fake = createFake("fake");
            fake.setStringValues(Set.of("s1", "s2"));
            insert(fake);

            var delete = index.prepareDelete("fake");
            delete.prepare();
            delete.commit();

            assertTrue(index.all().isEmpty());
        }

        @Test
        void shouldHandleDeleteIfPrimaryKeyNotExist() {
            var fake = createFake("fake");
            fake.setStringValues(Set.of("s1"));
            insert(fake);

            var delete = index.prepareDelete("non-existent-fake");
            delete.prepare();
            delete.commit();

            assertEquals(Set.of("fake"), index.equal("s1"));
        }

        @Test
        void shouldRollbackDeleteCorrectly() {
            var fake = createFake("fake");
            fake.setStringValues(Set.of("s1", "s2"));
            insert(fake);

            var delete = index.prepareDelete("fake");
            delete.prepare();
            delete.commit();
            delete.rollback();

            // rollback restores previous keys
            assertEquals(Set.of("fake"), index.equal("s1"));
            assertEquals(Set.of("fake"), index.equal("s2"));
            assertEquals(Set.of("s1", "s2"), index.getKeys("fake"));
        }

        @Test
        void shouldAllowDuplicateKeysOnInsert() {
            var fake1 = createFake("fake1");
            fake1.setStringValues(Set.of("common"));

            // first insert
            var insert1 = index.prepareInsert(fake1);
            insert1.prepare();
            insert1.commit();

            // second insert with the same key -> should succeed for non-unique index
            var fake2 = createFake("fake2");
            fake2.setStringValues(Set.of("common"));
            var insert2 = index.prepareInsert(fake2);
            insert2.prepare();
            insert2.commit();

            assertEquals(Set.of("fake1", "fake2"), index.equal("common"));
        }

        @Test
        void shouldAllowDuplicateKeysOnUpdate() {
            var fake1 = createFake("fake1");
            fake1.setStringValues(Set.of("s1"));
            var insert1 = index.prepareInsert(fake1);
            insert1.prepare();
            insert1.commit();

            var fake2 = createFake("fake2");
            fake2.setStringValues(Set.of("s2"));
            var insert2 = index.prepareInsert(fake2);
            insert2.prepare();
            insert2.commit();

            // Update fake2's values to contain "s1" — should be allowed for non-unique index
            fake2.setStringValues(Set.of("s1"));
            var update = index.prepareUpdate(fake2);
            update.prepare();
            update.commit();

            assertEquals(Set.of("fake1", "fake2"), index.equal("s1"));
        }

        @Test
        void shouldHandleEmptyValuesAsNullOnInsert() {
            var fake = createFake("fake");
            fake.setStringValues(Collections.emptySet());

            var insert = index.prepareInsert(fake);
            insert.prepare();
            insert.commit();

            assertEquals(Set.of("fake"), index.isNull());
            assertTrue(index.isNotNull().isEmpty());
            assertEquals(Collections.emptySet(), index.getKeys("fake"));
        }

        @Test
        void shouldHandleEmptyValuesAsNullOnUpdate() {
            var fake = createFake("fake");
            fake.setStringValues(Set.of("s1"));
            insert(fake);

            // update to empty values
            fake.setStringValues(Collections.emptySet());
            var update = index.prepareUpdate(fake);
            update.prepare();
            update.commit();

            assertEquals(Set.of("fake"), index.isNull());
            assertTrue(index.equal("s1").isEmpty());
            assertEquals(Collections.emptySet(), index.getKeys("fake"));
        }

    }

    void insert(Fake fake) {
        var insert = index.prepareInsert(fake);
        insert.prepare();
        insert.commit();
    }

    Fake createFake(String name) {
        var fake = new Fake();
        fake.setMetadata(new Metadata());
        fake.getMetadata().setName(name);
        return fake;
    }

}