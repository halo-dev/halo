package run.halo.app.extension.index;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

import java.io.IOException;
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
class SingleValueIndexTest {

    @Mock
    SingleValueIndexSpec<Fake, String> spec;

    @InjectMocks
    SingleValueIndex<Fake, String> index;


    @Nested
    class NonNullAndUniqueTest {

        @BeforeEach
        void setUp() {
            lenient().when(spec.getName()).thenReturn("metadata.name");
            lenient().when(spec.getKeyType()).thenReturn(String.class);
            lenient().when(spec.isNullable()).thenReturn(false);
            lenient().when(spec.isUnique()).thenReturn(true);
            lenient().when(spec.getValue(any(Fake.class))).thenAnswer(invocation -> {
                Fake fake = invocation.getArgument(0);
                return fake.getStringValue();
            });
        }

        @Test
        void shouldCloseIndexCorrectly() throws IOException {
            var fake = createFake("fake");
            fake.setStringValue("string");
            insert(fake);

            index.close();

            assertEquals(Set.of(), index.equal("string"));
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
                fake.setStringValue("string");

                var insert = index.prepareInsert(fake);
                insert.prepare();
                insert.commit();
                assertEquals(Set.of("fake"), index.equal("string"));
            }

            @Test
            void shouldRollbackInsertCorrectly() {
                var fake = createFake("fake");
                fake.setStringValue("string");

                var insert = index.prepareInsert(fake);
                insert.prepare();
                insert.commit();
                insert.rollback();

                assertEquals(Set.of(), index.equal("string"));
            }

            @Test
            void shouldUpdateCorrectly() {
                var fake = createFake("fake");
                fake.setStringValue("string");

                var insert = index.prepareInsert(fake);
                insert.prepare();
                insert.commit();

                fake.setStringValue("new-string");
                var update = index.prepareUpdate(fake);
                update.prepare();
                update.commit();

                assertEquals(Set.of("fake"), index.equal("new-string"));
            }


            @Test
            void shouldRollbackUpdateCorrectly() {
                var fake = createFake("fake");
                fake.setStringValue("string");

                var insert = index.prepareInsert(fake);
                insert.prepare();
                insert.commit();

                fake.setStringValue("new-string");
                var update = index.prepareUpdate(fake);
                update.prepare();
                update.commit();
                update.rollback();

                assertEquals(Set.of("fake"), index.equal("string"));
            }

            @Test
            void shouldDeleteCorrectly() {
                var fake = createFake("fake");
                fake.setStringValue("string");

                var insert = index.prepareInsert(fake);
                insert.prepare();
                insert.commit();

                var delete = index.prepareDelete("fake");
                delete.prepare();
                delete.commit();

                assertEquals(Set.of(), index.equal("string"));
            }

            @Test
            void shouldHandleDeleteIfPrimaryKeyNotExist() {
                var fake = createFake("fake");
                fake.setStringValue("string");
                var insert = index.prepareInsert(fake);
                insert.prepare();
                insert.commit();

                var delete = index.prepareDelete("non-existent-fake");
                delete.prepare();
                delete.commit();

                assertEquals(Set.of("fake"), index.equal("string"));
            }

            @Test
            void shouldRollbackDeleteCorrectly() {
                var fake = createFake("fake");
                fake.setStringValue("string");

                var insert = index.prepareInsert(fake);
                insert.prepare();
                insert.commit();

                var delete = index.prepareDelete("fake");
                delete.prepare();
                delete.commit();
                delete.rollback();

                assertEquals(Set.of("fake"), index.equal("string"));
            }

            @Test
            void shouldHandleDuplicateKeysOnInsert() {
                var fake1 = createFake("fake1");
                fake1.setStringValue("string");

                // first insert
                var insert = index.prepareInsert(fake1);
                insert.prepare();
                insert.commit();

                // second insert with the same string value
                var fake2 = createFake("fake2");
                fake2.setStringValue("string");
                insert = index.prepareInsert(fake2);
                assertThrows(DuplicateKeyException.class, insert::prepare);
            }

            @Test
            void shouldHandleDuplicateKeysOnUpdate() {
                var fake1 = createFake("fake1");
                fake1.setStringValue("string1");

                var insert1 = index.prepareInsert(fake1);
                insert1.prepare();
                insert1.commit();

                var fake2 = createFake("fake2");
                fake2.setStringValue("string2");

                var insert2 = index.prepareInsert(fake2);
                insert2.prepare();
                insert2.commit();

                // Attempt to update fake2's stringValue to "string1", which should cause a
                // duplicate
                // key
                fake2.setStringValue("string1");
                var update = index.prepareUpdate(fake2);
                assertThrows(DuplicateKeyException.class, update::prepare);
            }

            @Test
            void shouldHandleNullValueOnInsert() {
                var fake = createFake("fake");
                fake.setStringValue(null);

                var insert = index.prepareInsert(fake);
                assertThrows(IllegalArgumentException.class, insert::prepare);
            }

            @Test
            void shouldHandleNullValueOnUpdate() {
                var fake = createFake("fake");
                fake.setStringValue("string");

                var insert = index.prepareInsert(fake);
                insert.prepare();
                insert.commit();

                // Now update to null value
                fake.setStringValue(null);
                var update = index.prepareUpdate(fake);
                assertThrows(IllegalArgumentException.class, update::prepare);
            }
        }

