package run.halo.app.theme.router;

import org.junit.jupiter.api.Test;

/**
 * @author guqing
 * @since 2.0.0
 */
class RadixRouterTreeTest {

    @Test
    void insert() {
        RadixTree<String> radixTree = new RadixTree<>();
        radixTree.insert("/users", "users");
        radixTree.insert("/users/a", "users-a");
        radixTree.insert("/users/a/b/c", "users-a-b-c");
        radixTree.insert("/users/a/b/c/d", "users-a-b-c-d");
        radixTree.insert("/users/a/b/e/f", "users-a-b-c-d");
        radixTree.insert("/users/b/d", "users-b-d");
        radixTree.insert("/users/b/d/e/f", "users-b-d-e-f");
        radixTree.insert("/users/b/d/g/h", "users-b-d-g-h");
        radixTree.insert("/users/b/f/g/h", "users-b-f-g-h");
        radixTree.insert("/users/c/d/g/h", "users-c-d-g-h");
        radixTree.insert("/users/c/f/g/h", "users-c-f-g-h");
        radixTree.insert("/test/hello", "test-hello");
        radixTree.insert("/test/中文/abc", "test-中文-abc");
        radixTree.insert("/test/中/test", "test-中-test");

        radixTree.checkPriorities();
        radixTree.checkIndices();
        String display = radixTree.display();
        System.out.println(display);
    }

    @Test
    void delete() {
    }

    @Test
    void match() {
    }

    @Test
    void getKeys() {
    }
}