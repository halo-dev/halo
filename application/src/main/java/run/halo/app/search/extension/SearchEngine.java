package run.halo.app.search.extension;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.extension.Ref;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@GVK(group = "plugin.halo.run", version = "v1alpha1", kind = "SearchEngine",
    plural = "searchengines", singular = "searchengine")
public class SearchEngine extends AbstractExtension {

    @Schema(requiredMode = REQUIRED)
    private SearchEngineSpec spec;

    @Data
    public static class SearchEngineSpec {

        private String logo;

        private String website;

        @Schema(requiredMode = REQUIRED)
        private String displayName;

        private String description;

        private Ref settingRef;

        private String postSearchImpl;

    }

}
