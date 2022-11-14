package run.halo.app.infra;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ConditionList}.
 *
 * @author guqing
 * @since 2.0.0
 */
class ConditionListTest {

    @Test
    void add() {
        ConditionList conditionList = new ConditionList();
        conditionList.add(condition("type", "message", "reason", ConditionStatus.FALSE));
        conditionList.add(condition("type", "message", "reason", ConditionStatus.FALSE));

        assertThat(conditionList.size()).isEqualTo(1);
        conditionList.add(condition("type", "message", "reason", ConditionStatus.TRUE));
        assertThat(conditionList.size()).isEqualTo(2);
    }

    @Test
    void addAndEvictFIFO() {
    }

    @Test
    void testAddAndEvictFIFO() {
    }

    private Condition condition(String type, String message, String reason,
        ConditionStatus status) {
        Condition condition = new Condition();
        condition.setType(type);
        condition.setMessage(message);
        condition.setReason(reason);
        condition.setStatus(status);
        return condition;
    }
}