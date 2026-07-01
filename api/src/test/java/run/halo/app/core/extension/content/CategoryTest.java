package run.halo.app.core.extension.content;

import static org.assertj.core.api.Assertions.assertThat;

import io.swagger.v3.oas.annotations.media.Schema;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.Test;
import run.halo.app.infra.utils.JsonUtils;

class CategoryTest {

    @Test
    void shouldSerializeParentAndLegacyChildren() {
        var category = new Category();
        var spec = new Category.CategorySpec();
        spec.setDisplayName("Java");
        spec.setSlug("java");
        spec.setPriority(0);
        spec.setParent("programming");
        spec.setChildren(List.of("spring", "jvm"));
        category.setSpec(spec);

        assertThat(JsonUtils.objectToJson(category))
                .contains("\"parent\":\"programming\"")
                .contains("\"children\":[\"spring\",\"jvm\"]");
    }

    @Test
    void shouldKeepLegacyChildrenFieldDeprecated() throws NoSuchFieldException {
        Field field = Category.CategorySpec.class.getDeclaredField("children");

        assertThat(field.getAnnotation(Deprecated.class)).isNotNull();
        assertThat(field.getAnnotation(Schema.class).deprecated()).isTrue();
    }
}
