package run.halo.app.localization;

import static run.halo.app.localization.LocalizableSchema.text;

import java.util.List;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.extension.GroupVersionKind;

/**
 * Declares public display fields of SinglePage resources.
 *
 * @since 2.28.0
 */
@Component
public class SinglePageLocalizationDescriptor implements ResourceLocalizationDescriptor {
    @Override
    public GroupVersionKind supports() {
        return GroupVersionKind.fromExtension(SinglePage.class);
    }

    @Override
    public List<LocalizableField> fields() {
        return LocalizableField.list(
                LocalizableField.html("content.html", null).label("core.localization.field.body"),
                LocalizableField.raw("content.raw", null).label("core.localization.field.sourceBody"));
    }

    @Override
    public List<LocalizableFieldCollection> fieldCollections() {
        return List.of(LocalizableFieldCollection.builder("htmlMetas", "/spec/htmlMetas")
                .valueField("content")
                .group("core.localization.group.seo")
                .entries(
                        "name",
                        entries -> entries.add(
                                        "description",
                                        text().label("core.localization.field.seoDescription")
                                                .multiline(true))
                                .add("keywords", text().label("core.localization.field.seoKeywords"))
                                .add("twitter:title", text().label("core.localization.field.twitterTitle"))
                                .add(
                                        "twitter:description",
                                        text().label("core.localization.field.twitterDescription")
                                                .multiline(true)))
                .entries(
                        "property",
                        entries -> entries.add("og:title", text().label("core.localization.field.sharingTitle"))
                                .add(
                                        "og:description",
                                        text().label("core.localization.field.sharingDescription")
                                                .multiline(true)))
                .build());
    }

    @Override
    public List<LocalizableDocument> documents() {
        return List.of(new LocalizableDocument("content.html", "content.raw", "HTML", null));
    }

    @Override
    public String displayName() {
        return "core.page.title";
    }

    @Override
    public String titleField() {
        return "title";
    }
}
