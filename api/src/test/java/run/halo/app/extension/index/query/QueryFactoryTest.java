package run.halo.app.extension.index.query;

import static org.assertj.core.api.Assertions.assertThat;
import static run.halo.app.extension.index.query.QueryFactory.all;
import static run.halo.app.extension.index.query.QueryFactory.between;
import static run.halo.app.extension.index.query.QueryFactory.equal;
import static run.halo.app.extension.index.query.QueryFactory.equalOtherField;
import static run.halo.app.extension.index.query.QueryFactory.notEqual;
import static run.halo.app.extension.index.query.QueryFactory.notEqualOtherField;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link QueryFactory}.
 *
 * @author guqing
 * @since 2.12.0
 */
class QueryFactoryTest {

    @Test
    void allTest() {
        var indexView = IndexViewDataSet.createEmployeeIndexView();
        var resultSet = all("firstName").matches(indexView);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "100", "101", "102", "103", "104", "105"
        );
    }

    @Test
    void isNullTest() {
        var indexView = IndexViewDataSet.createPostIndexViewWithNullCell();
        var resultSet = QueryFactory.isNull("publishTime").matches(indexView);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "102", "103", "104", "108"
        );
    }

    @Test
    void isNotNullTest() {
        var indexView = IndexViewDataSet.createPostIndexViewWithNullCell();
        var resultSet = QueryFactory.isNotNull("publishTime").matches(indexView);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "100", "101", "105", "106", "107"
        );
    }

    @Test
    void equalTest() {
        var indexView = IndexViewDataSet.createEmployeeIndexView();
        var resultSet = equal("lastName", "Fay").matches(indexView);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "100", "104", "105"
        );
    }

    @Test
    void equalOtherFieldTest() {
        var indexView = IndexViewDataSet.createEmployeeIndexView();
        var resultSet = equalOtherField("managerId", "id").matches(indexView);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "102", "103"
        );
    }

    @Test
    void notEqualTest() {
        var indexView = IndexViewDataSet.createEmployeeIndexView();
        var resultSet = notEqual("lastName", "Fay").matches(indexView);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "101", "102", "103"
        );
    }

    @Test
    void notEqualOtherFieldTest() {
        var indexView = IndexViewDataSet.createEmployeeIndexView();
        var resultSet = notEqualOtherField("managerId", "id").matches(indexView);
        // 103 102 is equal
        assertThat(resultSet).containsExactlyInAnyOrder(
            "100", "101", "104", "105"
        );
    }

    @Test
    void lessThanTest() {
        var indexView = IndexViewDataSet.createEmployeeIndexView();
        var resultSet = QueryFactory.lessThan("id", "103").matches(indexView);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "100", "101", "102"
        );
    }

    @Test
    void lessThanOtherFieldTest() {
        var indexView = IndexViewDataSet.createEmployeeIndexView();
        var resultSet = QueryFactory.lessThanOtherField("id", "managerId").matches(indexView);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "100", "101"
        );
    }

    @Test
    void lessThanOrEqualTest() {
        var indexView = IndexViewDataSet.createEmployeeIndexView();
        var resultSet = QueryFactory.lessThanOrEqual("id", "103").matches(indexView);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "100", "101", "102", "103"
        );
    }

    @Test
    void lessThanOrEqualOtherFieldTest() {
        var indexView = IndexViewDataSet.createEmployeeIndexView();
        var resultSet =
            QueryFactory.lessThanOrEqualOtherField("id", "managerId").matches(indexView);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "100", "101", "102", "103"
        );
    }

    @Test
    void greaterThanTest() {
        var indexView = IndexViewDataSet.createEmployeeIndexView();
        var resultSet = QueryFactory.greaterThan("id", "103").matches(indexView);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "104", "105"
        );
    }

    @Test
    void greaterThanOtherFieldTest() {
        var indexView = IndexViewDataSet.createEmployeeIndexView();
        var resultSet = QueryFactory.greaterThanOtherField("id", "managerId").matches(indexView);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "104", "105"
        );
    }

    @Test
    void greaterThanOrEqualTest() {
        var indexView = IndexViewDataSet.createEmployeeIndexView();
        var resultSet = QueryFactory.greaterThanOrEqual("id", "103").matches(indexView);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "103", "104", "105"
        );
    }

    @Test
    void greaterThanOrEqualOtherFieldTest() {
        var indexView = IndexViewDataSet.createEmployeeIndexView();
        var resultSet =
            QueryFactory.greaterThanOrEqualOtherField("id", "managerId").matches(indexView);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "102", "103", "104", "105"
        );
    }

    @Test
    void inTest() {
        var indexView = IndexViewDataSet.createEmployeeIndexView();
        var resultSet = QueryFactory.in("id", "103", "104").matches(indexView);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "103", "104"
        );
    }

    @Test
    void inTest2() {
        var indexView = IndexViewDataSet.createEmployeeIndexView();
        var resultSet = QueryFactory.in("lastName", "Fay").matches(indexView);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "100", "104", "105"
        );
    }

    @Test
    void betweenTest() {
        var indexView = IndexViewDataSet.createEmployeeIndexView();
        var resultSet = between("id", "103", "105").matches(indexView);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "103", "104", "105"
        );

        indexView = IndexViewDataSet.createEmployeeIndexView();
        resultSet = between("salary", "2000", "2400").matches(indexView);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "101", "102", "103"
        );
    }

    @Test
    void betweenLowerExclusive() {
        var indexView = IndexViewDataSet.createEmployeeIndexView();
        var resultSet =
            QueryFactory.betweenLowerExclusive("salary", "2000", "2400").matches(indexView);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "101", "102"
        );
    }

    @Test
    void betweenUpperExclusive() {
        var indexView = IndexViewDataSet.createEmployeeIndexView();
        var resultSet =
            QueryFactory.betweenUpperExclusive("salary", "2000", "2400").matches(indexView);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "102", "103"
        );
    }

    @Test
    void betweenExclusive() {
        var indexView = IndexViewDataSet.createEmployeeIndexView();
        var resultSet = QueryFactory.betweenExclusive("salary", "2000", "2400").matches(indexView);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "102"
        );
    }

    @Test
    void startsWithTest() {
        var indexView = IndexViewDataSet.createEmployeeIndexView();
        var resultSet = QueryFactory.startsWith("firstName", "W").matches(indexView);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "102"
        );
    }

    @Test
    void endsWithTest() {
        var indexView = IndexViewDataSet.createEmployeeIndexView();
        var resultSet = QueryFactory.endsWith("firstName", "y").matches(indexView);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "103"
        );
    }

    @Test
    void containsTest() {
        var indexView = IndexViewDataSet.createEmployeeIndexView();
        var resultSet = QueryFactory.contains("firstName", "i").matches(indexView);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "102"
        );
        resultSet = QueryFactory.contains("firstName", "N").matches(indexView);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "104", "105"
        );
    }
}
