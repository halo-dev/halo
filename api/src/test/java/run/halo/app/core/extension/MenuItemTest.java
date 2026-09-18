package run.halo.app.core.extension;

import static org.assertj.core.api.Assertions.assertThat;

import io.swagger.v3.oas.annotations.media.Schema;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashSet;
import org.junit.jupiter.api.Test;
import run.halo.app.infra.utils.JsonUtils;

class MenuItemTest {

    @Test
    void shouldSerializeAndDeserializeRouteRefs() {
        assertThat(Arrays.stream(MenuItem.RouteRef.values())
                        .map(routeRef -> JsonUtils.objectToJson(routeRef))
                        .toList())
                .containsExactly("\"archives\"", "\"categories\"", "\"tags\"");

        var item = JsonUtils.jsonToObject(
                "{\"spec\":{\"displayName\":\"Archives\",\"routeRef\":\"archives\"}}", MenuItem.class);

        assertThat(item.getSpec().getRouteRef()).isEqualTo(MenuItem.RouteRef.ARCHIVES);
    }

    @Test
    void shouldDeserializeLegacyMenuItemWithoutRouteRef() {
        var item = JsonUtils.jsonToObject(
                "{\"spec\":{\"displayName\":\"Archives\",\"href\":\"/archives\"}}", MenuItem.class);

        assertThat(item.getSpec().getRouteRef()).isNull();
        assertThat(item.getSpec().getHref()).isEqualTo("/archives");
    }

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
