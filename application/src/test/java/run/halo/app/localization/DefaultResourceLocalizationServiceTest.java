package run.halo.app.localization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.web.filter.reactive.ServerWebExchangeContextFilter.EXCHANGE_CONTEXT_ATTRIBUTE;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.MenuItem;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.Unstructured;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;
import run.halo.app.theme.finders.vo.MenuItemVo;

class DefaultResourceLocalizationServiceTest {
    ExtensionGetter getter;
    ResourceLocalizationProvider provider;
    DefaultResourceLocalizationService service;
    ResourceLocalizationDescriptor postDescriptor;
    LocalizationContext context =
            new LocalizationContext(Locale.CHINA, Locale.US, List.of(Locale.ENGLISH), LocalizationScope.PUBLIC_PAGE);

    @BeforeEach
    void setup() {
        getter = mock(ExtensionGetter.class);
        provider = mock(ResourceLocalizationProvider.class);
        when(getter.getEnabledExtension(ResourceLocalizationProvider.class)).thenReturn(Mono.just(provider));
        var mapper = JsonMapper.builder().findAndAddModules().build();
        postDescriptor = CombinedResourceLocalizationDescriptor.combine(
                        new LocalizableAnnotations(mapper).descriptor(Post.class), new PostLocalizationDescriptor())
                .orElseThrow();
        when(getter.getEnabledExtensions(ResourceLocalizationDescriptor.class)).thenReturn(Flux.just(postDescriptor));
        service = new DefaultResourceLocalizationService(getter, mapper);
    }

    Post source(String name) {
        Post post = new Post();
        Metadata metadata = new Metadata();
        metadata.setName(name);
        post.setMetadata(metadata);
        var spec = new Post.PostSpec();
        spec.setTitle("源标题");
        spec.setSlug("unchanged-slug");
        post.setSpec(spec);
        post.getStatusOrDefault().setExcerpt("源摘要");
        return post;
    }

    LocalizationPatch patch(Post post, Locale locale, LocalizationPatch.Field... fields) {
        return new LocalizationPatch(ResourceReference.from(post), locale, List.of(fields));
    }

    LocalizationPatch.Field approved(String key, String source, String value) {
        return new LocalizationPatch.Field(key, LocalizationHash.of(source), value, LocalizationPatch.State.APPROVED);
    }

    private LocalizationPatch.Field documentField(
            String key, Map<String, String> source, List<LocalizableDocument> documents, String value) {
        return new LocalizationPatch.Field(
                key,
                LocalizationHash.sourceFields(source, documents).get(key),
                value,
                LocalizationPatch.State.APPROVED);
    }

    @Test
    void extensionProjectionSupportsFieldsOutsideSpecAndStatus() {
        var source = new RootFieldExtension();
        var metadata = new Metadata();
        metadata.setName("one");
        source.setMetadata(metadata);
        source.setOutside("Source");
        var descriptor = new LocalizableAnnotations(
                        JsonMapper.builder().findAndAddModules().build())
                .descriptor(RootFieldExtension.class);
        when(getter.getEnabledExtensions(ResourceLocalizationDescriptor.class)).thenReturn(Flux.just(descriptor));
        when(provider.resolveAll(any(), any()))
                .thenReturn(Flux.just(new LocalizationPatch(
                        ResourceReference.from(source),
                        Locale.US,
                        List.of(approved("outside", "Source", "Translated")))));
        var projected = service.localize(source, context).block();
        assertThat(projected.getMetadata().getName()).isEqualTo("one");
        assertThat(projected.getOutside()).isEqualTo("Translated");
        assertThat(source.getOutside()).isEqualTo("Source");
    }

    @Test
    void explicitFieldsLocalizeNonExtensionResources() {
        var subject = ResourceReference.external("shop", "product", "42");
        var fields = LocalizableField.list(LocalizableField.text("title", "/title"));
        when(provider.resolveAll(any(), any()))
                .thenReturn(Flux.just(
                        new LocalizationPatch(subject, Locale.US, List.of(approved("title", "Source", "Translated")))));

        assertThat(service.localizeFields(subject, fields, Map.of("title", "Source"), context)
                        .block())
                .containsEntry("title", "Translated");
    }

