package run.halo.app.core.extension.notification;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Subscription}.
 *
 * @author guqing
 * @since 2.13.0
 */
class SubscriptionTest {

    @Test
    void reasonSubjectToStringTest() {
        Subscription.ReasonSubject subject = new Subscription.ReasonSubject();
        subject.setApiVersion("v1");
        subject.setKind("Kind");
        subject.setName("Name");

        String expected = "Kind#v1/Name";
        String actual = subject.toString();

        assertThat(actual).isEqualTo(expected);
    }
}