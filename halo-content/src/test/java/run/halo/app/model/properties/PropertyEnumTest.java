package run.halo.app.model.properties;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Map;

/**
 * @author johnniang
 * @date 19-4-25
 */
@Slf4j
public class PropertyEnumTest {

    @Test
    public void getValuePropertyMapTest() {
        Map<String, PropertyEnum> result = PropertyEnum.getValuePropertyEnumMap();

        log.debug(result.toString());
    }
}