    @Test
    void repeatedSourceIdentityIsResolvedOncePerBatch() {
        var source = source("one");
        when(provider.resolveAll(any(), any())).thenReturn(Flux.empty());

        assertThat(service.localizeAll(List.of(source, source), context).block())
                .containsExactly(source, source);

        verify(provider).resolveAll(Set.of(ResourceReference.from(source)), context);
    }

    @Test
    void descriptorAwareFieldProjectionRejectsNonExtensionReferences() {
        var subject = ResourceReference.external("shop", "product", "42");

        StepVerifier.create(service.localizeFields(subject, Map.of("title", "Source"), context))
                .expectErrorSatisfies(error -> assertThat(error)
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("explicit field"));
    }

    @Test
    void duplicateDescriptorsDoNotDisableUnrelatedResourceTranslations() {
        var post = source("one");
        var duplicate = new LocalizableAnnotations(JsonMapper.builder().build())
                .descriptor(run.halo.app.core.extension.content.Tag.class);
        when(getter.getEnabledExtensions(ResourceLocalizationDescriptor.class))
                .thenReturn(Flux.just(postDescriptor, duplicate, duplicate));
        when(provider.resolveAll(any(), any()))
                .thenReturn(Flux.just(patch(post, Locale.US, approved("title", "源标题", "Title"))));

        assertThat(service.localize(post, context).block().getSpec().getTitle()).isEqualTo("Title");
        var fields = Map.of("displayName", "Source tag");
        assertThat(service.localizeFields(new ResourceReference(duplicate.supports(), "tag"), fields, context)
                        .block())
                .isEqualTo(fields);
    }

    @Test
    void invalidDescriptorFieldsDoNotDisableUnrelatedResourceTranslations() {
        var post = source("one");
        var valid = new LocalizableAnnotations(JsonMapper.builder().build())
                .descriptor(run.halo.app.core.extension.content.Tag.class);
        var invalid = new ResourceLocalizationDescriptor() {
            @Override
            public GroupVersionKind supports() {
                return valid.supports();
            }

            @Override
            public List<LocalizableField> fields() {
                var field = valid.fields().getFirst();
                return List.of(field, field);
            }
        };
        when(getter.getEnabledExtensions(ResourceLocalizationDescriptor.class))
                .thenReturn(Flux.just(postDescriptor, invalid));
        when(provider.resolveAll(any(), any()))
                .thenReturn(Flux.just(patch(post, Locale.US, approved("title", "源标题", "Title"))));

        assertThat(service.localize(post, context).block().getSpec().getTitle()).isEqualTo("Title");
        var fields = Map.of("displayName", "Source tag");
        assertThat(service.localizeFields(new ResourceReference(invalid.supports(), "tag"), fields, context)
                        .block())
                .isEqualTo(fields);
    }

    @Test
    void projectsCopiesWithFieldFallbackAndCannotModifyUndeclaredFields() {
        var post = source("one");
        when(provider.resolveAll(any(), any()))
                .thenReturn(Flux.just(
                        patch(
                                post,
                                Locale.US,
                                approved("title", "源标题", "Title"),
                                approved("excerpt", "old excerpt", "Stale"),
                                approved("slug", "unchanged-slug", "bad")),
                        patch(post, Locale.ENGLISH, approved("excerpt", "源摘要", "Summary"))));
        service.localize(post, context)
                .as(StepVerifier::create)
                .assertNext(copy -> {
                    assertThat(copy).isNotSameAs(post);
                    assertThat(copy.getSpec().getTitle()).isEqualTo("Title");
                    assertThat(copy.getStatus().getExcerpt()).isEqualTo("Summary");
                    assertThat(copy.getSpec().getSlug()).isEqualTo("unchanged-slug");
                    assertThat(post.getSpec().getTitle()).isEqualTo("源标题");
                    assertThat(post.getStatus().getExcerpt()).isEqualTo("源摘要");
                })
                .verifyComplete();
    }

