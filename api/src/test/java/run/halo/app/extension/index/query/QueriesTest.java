package run.halo.app.extension.index.query;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class QueriesTest {

    @Test
    void shouldBuildAndConditionStaticMethod() {
        var condition = Queries.and(
            Queries.equal("status", "active"),
            Queries.greaterThan("age", 18)
        );
        assertEquals("(status = active AND age > 18)", condition.toString());
    }

    @Test
    void shouldBuildOrConditionStaticMethod() {
        var condition = Queries.or(
            Queries.lessThan("price", 50, false),
            Queries.equal("category", "Books")
        );
        assertEquals("(price < 50 OR category = Books)", condition.toString());
    }

    @Test
    void shouldBuildNotConditionStaticMethod() {
        var condition = Queries.not(Queries.equal("role", "Admin"));
        assertEquals("role != Admin", condition.toString());
    }

    @Test
    void shouldBuildBetweenCondition() {
        var condition = Queries.between("age", 18, true, 30, false);
        assertEquals("age BETWEEN [18, 30)", condition.toString());
    }

    @Test
    void shouldBuildEqualCondition() {
        var condition = Queries.equal("name", "John");
        assertEquals("name = John", condition.toString());
    }

    @Test
    void shouldBuildInCondition() {
        var condition = Queries.in("status", "active", "pending");
        assertEquals("status IN (active, pending)", condition.toString());

        condition = Queries.in("status", List.<String>of("active", "pending"));
        assertEquals("status IN (active, pending)", condition.toString());
    }

    @Test
    void shouldRefineInConditionWithSingleValueToEqualCondition() {
        var condition = Queries.in("status", "active");
        assertEquals("status = active", condition.toString());
    }

    @Test
    void shouldBuildGreaterThanCondition() {
        var condition = Queries.greaterThan("score", 85);
        assertEquals("score > 85", condition.toString());
    }

    @Test
    void shouldBuildLessThanCondition() {
        var condition = Queries.lessThan("price", 100, true);
        assertEquals("price <= 100", condition.toString());
    }

    @Test
    void shouldBuildLessThanConditionExclusive() {
        var condition = Queries.lessThan("price", 100);
        assertEquals("price < 100", condition.toString());
    }

    @Test
    void shouldBuildEmptyCondition() {
        var condition = Queries.empty();
        assertEquals("EMPTY", condition.toString());
    }

    @Test
    void shouldBuildAllCondition() {
        var condition = Queries.all("tags");
        assertEquals("ALL tags", condition.toString());
    }

    @Test
    void shouldBuildNotEqualCondition() {
        var condition = Queries.notEqual("name", "Alice");
        assertEquals("name != Alice", condition.toString());
    }

    @Test
    void shouldBuildNotBetweenCondition() {
        var condition = Queries.between("age", 20, false, 40, true).not();
        assertEquals("age NOT BETWEEN [20, 40)", condition.toString());
    }

    @Test
    void shouldBuildNotInCondition() {
        var condition = Queries.in("status", "inactive", "banned").not();
        assertEquals("status NOT IN (inactive, banned)", condition.toString());
    }

    @Test
    void shouldBuildNotAllCondition() {
        var condition = Queries.all("categories").not();
        assertEquals("NONE categories", condition.toString());
    }

    @Test
    void shouldBuildStartsWithCondition() {
        var condition = Queries.startsWith("username", "admin");
        assertEquals("username STARTS WITH admin", condition.toString());
    }

    @Test
    void shouldBuildNotStartsWithCondition() {
        var condition = Queries.startsWith("username", "guest").not();
        assertEquals("username NOT STARTS WITH guest", condition.toString());
    }

    @Test
    void shouldBuildEndsWithCondition() {
        var condition = Queries.endsWith("email", "@example.com");
        assertEquals("email ENDS WITH @example.com", condition.toString());
    }

    @Test
    void shouldBuildNotEndsWithCondition() {
        var condition = Queries.endsWith("email", "@spam.com").not();
        assertEquals("email NOT ENDS WITH @spam.com", condition.toString());
    }

    @Test
    void shouldBuildContainsCondition() {
        var condition = Queries.contains("description", "important");
        assertEquals("description CONTAINS important", condition.toString());
    }

    @Test
    void shouldBuildNotContainsCondition() {
        var condition = Queries.contains("notes", "confidential").not();
        assertEquals("notes NOT CONTAINS confidential", condition.toString());
    }

    @Test
    void shouldBuildAndCondition() {
        var condition = Queries.equal("status", "active")
            .and(Queries.greaterThan("age", 18));
        assertEquals("(status = active AND age > 18)", condition.toString());
    }

    @Test
    void shouldBuildOrCondition() {
        var condition = Queries.lessThan("price", 50, false)
            .or(Queries.equal("category", "Books"));
        assertEquals("(price < 50 OR category = Books)", condition.toString());
    }


    @Test
    void shouldBuildNotCondition() {
        var condition = Queries.equal("role", "Admin").not();
        assertEquals("role != Admin", condition.toString());
    }

    @Test
    void shouldBuildLabelExistsCondition() {
        var condition = Queries.labelExists("premiumUser");
        assertEquals("EXISTS metadata.labels['premiumUser']", condition.toString());
    }

    @Test
    void shouldBuildLabelNotExistsCondition() {
        var condition = Queries.labelExists("betaTester").not();
        assertEquals("NOT EXISTS metadata.labels['betaTester']", condition.toString());
    }

    @Test
    void shouldBuildLabelEqualCondition() {
        var condition = Queries.labelEqual("region", "CN");
        assertEquals("metadata.labels['region'] = 'CN'", condition.toString());
    }

    @Test
    void shouldBuildLabelNotEqualCondition() {
        var condition = Queries.labelEqual("tier", "gold").not();
        assertEquals("metadata.labels['tier'] <> 'gold'", condition.toString());
    }

    @Test
    void shouldBuildLabelInCondition() {
        var condition = Queries.labelIn("env", List.of("prod", "staging"));
        assertEquals("metadata.labels['env'] IN ('prod', 'staging')", condition.toString());
    }

    @Test
    void shouldBuildLabelNotInCondition() {
        var condition = Queries.labelIn("version", List.of("v1", "v2")).not();
        assertEquals("metadata.labels['version'] NOT IN ('v1', 'v2')", condition.toString());
    }

    @Test
    void shouldBuildChainedConditions() {
        var condition = Queries.equal("name", "Bob")
            .and(Queries.greaterThan("age", 25))
            .or(Queries.in("status", "active", "pending"));
        assertEquals(
            "((name = Bob AND age > 25) OR status IN (active, pending))", condition.toString()
        );
    }

    @Test
    void shouldBuildComplexCondition() {
        var condition = Queries.between("salary", 50000, true, 100000, false)
            .and(Queries.equal("department", "Engineering").not())
            .or(Queries.in("role", "Manager", "Director"));
        assertEquals("""
            ((salary BETWEEN [50000, 100000) AND department != Engineering) OR role IN (Manager,\
             Director))\
            """, condition.toString()
        );
    }
}