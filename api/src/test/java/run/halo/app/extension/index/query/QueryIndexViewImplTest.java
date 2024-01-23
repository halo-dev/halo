package run.halo.app.extension.index.query;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link QueryIndexViewImpl}.
 *
 * @author guqing
 * @since 2.12.0
 */
class QueryIndexViewImplTest {

    @Test
    void findIdsForFieldValueEqualTest() {
        var indexView = EmployeeDataSet.createEmployeeIndexView();
        var resultSet = indexView.findIdsForFieldValueEqual("managerId", "id");
        assertThat(resultSet).containsExactlyInAnyOrder(
            "102", "103"
        );
    }

    @Test
    void findIdsForFieldValueGreaterThanTest() {
        var indexView = EmployeeDataSet.createEmployeeIndexView();
        var resultSet = indexView.findIdsForFieldValueGreaterThan("id", "managerId", false);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "104", "105"
        );

        indexView = EmployeeDataSet.createEmployeeIndexView();
        resultSet = indexView.findIdsForFieldValueGreaterThan("id", "managerId", true);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "103", "102", "104", "105"
        );
    }

    @Test
    void findIdsForFieldValueGreaterThanTest2() {
        var indexView = EmployeeDataSet.createEmployeeIndexView();
        var resultSet = indexView.findIdsForFieldValueGreaterThan("managerId", "id", false);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "100", "101"
        );

        indexView = EmployeeDataSet.createEmployeeIndexView();
        resultSet = indexView.findIdsForFieldValueGreaterThan("managerId", "id", true);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "100", "101", "102", "103"
        );
    }

    @Test
    void findIdsForFieldValueLessThanTest() {
        var indexView = EmployeeDataSet.createEmployeeIndexView();
        var resultSet = indexView.findIdsForFieldValueLessThan("id", "managerId", false);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "100", "101"
        );

        indexView = EmployeeDataSet.createEmployeeIndexView();
        resultSet = indexView.findIdsForFieldValueLessThan("id", "managerId", true);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "100", "101", "102", "103"
        );
    }

    @Test
    void findIdsForFieldValueLessThanTest2() {
        var indexView = EmployeeDataSet.createEmployeeIndexView();
        var resultSet = indexView.findIdsForFieldValueLessThan("managerId", "id", false);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "104", "105"
        );

        indexView = EmployeeDataSet.createEmployeeIndexView();
        resultSet = indexView.findIdsForFieldValueLessThan("managerId", "id", true);
        assertThat(resultSet).containsExactlyInAnyOrder(
            "103", "102", "104", "105"
        );
    }
}
