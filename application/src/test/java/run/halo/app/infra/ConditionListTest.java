package run.halo.app.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.time.Instant;
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

    @Test
    void shouldNotAddIfTypeIsSame() {
        var conditions = new ConditionList();
        var condition = Condition.builder()
            .type("type")
            .status(ConditionStatus.TRUE)
            .reason("reason")
            .message("message")
            .build();

        var anotherCondition = Condition.builder()
            .type("type")
            .status(ConditionStatus.FALSE)
            .reason("another reason")
            .message("another message")
            .build();

        conditions.addAndEvictFIFO(condition);
        conditions.addAndEvictFIFO(anotherCondition);

        assertEquals(1, conditions.size());
    }

    @Test
    void shouldNotUpdateLastTransitionTimeIfStatusNotChanged() {
        var now = Instant.now();
        var conditions = new ConditionList();
        conditions.addAndEvictFIFO(
            Condition.builder()
                .type("type")
                .status(ConditionStatus.TRUE)
                .reason("reason")
                .message("message")
                .lastTransitionTime(now)
                .build()
        );

        conditions.addAndEvictFIFO(
            Condition.builder()
                .type("type")
                .status(ConditionStatus.TRUE)
                .reason("reason")
                .message("message")
                .lastTransitionTime(now.plus(Duration.ofSeconds(1)))
                .build()
        );

        assertEquals(1, conditions.size());
        // make sure the last transition time was not modified.
        assertEquals(now, conditions.peek().getLastTransitionTime());
    }

    @Test
    void shouldUpdateLastTransitionTimeIfStatusChanged() {
        var now = Instant.now();
        var conditions = new ConditionList();
        conditions.addAndEvictFIFO(
            Condition.builder()
                .type("type")
                .status(ConditionStatus.TRUE)
                .reason("reason")
                .message("message")
                .lastTransitionTime(now)
                .build()
        );

        conditions.addAndEvictFIFO(
            Condition.builder()
                .type("type")
                .status(ConditionStatus.FALSE)
                .reason("reason")
                .message("message")
                .lastTransitionTime(now.plus(Duration.ofSeconds(1)))
                .build()
        );

        assertEquals(1, conditions.size());
        assertEquals(now.plus(Duration.ofSeconds(1)), conditions.peek().getLastTransitionTime());
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