        @Nested
        class IndexQueryTest {

            @BeforeEach
            void setUp() {
                // insert some data for query tests
                var fake1 = createFake("fake1");
                fake1.setStringValue("string1");

                var fake2 = createFake("fake2");
                fake2.setStringValue("string2");

                var fake3 = createFake("fake3");
                fake3.setStringValue("string3");

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
                assertThrows(IllegalArgumentException.class, () -> index.isNull());
            }

            @Test
            void isNotNullQuery() {
                assertThrows(IllegalArgumentException.class, () -> index.isNotNull());
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
    class NullableAndNonUniqueTest {

        @BeforeEach
        void setUp() {
            lenient().when(spec.getName()).thenReturn("metadata.name");
            lenient().when(spec.getKeyType()).thenReturn(String.class);
            lenient().when(spec.isNullable()).thenReturn(true);
            lenient().when(spec.isUnique()).thenReturn(false);
            lenient().when(spec.getValue(any(Fake.class))).thenAnswer(invocation -> {
                Fake fake = invocation.getArgument(0);
                return fake.getStringValue();
            });
        }

        @Test
        void shouldGetKeyType() {
            assertEquals(String.class, index.getKeyType());
        }

        @Test
        void shouldInsertAllowDuplicate() {
            var fake1 = createFake("fake1");
            fake1.setStringValue("string");

            var fake2 = createFake("fake2");
            fake2.setStringValue("string");

            var insert1 = index.prepareInsert(fake1);
            insert1.prepare();
            insert1.commit();

            var insert2 = index.prepareInsert(fake2);
            insert2.prepare();
            insert2.commit();

            assertEquals(Set.of("fake1", "fake2"), index.equal("string"));
        }

        @Test
        void shouldUpdateAllowDuplicate() {
            var fake1 = createFake("fake1");
            fake1.setStringValue("string1");

            var insert1 = index.prepareInsert(fake1);
            insert1.prepare();
            insert1.commit();

            var fake2 = createFake("fake2");
            fake2.setStringValue("string2");

            var insert2 = index.prepareInsert(fake2);
            insert2.prepare();
            insert2.commit();

            // update fake2 to string1 should be allowed when not unique
            fake2.setStringValue("string1");
            var update = index.prepareUpdate(fake2);
            update.prepare();
            update.commit();

            assertEquals(Set.of("fake1", "fake2"), index.equal("string1"));
        }

        @Test
        void shouldHandleNullValueOnInsert() {
            var fake = createFake("fake");
            fake.setStringValue(null);

            var insert = index.prepareInsert(fake);
            insert.prepare();
            insert.commit();

            assertEquals(Set.of("fake"), index.isNull());
            // no non-null entries exist
            assertTrue(index.isNotNull().isEmpty());
        }

        @Test
        void shouldHandleNullValueOnUpdate() {
            var fake = createFake("fake");
            fake.setStringValue("string");

            var insert = index.prepareInsert(fake);
            insert.prepare();
            insert.commit();

            // update to null should be allowed
            fake.setStringValue(null);
            var update = index.prepareUpdate(fake);
            update.prepare();
            update.commit();

            assertEquals(Set.of("fake"), index.isNull());
            assertTrue(index.equal("string").isEmpty());
        }

        @Test
        void shouldRollbackInsertCorrectly() {
            var fake = createFake("fake");
            fake.setStringValue("string");

            var insert = index.prepareInsert(fake);
            insert.prepare();
            insert.commit();
            insert.rollback();

            assertEquals(Set.of(), index.equal("string"));
        }

        @Test
        void shouldRollbackUpdateCorrectly() {
            var fake = createFake("fake");
            fake.setStringValue("string");

            var insert = index.prepareInsert(fake);
            insert.prepare();
            insert.commit();

            fake.setStringValue("new-string");
            var update = index.prepareUpdate(fake);
            update.prepare();
            update.commit();
            update.rollback();

            assertEquals(Set.of("fake"), index.equal("string"));
        }

        @Test
        void shouldDeleteCorrectly() {
            var fake = createFake("fake");
            fake.setStringValue("string");

            var insert = index.prepareInsert(fake);
            insert.prepare();
            insert.commit();

            var delete = index.prepareDelete("fake");
            delete.prepare();
            delete.commit();

            assertEquals(Set.of(), index.equal("string"));
        }

        @Test
        void shouldHandleDeleteIfPrimaryKeyNotExist() {
            var fake = createFake("fake");
            fake.setStringValue("string");
            var insert = index.prepareInsert(fake);
            insert.prepare();
            insert.commit();

            var delete = index.prepareDelete("non-existent-fake");
            delete.prepare();
            delete.commit();

            assertEquals(Set.of("fake"), index.equal("string"));
        }

        @Test
        void shouldRollbackDeleteCorrectly() {
            var fake = createFake("fake");
            fake.setStringValue("string");

            var insert = index.prepareInsert(fake);
            insert.prepare();
            insert.commit();

            var delete = index.prepareDelete("fake");
            delete.prepare();
            delete.commit();
            delete.rollback();

            assertEquals(Set.of("fake"), index.equal("string"));
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

