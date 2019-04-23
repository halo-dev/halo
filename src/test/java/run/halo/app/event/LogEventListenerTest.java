package run.halo.app.event;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.context.ApplicationListener;
import run.halo.app.event.logger.LogEvent;
import run.halo.app.event.logger.LogEventListener;
import run.halo.app.utils.ReflectionUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

/**
 * Log event listener test.
 *
 * @author johnniang
 * @date 19-4-21
 */
@Slf4j
public class LogEventListenerTest {

    @Test
    public void getGenericTypeTest() {
        ParameterizedType parameterizedType = ReflectionUtils.getParameterizedType(ApplicationListener.class, LogEventListener.class);
        Type[] actualTypeArguments = Objects.requireNonNull(parameterizedType).getActualTypeArguments();
        Type actualTypeArgument = actualTypeArguments[0];
        assertThat(actualTypeArgument.getTypeName(), equalTo(LogEvent.class.getTypeName()));
    }
}