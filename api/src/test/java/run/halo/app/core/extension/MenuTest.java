package run.halo.app.core.extension;

import static org.assertj.core.api.Assertions.assertThat;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.LinkedHashSet;
import org.junit.jupiter.api.Test;
import run.halo.app.infra.utils.JsonUtils;

class MenuTest {

    @Test
    void shouldSerializeLegacyMenuItems() {
        var menu = new Menu();
        var spec = new Menu.Spec();
        spec.setDisplayName("Primary");
        spec.setMenuItems(new LinkedHashSet<>(java.util.List.of("home", "about")));
        menu.setSpec(spec);

        assertThat(JsonUtils.objectToJson(menu)).contains("\"menuItems\":[\"home\",\"about\"]");
    }

    @Test
    void shouldMarkLegacyMenuItemsFieldDeprecated() throws NoSuchFieldException {
        var field = Menu.Spec.class.getDeclaredField("menuItems");

        assertThat(field.getAnnotation(Schema.class).deprecated()).isTrue();
    }
}
