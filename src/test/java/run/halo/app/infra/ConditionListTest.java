package run.halo.app.infra;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Iterator;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import run.halo.app.infra.utils.JsonUtils;

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
    void addAndEvictFIFO() throws JSONException {
        ConditionList conditionList = new ConditionList();
        conditionList.addFirst(condition("type", "message", "reason", ConditionStatus.FALSE));
        conditionList.addFirst(condition("type2", "message2", "reason2", ConditionStatus.FALSE));
        conditionList.addFirst(condition("type3", "message3", "reason3", ConditionStatus.FALSE));

        JSONAssert.assertEquals("""
                [
                    {
                        "type": "type3",
                        "status": "FALSE",
                        "message": "message3",
                        "reason": "reason3"
                    },
                    {
                        "type": "type2",
                        "status": "FALSE",
                        "message": "message2",
                        "reason": "reason2"
                    },
                    {
                        "type": "type",
                        "status": "FALSE",
                        "message": "message",
                        "reason": "reason"
                    }
                ]
                """,
            JsonUtils.objectToJson(conditionList),
            true);
        assertThat(conditionList.size()).isEqualTo(3);

        conditionList.addAndEvictFIFO(
            condition("type4", "message4", "reason4", ConditionStatus.FALSE), 1);

        assertThat(conditionList.size()).isEqualTo(1);

        // json serialize test.
        JSONAssert.assertEquals("""
                [
                    {
                        "type": "type4",
                        "status": "FALSE",
                        "message": "message4",
                        "reason": "reason4"
                    }
                ]
                """,
            JsonUtils.objectToJson(conditionList), true);
    }

    @Test
    void peek() {
        ConditionList conditionList = new ConditionList();
        conditionList.addFirst(condition("type", "message", "reason", ConditionStatus.FALSE));
        Condition condition = condition("type2", "message2", "reason2", ConditionStatus.FALSE);
        conditionList.addFirst(condition);

        Condition peek = conditionList.peek();
        assertThat(peek).isEqualTo(condition);
    }

    @Test
    void removeLast() {
        ConditionList conditionList = new ConditionList();
        Condition condition = condition("type", "message", "reason", ConditionStatus.FALSE);
        conditionList.addFirst(condition);

        conditionList.addFirst(condition("type2", "message2", "reason2", ConditionStatus.FALSE));

        assertThat(conditionList.size()).isEqualTo(2);
        assertThat(conditionList.removeLast()).isEqualTo(condition);
        assertThat(conditionList.size()).isEqualTo(1);
    }

    @Test
    void test() {
        ConditionList conditionList = new ConditionList();
        conditionList.addAndEvictFIFO(
            condition("type", "message", "reason", ConditionStatus.FALSE));
        conditionList.addAndEvictFIFO(
            condition("type2", "message2", "reason2", ConditionStatus.FALSE));

        Iterator<Condition> iterator = conditionList.iterator();
        assertThat(iterator.next().getType()).isEqualTo("type2");
        assertThat(iterator.next().getType()).isEqualTo("type");
    }

    @Test
    void deserialization() {
        String s = """
            [{
                "type": "type3",
                "status": "FALSE",
                "message": "message3",
                "reason": "reason3"
            },
            {
                "type": "type2",
                "status": "FALSE",
                "message": "message2",
                "reason": "reason2"
            },
            {
                "type": "type",
                "status": "FALSE",
                "message": "message",
                "reason": "reason"
            }]
            """;
        ConditionList conditions = JsonUtils.jsonToObject(s, ConditionList.class);
        assertThat(conditions.peek().getType()).isEqualTo("type3");
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