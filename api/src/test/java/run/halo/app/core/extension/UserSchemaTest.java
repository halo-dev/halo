package run.halo.app.core.extension;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import run.halo.app.extension.Scheme;

class UserSchemaTest {

    @Test
    void shouldNotRequireEmailInUserSpec() {
        var scheme = Scheme.buildFromType(User.class);
        var userSpec = scheme.openApiSchema()
                .path("components")
                .path("schemas")
                .path("UserSpec");
        var required = new ArrayList<String>();
        userSpec.path("required").forEach(node -> required.add(node.asText()));
        assertThat(required).doesNotContain("email");
    }
}
