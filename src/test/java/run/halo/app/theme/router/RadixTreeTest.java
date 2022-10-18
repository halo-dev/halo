package run.halo.app.theme.router;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link RadixTree}.
 *
 * @author guqing
 * @since 2.0.0
 */
class RadixTreeTest {

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

        radixTree.checkIndices();
        String display = radixTree.display();
        assertThat(display).isEqualTo("""
            / [indices=ut]
            ├── users [value=users]*
            │   └── / [indices=abc]
            │       ├── a [value=users-a]*
            │       │   └── /b/ [indices=ce]
            │       │       ├── c [value=users-a-b-c]*
            │       │       │   └── /d [value=users-a-b-c-d]*
            │       │       └── e/f [value=users-a-b-c-d]*
            │       ├── b/ [indices=df]
            │       │   ├── d [value=users-b-d]*
            │       │   │   └── / [indices=eg]
            │       │   │       ├── e/f [value=users-b-d-e-f]*
            │       │   │       └── g/h [value=users-b-d-g-h]*
            │       │   └── f/g/h [value=users-b-f-g-h]*
            │       └── c/ [indices=df]
            │           ├── d/g/h [value=users-c-d-g-h]*
            │           └── f/g/h [value=users-c-f-g-h]*
            └── test/ [indices=h中]
                ├── hello [value=test-hello]*
                └── 中 [indices=文/]
                    ├── 文/abc [value=test-中文-abc]*
                    └── /test [value=test-中-test]*
               """);
    }

    @Test
    void delete() {
        RadixTree<String> radixTree = new RadixTree<>();
        radixTree.insert("/", "index");
        radixTree.insert("/categories/default", "categories-default");
        radixTree.insert("/tags/halo", "tags-halo");
        radixTree.insert("/archives/hello-halo", "archives-hello-halo");
        radixTree.insert("/about", "about");
        radixTree.delete("/tags/halo");
        radixTree.delete("/archives/hello-halo");
        radixTree.insert("/tags/halo", "tags-halo");
        radixTree.delete("/");

        String display = radixTree.display();
        assertThat(display).isEqualTo("""
            / [indices=cat]
            ├── categories/default [value=categories-default]*
            ├── about [value=about]*
            └── tags/halo [value=tags-halo]*
                """);

        radixTree.checkIndices();
    }

    @Test
    void getSize() {
        RadixTree<String> radixTree = new RadixTree<>();
        radixTree.insert("/", "index");
        radixTree.insert("/categories/default", "categories-default");
        radixTree.insert("/tags/halo", "tags-halo");
        radixTree.insert("/archives/hello-halo", "archives-hello-halo");

        assertThat(radixTree.getSize()).isEqualTo(4);

        radixTree.insert("/about", "about");
        radixTree.delete("/tags/halo");
        assertThat(radixTree.getSize()).isEqualTo(4);

        radixTree.delete("/archives/hello-halo");
        radixTree.insert("/tags/halo", "tags-halo");
        radixTree.delete("/");
        assertThat(radixTree.getSize()).isEqualTo(3);
    }

    @Test
    void contains() {
        RadixTree<String> radixTree = new RadixTree<>();
        radixTree.insert("/", "index");
        radixTree.insert("/categories/default", "categories-default");
        radixTree.insert("/tags/halo", "tags-halo");
        radixTree.insert("/archives/hello-halo", "archives-hello-halo");

        assertThat(radixTree.contains("/tags/halo")).isTrue();
        assertThat(radixTree.contains("/archives/hello-halo")).isTrue();
        assertThat(radixTree.contains("/categories/default")).isTrue();

        assertThat(radixTree.contains("/tags/test")).isFalse();
        assertThat(radixTree.contains("/tags/abc")).isFalse();
        assertThat(radixTree.contains("/archives/abc")).isFalse();
        assertThat(radixTree.contains("/archives")).isFalse();
    }

    @Test
    void replace() {
        RadixTree<String> radixTree = new RadixTree<>();
        radixTree.insert("/", "index");
        radixTree.insert("/categories/default", "categories-default");

        boolean replaced = radixTree.replace("/categories/default", "categories-new");
        assertThat(replaced).isTrue();
        assertThat(radixTree.find("/categories/default")).isEqualTo("categories-new");
    }

    @Test
    void find() {
        RadixTree<String> radixTree = new RadixTree<>();
        for (String testCase : testCases()) {
            radixTree.insert(testCase, testCase);
        }

        for (String testCase : testCases()) {
            String s = radixTree.find(testCase);
            assertThat(s).isEqualTo(testCase);
        }
    }

    @Test
    void visitTimes() {
        AtomicInteger visitCount = new AtomicInteger(0);
        RadixTree<String> radixTree = new RadixTree<>() {
            @Override
            protected <R> void visit(String prefix, Visitor<String, R> visitor,
                RadixTreeNode<String> parent, RadixTreeNode<String> node) {
                visitCount.getAndIncrement();
                super.visit(prefix, visitor, parent, node);
            }
        };

        for (String testCase : testCases()) {
            radixTree.insert(testCase, testCase);
        }

        /*
         *  / [indices=hbAscxy01adnΠuvw] 1
         *  ├── s [indices=er] 2
            │   ├── earch/query [value=/search/query]* 3
            │   └── rc/*filepath [value=/src/*filepath]* 3
         *  ├── u [indices=/s] 2
         *  │   └── sers/a/b/c [indices=/c] 3
         *  │       ├── /d [value=/users/a/b/c/d]* 4
         *  │       └── c/d [value=/users/a/b/cc/d]* 4
         *  //...
         */

        String key = "/users/a/b/c/d";
        AtomicInteger resultVisitorCount = new AtomicInteger(0);
        RadixTree.Visitor<String, String> visitor = new RadixTree.Visitor<>() {
            public void visit(String key, RadixTreeNode<String> parent,
                RadixTreeNode<String> node) {
                resultVisitorCount.getAndIncrement();
                if (node.isReal()) {
                    result = node.getValue();
                }
            }
        };

        RadixTreeNode<String> root = radixTree.getRoot();
        radixTree.visit(key, visitor, null, root);
        assertThat(resultVisitorCount.get()).isEqualTo(1);
        assertThat(visitor.result).isEqualTo(key);
        assertThat(visitCount.get()).isEqualTo(4);

        // clear counter
        visitCount.set(0);
        resultVisitorCount.set(0);
        visitor.result = null;
        key = "/search/query";
        radixTree.visit(key, visitor, null, root);
        assertThat(resultVisitorCount.get()).isEqualTo(1);
        assertThat(visitCount.get()).isEqualTo(3);
        assertThat(visitor.getResult()).isEqualTo(key);

        // clear counter
        visitCount.set(0);
        resultVisitorCount.set(0);
        visitor.result = null;
        // not exists key
        key = "/search";
        radixTree.visit(key, visitor, null, root);
        assertThat(resultVisitorCount.get()).isEqualTo(0);
        assertThat(visitCount.get()).isEqualTo(3);
        assertThat(visitor.getResult()).isEqualTo(null);

        // clear counter
        visitCount.set(0);
        resultVisitorCount.set(0);
        visitor.result = null;
        // not exists key
        key = "/s";
        radixTree.visit(key, visitor, null, root);
        assertThat(resultVisitorCount.get()).isEqualTo(1);
        assertThat(visitCount.get()).isEqualTo(2);
        assertThat(visitor.getResult()).isEqualTo(null);
    }

    private List<String> testCases() {
        return Arrays.asList(
            "/hi",
            "/b/",
            "/ABC/",
            "/search/query",
            "/cmd/tool/",
            "/src/*filepath",
            "/x",
            "/x/y",
            "/y/",
            "/y/z",
            "/0/id",
            "/0/id/1",
            "/1/id/",
            "/1/id/2",
            "/aa",
            "/a/",
            "/doc",
            "/doc/go_faq.html",
            "/doc/go1.html",
            "/doc/go/away",
            "/no/a",
            "/no/b",
            "/Π",
            "/u/apfêl/",
            "/u/äpfêl/",
            "/u/öpfêl",
            "/v/Äpfêl/",
            "/v/Öpfêl",
            "/w/♬",
            "/w/♭/",
            "/w/𠜎",
            "/w/𠜏/",
            "/users/a/b/c/d",
            "/users/a/b/cc/d"
        );
    }
}