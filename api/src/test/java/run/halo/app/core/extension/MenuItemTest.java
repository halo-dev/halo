package run.halo.app.core.extension;

import static org.assertj.core.api.Assertions.assertThat;

import io.swagger.v3.oas.annotations.media.Schema;
import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import org.junit.jupiter.api.Test;
import run.halo.app.infra.utils.JsonUtils;

class MenuItemTest {

    @Test
    void shouldSerializeNewHierarchyFieldsAndLegacyChildren() {
        var item = new MenuItem();
        var spec = new MenuItem.MenuItemSpec();
        spec.setDisplayName("About");
        spec.setMenuName("primary");
        spec.setParent("root");
        spec.setChildren(new LinkedHashSet<>(java.util.List.of("child-a", "child-b")));
        item.setSpec(spec);

        assertThat(JsonUtils.objectToJson(item))
                .contains("\"menuName\":\"primary\"")
                .contains("\"parent\":\"root\"")
                .contains("\"children\":[\"child-a\",\"child-b\"]");
    }

    @Test
    void shouldKeepLegacyChildrenFieldDeprecated() throws NoSuchFieldException {
        Field field = MenuItem.MenuItemSpec.class.getDeclaredField("children");

        assertThat(field.getAnnotation(Deprecated.class)).isNotNull();
        assertThat(field.getAnnotation(Schema.class).deprecated()).isTrue();
    }
}
