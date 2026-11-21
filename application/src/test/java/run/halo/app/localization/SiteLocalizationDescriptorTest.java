package run.halo.app.localization;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import run.halo.app.extension.ConfigMap;

class SiteLocalizationDescriptorTest {

    private final SiteLocalizationDescriptor descriptor = new SiteLocalizationDescriptor();

    @Test
    void resolvesSitePresentationFieldsFromSystemSettings() {
        var config = new ConfigMap();
        config.setData(Map.of(
                "basic",
                "{\"title\":\"Site\",\"subtitle\":\"Subtitle\"}",
                "seo",
                "{\"description\":\"Description\",\"keywords\":\"Keywords\"}"));

        assertThat(descriptor.resolveFields(config, Map.of()).block())
                .containsEntry(SiteLocalizationDescriptor.TITLE, "Site")
                .containsEntry(SiteLocalizationDescriptor.SUBTITLE, "Subtitle")
                .containsEntry(SiteLocalizationDescriptor.SEO_DESCRIPTION, "Description")
                .containsEntry(SiteLocalizationDescriptor.SEO_KEYWORDS, "Keywords");
    }

    @Test
    void resolvesMissingSiteSettingsAsEmptySourceFields() {
        var config = new ConfigMap();

        assertThat(descriptor.resolveFields(config, Map.of()).block())
                .containsEntry(SiteLocalizationDescriptor.TITLE, "")
                .containsEntry(SiteLocalizationDescriptor.SUBTITLE, "")
                .containsEntry(SiteLocalizationDescriptor.SEO_DESCRIPTION, "")
                .containsEntry(SiteLocalizationDescriptor.SEO_KEYWORDS, "");
    }
}
