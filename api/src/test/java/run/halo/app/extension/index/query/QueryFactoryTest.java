package run.halo.app.extension.index.query;

import static org.assertj.core.api.Assertions.assertThat;
import static run.halo.app.extension.index.query.QueryFactory.all;
import static run.halo.app.extension.index.query.QueryFactory.and;
import static run.halo.app.extension.index.query.QueryFactory.between;
import static run.halo.app.extension.index.query.QueryFactory.contains;
import static run.halo.app.extension.index.query.QueryFactory.endsWith;
import static run.halo.app.extension.index.query.QueryFactory.equal;
import static run.halo.app.extension.index.query.QueryFactory.equalOtherField;
import static run.halo.app.extension.index.query.QueryFactory.getFieldNamesUsedInQuery;
import static run.halo.app.extension.index.query.QueryFactory.greaterThan;
import static run.halo.app.extension.index.query.QueryFactory.greaterThanOrEqual;
import static run.halo.app.extension.index.query.QueryFactory.greaterThanOrEqualOtherField;
import static run.halo.app.extension.index.query.QueryFactory.greaterThanOtherField;
import static run.halo.app.extension.index.query.QueryFactory.in;
import static run.halo.app.extension.index.query.QueryFactory.isNotNull;
import static run.halo.app.extension.index.query.QueryFactory.isNull;
import static run.halo.app.extension.index.query.QueryFactory.lessThan;
import static run.halo.app.extension.index.query.QueryFactory.lessThanOrEqual;
import static run.halo.app.extension.index.query.QueryFactory.lessThanOrEqualOtherField;
import static run.halo.app.extension.index.query.QueryFactory.lessThanOtherField;
import static run.halo.app.extension.index.query.QueryFactory.notEqual;
import static run.halo.app.extension.index.query.QueryFactory.notEqualOtherField;
import static run.halo.app.extension.index.query.QueryFactory.or;
import static run.halo.app.extension.index.query.QueryFactory.startsWith;

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
        var resultSet = isNull("publishTime").matches(indexView);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "102", "103", "104", "108"
        );
    }

    @Test
    void isNotNullTest() {
        var indexView = IndexViewDataSet.createPostIndexViewWithNullCell();
        var resultSet = isNotNull("publishTime").matches(indexView);
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
        var resultSet = lessThan("id", "103").matches(indexView);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "100", "101", "102"
        );
    }

    @Test
    void lessThanOtherFieldTest() {
        var indexView = IndexViewDataSet.createEmployeeIndexView();
        var resultSet = lessThanOtherField("id", "managerId").matches(indexView);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "100", "101"
        );
    }

    @Test
    void lessThanOrEqualTest() {
        var indexView = IndexViewDataSet.createEmployeeIndexView();
        var resultSet = lessThanOrEqual("id", "103").matches(indexView);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "100", "101", "102", "103"
        );
    }

    @Test
    void lessThanOrEqualOtherFieldTest() {
        var indexView = IndexViewDataSet.createEmployeeIndexView();
        var resultSet =
            lessThanOrEqualOtherField("id", "managerId").matches(indexView);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "100", "101", "102", "103"
        );
    }

    @Test
    void greaterThanTest() {
        var indexView = IndexViewDataSet.createEmployeeIndexView();
        var resultSet = greaterThan("id", "103").matches(indexView);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "104", "105"
        );
    }

    @Test
    void greaterThanOtherFieldTest() {
        var indexView = IndexViewDataSet.createEmployeeIndexView();
        var resultSet = greaterThanOtherField("id", "managerId").matches(indexView);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "104", "105"
        );
    }

    @Test
    void greaterThanOrEqualTest() {
        var indexView = IndexViewDataSet.createEmployeeIndexView();
        var resultSet = greaterThanOrEqual("id", "103").matches(indexView);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "103", "104", "105"
        );
    }

    @Test
    void greaterThanOrEqualOtherFieldTest() {
        var indexView = IndexViewDataSet.createEmployeeIndexView();
        var resultSet =
            greaterThanOrEqualOtherField("id", "managerId").matches(indexView);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "102", "103", "104", "105"
        );
    }

    @Test
    void inTest() {
        var indexView = IndexViewDataSet.createEmployeeIndexView();
        var resultSet = in("id", "103", "104").matches(indexView);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "103", "104"
        );
    }

    @Test
    void inTest2() {
        var indexView = IndexViewDataSet.createEmployeeIndexView();
        var resultSet = in("lastName", "Fay").matches(indexView);
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
        var resultSet = startsWith("firstName", "W").matches(indexView);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "102"
        );
    }

    @Test
    void endsWithTest() {
        var indexView = IndexViewDataSet.createEmployeeIndexView();
        var resultSet = endsWith("firstName", "y").matches(indexView);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "103"
        );
    }

    @Test
    void containsTest() {
        var indexView = IndexViewDataSet.createEmployeeIndexView();
        var resultSet = contains("firstName", "i").matches(indexView);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "102"
        );
        resultSet = contains("firstName", "N").matches(indexView);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "104", "105"
        );
    }

    @Test
    void notTest() {
        var indexView = IndexViewDataSet.createEmployeeIndexView();
        var resultSet =
            QueryFactory.not(QueryFactory.contains("firstName", "i")).matches(indexView);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "100", "101", "103", "104", "105"
        );
    }

    @Test
    void getUsedFieldNamesTest() {
        // single query
        var query = equal("firstName", "W");
        var fieldNames = getFieldNamesUsedInQuery(query);
        assertThat(fieldNames).containsExactlyInAnyOrder("firstName");

        // and composite query
        query = and(
            and(equal("firstName", "W"), equal("lastName", "Fay")),
            or(equalOtherField("id", "userId"), lessThan("age", "123"))
        );
        fieldNames = getFieldNamesUsedInQuery(query);
        assertThat(fieldNames).containsExactlyInAnyOrder("firstName", "lastName", "id", "userId",
            "age");

        // or composite query
        var complexQuery = or(
            equal("field1", "value1"),
            and(
                equal("field2", "value2"),
                equal("field3", "value3")
            ),
            equal("field4", "value4")
        );
        fieldNames = getFieldNamesUsedInQuery(complexQuery);
        assertThat(fieldNames).containsExactlyInAnyOrder("field1", "field2", "field3", "field4");
    }
}