    @Test
    void publicPageApiAndSearchScopesRejectDraftsAndWrongSubjects() {
        var post = source("one");
        when(provider.resolveAll(any(), any()))
                .thenReturn(Flux.just(
                        patch(
                                post,
                                Locale.US,
                                new LocalizationPatch.Field(
                                        "title", LocalizationHash.of("源标题"), "Draft", LocalizationPatch.State.DRAFT)),
                        patch(source("other"), Locale.ENGLISH, approved("title", "源标题", "Other"))));
        for (var scope :
                List.of(LocalizationScope.PUBLIC_PAGE, LocalizationScope.PUBLIC_API, LocalizationScope.SEARCH_INDEX)) {
            var publicContext = new LocalizationContext(Locale.CHINA, Locale.US, List.of(Locale.ENGLISH), scope);
            service.localize(post, publicContext)
                    .as(StepVerifier::create)
                    .assertNext(copy -> assertThat(copy.getSpec().getTitle()).isEqualTo("源标题"))
                    .verifyComplete();
            assertThat(service.localizeFields(ResourceReference.from(post), Map.of("title", "源标题"), publicContext)
                            .block())
                    .containsEntry("title", "源标题");
        }
    }

    @Test
    void disabledProviderAndProviderErrorsReturnSource() {
        var post = source("one");
        post.getStatus().setExcerpt(null);
        post.getStatus().setPermalink("/archives/one");
        when(getter.getEnabledExtension(ResourceLocalizationProvider.class)).thenReturn(Mono.empty());
        service.localize(post, context)
                .as(StepVerifier::create)
                .assertNext(copy -> {
                    assertThat(copy).isSameAs(post);
                    assertThat(copy.getSpec().getCover()).isNull();
                    assertThat(copy.getStatus().getExcerpt()).isNull();
                    assertThat(copy.getStatus().getPermalink()).isEqualTo("/archives/one");
                })
                .verifyComplete();
        when(getter.getEnabledExtension(ResourceLocalizationProvider.class))
                .thenReturn(Mono.error(new IllegalStateException("disabled")));
        service.localize(post, context)
                .as(StepVerifier::create)
                .expectNext(post)
                .verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "/archives/one%20two?page=2&next=%2F#title",
                "/blog/archives/one%20two?page=2&next=%2F#title",
                "https://example.org/archives/one%20two?page=2&next=%2F#title",
                "//example.org/archives/one%20two?page=2&next=%2F#title"
            })
    void passesPermalinksToProviderWithoutChangingSourceOrResult(String sourceLink) {
        var post = source("one");
        post.getStatus().setPermalink(sourceLink);
        var translatedLink = "/blog/en/archives/one%20two?page=2&next=%2F#title";
        var request = MockServerHttpRequest.get("https://example.org/blog/en/archives")
                .contextPath("/blog")
                .build();
        var exchange = MockServerWebExchange.from(request);
        when(provider.resolveAll(any(), any())).thenReturn(Flux.empty());
        when(provider.localizeUrl(eq(sourceLink), any())).thenReturn(translatedLink);

        for (var scope :
                List.of(LocalizationScope.PUBLIC_PAGE, LocalizationScope.PUBLIC_API, LocalizationScope.SEARCH_INDEX)) {
            var localization = new LocalizationContext(Locale.CHINA, Locale.US, List.of(), scope);
            var projected = service.localize(post, localization)
                    .contextWrite(reactor -> reactor.put(EXCHANGE_CONTEXT_ATTRIBUTE, exchange))
                    .block();

            assertThat(projected.getStatus().getPermalink()).isEqualTo(translatedLink);
            verify(provider).localizeUrl(sourceLink, localization);
        }
        assertThat(post.getStatus().getPermalink()).isEqualTo(sourceLink);
    }

    @Test
    void missingOrFailingUrlTranslationKeepsTheLinkWithoutDiscardingTranslatedFields() {
        var post = source("one");
        var sourceLink = "/archives/one%20two?page=2&next=%2F#title";
        post.getStatus().setPermalink(sourceLink);
        when(provider.resolveAll(any(), any()))
                .thenReturn(Flux.just(patch(post, Locale.US, approved("title", "源标题", "Title"))));
        when(provider.localizeUrl(sourceLink, context))
                .thenReturn(null)
                .thenThrow(new IllegalStateException("Unavailable"));

        for (int i = 0; i < 2; i++) {
            var projected = service.localize(post, context).block();

            assertThat(projected.getSpec().getTitle()).isEqualTo("Title");
            assertThat(projected.getStatus().getPermalink()).isEqualTo(sourceLink);
        }
        assertThat(post.getSpec().getTitle()).isEqualTo("源标题");
        assertThat(post.getStatus().getPermalink()).isEqualTo(sourceLink);
    }

    @Test
    void preservesUnchangedAndExternalPermalinksAndSkipsSourceLanguage() {
        var post = source("one");
        when(provider.resolveAll(any(), any())).thenReturn(Flux.empty());
        when(provider.localizeUrl(anyString(), eq(context))).thenAnswer(call -> call.getArgument(0));
        for (var link :
                List.of("/archives/one", "https://other.example/archives/one", "//other.example/archives/one")) {
            post.getStatus().setPermalink(link);
            assertThat(service.localize(post, context).block()).isSameAs(post);
        }
        clearInvocations(provider);
        var sourceContext =
                new LocalizationContext(Locale.CHINA, Locale.CHINA, List.of(), LocalizationScope.PUBLIC_PAGE);
        assertThat(service.localize(post, sourceContext).block()).isSameAs(post);
        verifyNoInteractions(provider);
    }

    @Test
    void absentTranslationsReturnSourceWithoutNormalizingNullFields() {
        var post = source("untranslated");
        post.getStatus().setExcerpt(null);
        when(provider.resolveAll(any(), any())).thenReturn(Flux.empty());

        assertThat(service.localize(post, context).block()).isSameAs(post);
        assertThat(post.getSpec().getCover()).isNull();
        assertThat(post.getStatus().getExcerpt()).isNull();
    }

    @Test
    void translatedTitlePreservesUntranslatedNullAndMissingFields() {
        var post = source("translated");
        post.getStatus().setExcerpt(null);
        when(provider.resolveAll(any(), any()))
                .thenReturn(Flux.just(patch(post, Locale.US, approved("title", "源标题", "Title"))));

        for (var inclusion : List.of(JsonInclude.Include.ALWAYS, JsonInclude.Include.NON_NULL)) {
            var mapper = JsonMapper.builder().findAndAddModules().build();
            mapper.setSerializationInclusion(inclusion);
            var projectionService = new DefaultResourceLocalizationService(getter, mapper);

            var copy = projectionService.localize(post, context).block();

            assertThat(copy).isNotNull().isNotSameAs(post);
            assertThat(copy.getSpec().getTitle()).isEqualTo("Title");
            assertThat(copy.getSpec().getCover()).as("cover with %s", inclusion).isNull();
            assertThat(copy.getStatus().getExcerpt())
                    .as("excerpt with %s", inclusion)
                    .isNull();
            assertThat(post.getSpec().getTitle()).isEqualTo("源标题");
        }
    }

    @Test
    void approvedEmptyTranslationCanReplaceANullField() {
        var post = source("empty-translation");
        post.getStatus().setExcerpt(null);
        when(provider.resolveAll(any(), any()))
                .thenReturn(Flux.just(patch(post, Locale.US, approved("excerpt", "", ""))));

        var copy = service.localize(post, context).block();

        assertThat(copy).isNotNull().isNotSameAs(post);
        assertThat(copy.getStatus().getExcerpt()).isEmpty();
        assertThat(post.getStatus().getExcerpt()).isNull();
    }

    @Test
    void semanticNullFieldsRetainTheirValuesUnlessATranslationIsApproved() {
        var descriptor = new SiteLocalizationDescriptor();
        when(getter.getEnabledExtensions(ResourceLocalizationDescriptor.class)).thenReturn(Flux.just(descriptor));
        var subject = new ResourceReference(descriptor.supports(), "system");
        var sources = new LinkedHashMap<String, String>();
        sources.put("title", null);
        sources.put("subtitle", null);
        when(provider.resolveAll(any(), any())).thenReturn(Flux.empty());

        assertThat(service.localizeFields(subject, sources, context).block()).isEqualTo(sources);

        when(provider.resolveAll(any(), any()))
                .thenReturn(
                        Flux.just(new LocalizationPatch(subject, Locale.US, List.of(approved("title", "", "Title")))));

        assertThat(service.localizeFields(subject, sources, context).block())
                .containsEntry("title", "Title")
                .containsEntry("subtitle", null);
        assertThat(sources).containsEntry("title", null).containsEntry("subtitle", null);
    }

    @Test
    void batchesAndPreservesOrderAndCount() {
        var posts = new ArrayList<Post>();
        for (int i = 0; i < 205; i++) {
            posts.add(source("post-" + i));
        }
        when(provider.resolveAll(any(), any())).thenReturn(Flux.empty());
        service.localizeAll(posts, context)
                .as(StepVerifier::create)
                .assertNext(result -> {
                    assertThat(result).hasSize(205);
                    assertThat(result.stream()
                                    .map(p -> p.getMetadata().getName())
                                    .toList())
                            .containsExactlyElementsOf(posts.stream()
                                    .map(p -> p.getMetadata().getName())
                                    .toList());
                })
                .verifyComplete();
        verify(provider, times(3)).resolveAll(any(), any());
    }

    @Test
    void semanticFieldsAreAllowlistedAndFailOpen() {
        var post = source("one");
        var documents = new PostLocalizationDescriptor().documents();
        var sources = Map.of("content.html", "<p>源文</p>", "content.raw", "<p>源文</p>", "secret", "private");
        when(provider.resolveAll(any(), any()))
                .thenReturn(Flux.just(patch(
                        post,
                        Locale.US,
                        documentField("content.html", sources, documents, "<p>Translation</p>"),
                        documentField("content.raw", sources, documents, "<p>Translation</p>"),
                        approved("secret", "private", "leak"))));
        service.localizeFields(ResourceReference.from(post), sources, documents, context)
                .as(StepVerifier::create)
                .assertNext(fields -> {
                    assertThat(fields.get("content.html")).isEqualTo("<p>Translation</p>");
                    assertThat(fields.get("secret")).isEqualTo("private");
                })
                .verifyComplete();
    }

    @Test
    void consoleAndInternalScopesDoNotCallProvider() {
        var post = source("one");
        post.getStatus().setPermalink("/archives/one?page=2#title");
        for (var scope : List.of(LocalizationScope.CONSOLE, LocalizationScope.INTERNAL)) {
            var internal = new LocalizationContext(Locale.CHINA, Locale.US, List.of(), scope);
            service.localize(post, internal)
                    .as(StepVerifier::create)
                    .expectNext(post)
                    .verifyComplete();
        }
        verifyNoInteractions(provider);
    }

    @Test
    void builtinDocumentPairsFallBackTogether() {
        var sources = Map.of("content.html", "<p>Source</p>", "content.raw", "<p>Source</p>");
        for (var descriptor : builtinContentDescriptors()) {
            var documents = descriptor.documents();
            when(getter.getEnabledExtensions(ResourceLocalizationDescriptor.class))
                    .thenReturn(Flux.just(descriptor));
            var subject = new ResourceReference(descriptor.supports(), "document");
            when(provider.resolveAll(any(), any()))
                    .thenReturn(Flux.just(
                            new LocalizationPatch(
                                    subject,
                                    Locale.US,
                                    List.of(documentField("content.html", sources, documents, "US"))),
                            new LocalizationPatch(
                                    subject,
                                    Locale.ENGLISH,
                                    List.of(
                                            documentField("content.html", sources, documents, "EN"),
                                            documentField("content.raw", sources, documents, "EN")))));

            assertThat(service.localizeFields(subject, sources, documents, context)
                            .block())
                    .as("%s document", descriptor.supports().kind())
                    .containsEntry("content.html", "EN")
                    .containsEntry("content.raw", "EN");
        }
    }

    @Test
    void builtinDocumentPairsRejectIncompleteAndStaleTranslations() {
        var sources = Map.of("content.html", "<p>Source</p>", "content.raw", "<p>Source</p>\n");
        for (var descriptor : builtinContentDescriptors()) {
            var documents = descriptor.documents();
            var html = documentField("content.html", sources, documents, "<p>Translation</p>");
            var raw = documentField("content.raw", sources, documents, "<p>Translation</p>\n");
            var originalRaw = Map.of("content.html", sources.get("content.html"), "content.raw", "old raw");
            var originalHtml = Map.of("content.html", "<p>Old source</p>", "content.raw", sources.get("content.raw"));
            var incompleteOrStale = List.of(
                    List.of(html),
                    List.of(raw),
                    List.of(html, documentField("content.raw", originalRaw, documents, "old translation")),
                    List.of(documentField("content.html", originalHtml, documents, "old translation"), raw));
            when(getter.getEnabledExtensions(ResourceLocalizationDescriptor.class))
                    .thenReturn(Flux.just(descriptor));
            var subject = new ResourceReference(descriptor.supports(), "document");
            for (var fields : incompleteOrStale) {
                when(provider.resolveAll(any(), any()))
                        .thenReturn(Flux.just(new LocalizationPatch(subject, Locale.US, fields)));

                assertThat(service.localizeFields(subject, sources, documents, context)
                                .block())
                        .as("%s with %s", descriptor.supports().kind(), fields)
                        .isEqualTo(sources);
            }
        }
    }

    @Test
    void builtinDocumentPairsCanTranslateHtmlWithNullSourceRaw() {
        var sources = new LinkedHashMap<String, String>();
        sources.put("content.html", "<p>Source</p>");
        sources.put("content.raw", null);
        for (var descriptor : builtinContentDescriptors()) {
            var documents = descriptor.documents();
            when(getter.getEnabledExtensions(ResourceLocalizationDescriptor.class))
                    .thenReturn(Flux.just(descriptor));
            var subject = new ResourceReference(descriptor.supports(), "html-document");
            when(provider.resolveAll(any(), any()))
                    .thenReturn(Flux.just(new LocalizationPatch(
                            subject,
                            Locale.US,
                            List.of(
                                    documentField("content.html", sources, documents, "<p>Translation</p>"),
                                    documentField("content.raw", sources, documents, "<p>Translation</p>")))));

            assertThat(service.localizeFields(subject, sources, documents, context)
                            .block())
                    .containsEntry("content.html", "<p>Translation</p>")
                    .containsEntry("content.raw", "<p>Translation</p>");
            assertThat(sources).containsEntry("content.raw", null);
        }
    }

    @Test
    void builtinDocumentPairsPreserveMarkdownRaw() {
        var sources = Map.of("content.html", "<h1>Source</h1>", "content.raw", "# Source");
        var markdown = List.of(new LocalizableDocument("content.html", "content.raw", "markdown", null));
        for (var descriptor : builtinContentDescriptors()) {
            when(getter.getEnabledExtensions(ResourceLocalizationDescriptor.class))
                    .thenReturn(Flux.just(descriptor));
            var subject = new ResourceReference(descriptor.supports(), "markdown-document");
            when(provider.resolveAll(any(), any()))
                    .thenReturn(Flux.just(new LocalizationPatch(
                            subject,
                            Locale.US,
                            List.of(
                                    documentField("content.html", sources, markdown, "<h1>Translation</h1>"),
                                    documentField("content.raw", sources, markdown, "# Translation")))));

            assertThat(service.localizeFields(subject, sources, markdown, context)
                            .block())
                    .as("%s Markdown document", descriptor.supports().kind())
                    .containsEntry("content.html", "<h1>Translation</h1>")
                    .containsEntry("content.raw", "# Translation");
            assertThat(service.localizeFields(subject, sources, descriptor.documents(), context)
                            .block())
                    .as("%s after a format-only change", descriptor.supports().kind())
                    .isEqualTo(sources);
            assertThat(service.localizeFields(subject, sources, context).block())
                    .isEqualTo(sources);
        }
    }

    private List<ResourceLocalizationDescriptor> builtinContentDescriptors() {
        return List.of(new PostLocalizationDescriptor(), new SinglePageLocalizationDescriptor());
    }

    @Test
    void pluginDocumentsStillRequireTheirDeclaredFieldPairs() {
        var descriptor = new ResourceLocalizationDescriptor() {
            @Override
            public GroupVersionKind supports() {
                return new GroupVersionKind("example.halo.run", "v1alpha1", "Document");
            }

            @Override
            public List<LocalizableField> fields() {
                return List.of(
                        new LocalizableField("body.html", null, LocalizableField.Format.HTML),
                        new LocalizableField("body.raw", null, LocalizableField.Format.RAW_CONTENT));
            }

            @Override
            public List<LocalizableDocument> documents() {
                return List.of(new LocalizableDocument("body.html", "body.raw", "custom-format", "custom-editor"));
            }
        };
        when(getter.getEnabledExtensions(ResourceLocalizationDescriptor.class)).thenReturn(Flux.just(descriptor));
        var subject = new ResourceReference(descriptor.supports(), "custom-document");
        var sources = Map.of("body.html", "<p>Source</p>", "body.raw", "{source}");
        var documents = descriptor.documents();
        var html = documentField("body.html", sources, documents, "<p>Translation</p>");
        when(provider.resolveAll(any(), any()))
                .thenReturn(Flux.just(new LocalizationPatch(subject, Locale.US, List.of(html))));

        assertThat(service.localizeFields(subject, sources, documents, context).block())
                .isEqualTo(sources);

        when(provider.resolveAll(any(), any()))
                .thenReturn(Flux.just(new LocalizationPatch(
                        subject,
                        Locale.US,
                        List.of(html, documentField("body.raw", sources, documents, "{translation}")))));

        assertThat(service.localizeFields(subject, sources, documents, context).block())
                .containsEntry("body.html", "<p>Translation</p>")
                .containsEntry("body.raw", "{translation}");
    }

    @Test
    void objectProjectionUsesPluginDocumentsResolvedForThisResource() {
        var source = new Unstructured(Map.of(
                "apiVersion",
                "example.halo.run/v1alpha1",
                "kind",
                "Note",
                "metadata",
                Map.of("name", "note"),
                "spec",
                Map.of("html", "<p>Source</p>", "raw", "{source}")));
        var current = List.of(new LocalizableDocument("html", "raw", "example-json", null));
        var old = List.of(new LocalizableDocument("html", "raw", "example-text", null));
        var descriptor = new ResourceLocalizationDescriptor() {
            @Override
            public GroupVersionKind supports() {
                return source.groupVersionKind();
            }

            @Override
            public List<LocalizableField> fields() {
                return List.of(
                        new LocalizableField("html", "/spec/html", LocalizableField.Format.HTML),
                        new LocalizableField("raw", "/spec/raw", LocalizableField.Format.RAW_CONTENT));
            }

            @Override
            public List<LocalizableDocument> documents() {
                return old;
            }

            @Override
            public Mono<List<LocalizableDocument>> resolveDocuments(run.halo.app.extension.Extension resource) {
                return Mono.just(current);
            }
        };
        when(getter.getEnabledExtensions(ResourceLocalizationDescriptor.class)).thenReturn(Flux.just(descriptor));
        var values = Map.of("html", "<p>Source</p>", "raw", "{source}");
        var subject = ResourceReference.from(source);
        var known = new LocalizationPatch(
                subject,
                Locale.ENGLISH,
                List.of(
                        documentField("html", values, current, "<p>Translation</p>"),
                        documentField("raw", values, current, "{translation}")));
        var stale = new LocalizationPatch(
                subject,
                Locale.US,
                List.of(
                        documentField("html", values, old, "<p>Stale</p>"),
                        documentField("raw", values, old, "{stale}")));
        when(provider.resolveAll(any(), any())).thenReturn(Flux.just(stale, known));

        var projected = service.localize(source, context).block();

        assertThat(projected).isNotSameAs(source);
        assertThat(((Map<?, ?>) projected.getData().get("spec")).get("raw")).isEqualTo("{translation}");
        assertThat(((Map<?, ?>) source.getData().get("spec")).get("raw")).isEqualTo("{source}");
    }

    @Test
    void menuItemLocalizesItsResolvedDisplayName() {
        var item = new MenuItem();
        var metadata = new Metadata();
        metadata.setName("item");
        item.setMetadata(metadata);
        item.setSpec(new MenuItem.MenuItemSpec());
        item.getSpec().setDisplayName("Configured label");
        item.getSpec().setHref("/outside-app/menu");
        item.setStatus(new MenuItem.MenuItemStatus());
        item.getStatus().setDisplayName("Resolved label");
        when(provider.localizeUrl(anyString(), eq(context))).thenAnswer(call -> call.getArgument(0));
        var subject = ResourceReference.from(item);
        var descriptor = new LocalizableAnnotations(
                        JsonMapper.builder().findAndAddModules().build())
                .descriptor(MenuItem.class);
        when(getter.getEnabledExtensions(ResourceLocalizationDescriptor.class)).thenReturn(Flux.just(descriptor));
        when(provider.resolveAll(any(), any()))
                .thenReturn(Flux.just(new LocalizationPatch(
                        subject, Locale.US, List.of(approved("displayName", "Resolved label", "Localized label")))));

        var localized = service.localize(item, context).block();

        assertThat(MenuItemVo.from(localized).getDisplayName()).isEqualTo("Localized label");
        assertThat(localized.getStatus().getDisplayName()).isEqualTo("Localized label");
        assertThat(localized.getSpec().getDisplayName()).isEqualTo("Configured label");
        assertThat(localized.getSpec().getHref()).isEqualTo("/outside-app/menu");
        verify(provider).localizeUrl("/outside-app/menu", context);
        assertThat(item.getSpec().getDisplayName()).isEqualTo("Configured label");
        assertThat(item.getStatus().getDisplayName()).isEqualTo("Resolved label");
    }

    @Test
    void targetMenuItemKeepsItsResolvedStatusDisplayName() {
        var item = new MenuItem();
        var metadata = new Metadata();
        metadata.setName("item");
        item.setMetadata(metadata);
        item.setSpec(new MenuItem.MenuItemSpec());
        item.getSpec().setTargetRef(run.halo.app.extension.Ref.of("post", Post.GVK));
        item.setStatus(new MenuItem.MenuItemStatus());
        item.getStatus().setDisplayName("Resolved label");
        when(provider.resolveAll(any(), any())).thenReturn(Flux.empty());

        var projected = service.localize(item, context).block();

        assertThat(projected).isSameAs(item);
        assertThat(MenuItemVo.from(projected).getDisplayName()).isEqualTo("Resolved label");
    }

    @Test
    void resourceOwnerCanExcludeNonPublicContent() {
        var post = source("private");
        var descriptor = new PostLocalizationDescriptor() {
            @Override
            public boolean isEligible(run.halo.app.extension.Extension source) {
                return false;
            }
        };
        when(getter.getEnabledExtensions(ResourceLocalizationDescriptor.class)).thenReturn(Flux.just(descriptor));
        when(provider.resolveAll(any(), any()))
                .thenReturn(Flux.just(patch(post, Locale.US, approved("title", "源标题", "Private"))));
        assertThat(service.localize(post, context).block()).isSameAs(post);
    }

    @GVK(group = "example.com", version = "v1", kind = "RootField", plural = "root-fields", singular = "root-field")
    static class RootFieldExtension extends AbstractExtension {
        @Localizable(key = "outside")
        private String outside;

        public String getOutside() {
            return outside;
        }

        public void setOutside(String outside) {
            this.outside = outside;
        }
    }
}
