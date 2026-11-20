package run.halo.app.localization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.PageRequest;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;

class DefaultResourcePresentationServiceTest {
    final JsonMapper mapper = JsonMapper.builder().findAndAddModules().build();
    final ExtensionGetter extensions = mock(ExtensionGetter.class);
    final ResourceLocalizationProvider provider = mock(ResourceLocalizationProvider.class);
    final ResourceLocalizationService localization = mock(ResourceLocalizationService.class);
    final DefaultResourcePresentationService service = new DefaultResourcePresentationService(localization, extensions);
    final LocalizableResourceDescriptor<Product> descriptor =
            LocalizableResourceDescriptors.fromAnnotations(Product.class, mapper);
    final LocalizationContext english =
            new LocalizationContext(Locale.CHINESE, Locale.ENGLISH, List.of(), LocalizationScope.PUBLIC_API);
    final Product source = new Product("one", "源标题", "/original.png", new BigDecimal("15.00"));

    @BeforeEach
    void setup() {
        when(extensions.getEnabledExtension(ResourceLocalizationProvider.class)).thenReturn(Mono.just(provider));
        when(provider.resolveAll(any(), any())).thenReturn(Flux.empty());
    }

    @Test
    void decoratedDatabaseClientProjectsDetachedDtoAndPreservesDomainValues() {
        var adapter = adapter(List.of(source));
        when(provider.resolveAll(any(), any()))
                .thenReturn(Flux.just(new LocalizationPatch(
                        descriptor.reference(source),
                        Locale.ENGLISH,
                        List.of(
                                approved("heading", "源标题", "Translated title"),
                                approved("artwork", "/original.png", "/english.png"),
                                approved("price", "15.00", "0")))));

        StepVerifier.create(service.client(adapter)
                        .fetch("one")
                        .contextWrite(context -> context.put(LocalizationContext.class, english)))
                .assertNext(value -> {
                    assertThat(value.title()).isEqualTo("Translated title");
                    assertThat(value.image()).isEqualTo("/english.png");
                    assertThat(value.price()).isEqualTo(source.price());
                    assertThat(value.id()).isEqualTo(source.id());
                    assertThat(source.title()).isEqualTo("源标题");
                    assertThat(source.image()).isEqualTo("/original.png");
                })
                .verifyComplete();
    }

    @Test
    void unauthorizedOrMissingSourceDoesNotReachProviders() {
        StepVerifier.create(service.client(adapter(List.of()))
                        .fetch("missing")
                        .contextWrite(context -> context.put(LocalizationContext.class, english)))
                .verifyComplete();
        verifyNoInteractions(provider);
    }

    @Test
    void ineligibleDomainSourceDoesNotReachProviders() {
        var ineligible = spy(descriptor);
        doReturn(false).when(ineligible).isEligible(source);

        var result = service.presentAll(List.of(source), ineligible)
                .contextWrite(view -> view.put(LocalizationContext.class, english))
                .block();

        assertThat(result).containsExactly(source);
        assertThat(result.getFirst()).isSameAs(source);
        verifyNoInteractions(provider);
    }

    @Test
    void repeatedDomainIdentityIsResolvedOncePerBatch() {
        var result = service.presentAll(List.of(source, source), descriptor)
                .contextWrite(view -> view.put(LocalizationContext.class, english))
                .block();

        assertThat(result).containsExactly(source, source);
        verify(provider).resolveAll(Set.of(descriptor.reference(source)), english);
    }

    @Test
    void rawConsoleAndInternalReadsDoNotProject() {
        var client = service.client(adapter(List.of(source)));
        assertThat(client.fetch("one").block()).isEqualTo(source);
        for (var scope :
                List.of(LocalizationScope.CONSOLE, LocalizationScope.INTERNAL, LocalizationScope.SEARCH_INDEX)) {
            var context = new LocalizationContext(Locale.CHINESE, Locale.ENGLISH, List.of(), scope);
            assertThat(client.fetch("one")
                            .contextWrite(view -> view.put(LocalizationContext.class, context))
                            .block())
                    .isEqualTo(source);
        }
        verifyNoInteractions(provider);
    }

    @Test
    void derivedUrlsArePresentedOnlyForPublicConsumers() {
        var sourceUrl = "/archives/hello";
        when(provider.localizeUrl(sourceUrl, english)).thenReturn("/en/archives/hello");

        StepVerifier.create(service.presentUrl(sourceUrl)
                        .contextWrite(view -> view.put(LocalizationContext.class, english)))
                .expectNext("/en/archives/hello")
                .verifyComplete();

        var console = new LocalizationContext(Locale.CHINESE, Locale.ENGLISH, List.of(), LocalizationScope.CONSOLE);
        StepVerifier.create(service.presentUrl(sourceUrl)
                        .contextWrite(view -> view.put(LocalizationContext.class, console)))
                .expectNext(sourceUrl)
                .verifyComplete();
        verify(provider, times(1)).localizeUrl(sourceUrl, english);
    }

    @Test
    void derivedUrlPresentationFailuresReturnTheSourceUrl() {
        var sourceUrl = "/archives/hello";
        when(provider.localizeUrl(sourceUrl, english)).thenThrow(new IllegalStateException("Unavailable"));

        StepVerifier.create(service.presentUrl(sourceUrl)
                        .contextWrite(view -> view.put(LocalizationContext.class, english)))
                .expectNext(sourceUrl)
                .verifyComplete();
    }

    @Test
    void preservesPageMetadataAndBatchesWithoutReordering() {
        var sources = new ArrayList<Product>();
        for (int i = 0; i < 205; i++) {
            sources.add(new Product(Integer.toString(i), "标题", "/a.png", BigDecimal.ONE));
        }
        var adapter = adapter(sources);
        var result = service.client(adapter)
                .list(PageRequestImpl.of(3, 205))
                .contextWrite(view -> view.put(LocalizationContext.class, english))
                .block();
        assertThat(result.getPage()).isEqualTo(3);
        assertThat(result.getSize()).isEqualTo(205);
        assertThat(result.getTotal()).isEqualTo(999);
        assertThat(result.getItems()).containsExactlyElementsOf(sources);
        verify(provider, times(3)).resolveAll(any(), any());
    }

    @Test
    void dynamicDomainFieldsFollowStableEntryIdentitiesAfterReordering() {
        var declaration = new LocalizableResourceDescriptor<Catalog>() {
            @Override
            public String namespace() {
                return "example.shop";
            }

            @Override
            public String type() {
                return "catalog";
            }

            @Override
            public Class<Catalog> resourceClass() {
                return Catalog.class;
            }

            @Override
            public ResourceReference reference(Catalog source) {
                return ResourceReference.external(namespace(), type(), source.id());
            }

            @Override
            public List<LocalizableField> fields() {
                return List.of();
            }

            @Override
            public List<LocalizableFieldCollection> fieldCollections() {
                return List.of(LocalizableFieldCollection.builder("attributes", "/entries")
                        .valueField("value")
                        .entries(
                                "code",
                                entries -> entries.add("caption", LocalizableSchema.text())
                                        .add("artwork", LocalizableSchema.image())
                                        .add("destination", LocalizableSchema.link()))
                        .build());
            }
        };
        var original = new Catalog(
                "catalog",
                List.of(
                        new CatalogEntry("caption", "原描述", true),
                        new CatalogEntry("artwork", "/old.png", false),
                        new CatalogEntry("destination", "/old", true)));
        var tree = mapper.valueToTree(original);
        var replacements = java.util.Map.of("原描述", "Translated", "/old.png", "/new.png", "/old", "/new");
        var patchFields = declaration.resolveDisplayFields(tree).stream()
                .map(field -> {
                    var value = tree.at(field.pointer()).asText();
                    return approved(field.key(), value, replacements.get(value));
                })
                .toList();
        when(provider.resolveAll(any(), any()))
                .thenReturn(
                        Flux.just(new LocalizationPatch(declaration.reference(original), Locale.ENGLISH, patchFields)));
        var reordered = new Catalog(
                original.id(),
                List.of(
                        original.entries().get(2),
                        original.entries().getFirst(),
                        original.entries().get(1)));
        var result = service.presentAll(List.of(reordered), declaration)
                .contextWrite(view -> view.put(LocalizationContext.class, english))
                .block()
                .getFirst();
        assertThat(result.entries())
                .containsExactly(
                        new CatalogEntry("destination", "/new", true),
                        new CatalogEntry("caption", "Translated", true),
                        new CatalogEntry("artwork", "/new.png", false));
        assertThat(reordered.entries().getFirst().value()).isEqualTo("/old");
    }

    @Test
    void customPropertyModelIsSharedByPatchesAndNestedSchemas() {
        var customMapper = JsonMapper.builder()
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .build();
        var declaration = LocalizableResourceDescriptors.fromAnnotations(DisplayCatalog.class, customMapper);
        var catalogSource = new DisplayCatalog(
                "catalog", "源标题", List.of(new DisplayEntry("row", "原描述", "/old.png", "/old")), new BigDecimal("15.00"));
        var tree = declaration.toJson(catalogSource);
        var rows = tree.get("content_rows").deepCopy();
        ((ObjectNode) rows.get(0))
                .put("title_text", "Translated description")
                .put("image_url", "/new.png")
                .put("link_url", "/new");
        var rowsSchema = declaration.fields().stream()
                .filter(field -> field.key().equals("rows"))
                .findFirst()
                .orElseThrow()
                .schema();
        when(provider.resolveAll(any(), any()))
                .thenReturn(Flux.just(new LocalizationPatch(
                        declaration.reference(catalogSource),
                        Locale.ENGLISH,
                        List.of(
                                approved("heading", catalogSource.displayName(), "Translated title"),
                                approved(
                                        "rows",
                                        LocalizableValues.encode(rowsSchema, tree.get("content_rows")),
                                        LocalizableValues.encode(rowsSchema, rows))))));
        var result = service.presentAll(List.of(catalogSource), declaration)
                .contextWrite(view -> view.put(LocalizationContext.class, english))
                .block()
                .getFirst();

        assertThat(result.displayName()).isEqualTo("Translated title");
        assertThat(result.contentRows())
                .containsExactly(new DisplayEntry("row", "Translated description", "/new.png", "/new"));
        assertThat(result.recordId()).isEqualTo(catalogSource.recordId());
        assertThat(result.unitPrice()).isEqualTo(new BigDecimal("15.00"));
        assertThat(catalogSource.contentRows().getFirst().imageUrl()).isEqualTo("/old.png");
    }

    @Test
    void resolvesDocumentFormatsPerSourceAndRejectsStaleFormatAndPartialPairs() {
        var declaration = new NoteDescriptor();
        var html = new Note("html", "<p>源文</p>", "<p>源文</p>", "html", "visual-editor");
        var markdown = new Note("markdown", "<p>源文</p>", "源文", "markdown", "markdown-editor");
        var stale = new Note("stale", "<p>源文</p>", "源文", "html", "visual-editor");
        var partial = new Note("partial", markdown.html(), markdown.raw(), markdown.rawType(), markdown.editor());
        var markdownTranslation = documentPatch(declaration, markdown, "<p>Translated</p>", "Translated");
        when(provider.resolveAll(any(), any()))
                .thenReturn(Flux.just(
                        documentPatch(declaration, html, "<p>Translated</p>", "<p>Translated</p>"),
                        markdownTranslation,
                        new LocalizationPatch(
                                declaration.reference(stale), Locale.ENGLISH, markdownTranslation.fields()),
                        new LocalizationPatch(
                                declaration.reference(partial),
                                Locale.ENGLISH,
                                List.of(markdownTranslation.fields().getFirst()))));

        var result = service.presentAll(List.of(html, markdown, stale, partial), declaration)
                .contextWrite(view -> view.put(LocalizationContext.class, english))
                .block();

        assertThat(result)
                .containsExactly(
                        new Note("html", "<p>Translated</p>", "<p>Translated</p>", "html", "visual-editor"),
                        new Note("markdown", "<p>Translated</p>", "Translated", "markdown", "markdown-editor"),
                        stale,
                        partial);
        assertThat(html.html()).isEqualTo("<p>源文</p>");
    }

    @Test
    void resolvedDocumentsCannotChangeDeclaredFieldPairs() {
        var declaration = spy(new NoteDescriptor());
        var noteSource = new Note("note", "<p>源文</p>", "源文", "markdown", "markdown-editor");
        var patch = documentPatch(declaration, noteSource, "<p>Translated</p>", "Translated");
        doReturn(Mono.just(List.of(new LocalizableDocument("body", null, "html", null))))
                .when(declaration)
                .resolveDocuments(noteSource);
        when(provider.resolveAll(any(), any())).thenReturn(Flux.just(patch));

        var result = service.presentAll(List.of(noteSource), declaration)
                .contextWrite(view -> view.put(LocalizationContext.class, english))
                .block();

        assertThat(result).containsExactly(noteSource);
    }

    private LocalizationPatch documentPatch(NoteDescriptor descriptor, Note source, String html, String raw) {
        var hashes = LocalizationHash.sourceFields(
                Map.of("body", source.html(), "raw", source.raw()),
                List.of(new LocalizableDocument("body", "raw", source.rawType(), source.editor())));
        return new LocalizationPatch(
                descriptor.reference(source),
                Locale.ENGLISH,
                List.of(
                        new LocalizationPatch.Field("body", hashes.get("body"), html, LocalizationPatch.State.APPROVED),
                        new LocalizationPatch.Field("raw", hashes.get("raw"), raw, LocalizationPatch.State.APPROVED)));
    }

    @Test
    void concurrentLanguagesDoNotSharePresentationValues() {
        when(provider.resolveAll(any(), any())).thenAnswer(invocation -> {
            LocalizationContext context = invocation.getArgument(1);
            return Flux.just(new LocalizationPatch(
                    descriptor.reference(source),
                    context.targetLocale(),
                    List.of(approved(
                            "heading", source.title(), context.targetLocale().toLanguageTag()))));
        });
        var japanese =
                new LocalizationContext(Locale.CHINESE, Locale.JAPANESE, List.of(), LocalizationScope.PUBLIC_API);
        var client = service.client(adapter(List.of(source)));
        var results = Mono.zip(
                        client.fetch(source.id()).contextWrite(view -> view.put(LocalizationContext.class, english)),
                        client.fetch(source.id()).contextWrite(view -> view.put(LocalizationContext.class, japanese)))
                .block();
        assertThat(results.getT1().title()).isEqualTo("en");
        assertThat(results.getT2().title()).isEqualTo("ja");
        assertThat(source.title()).isEqualTo("源标题");
    }

    @Test
    void staleAndOtherTenantTranslationsAreNotApplied() {
        when(provider.resolveAll(any(), any()))
                .thenReturn(Flux.just(
                        new LocalizationPatch(
                                descriptor.reference(source),
                                Locale.ENGLISH,
                                List.of(approved("heading", "旧标题", "Stale"))),
                        new LocalizationPatch(
                                ResourceReference.external("other.tenant", "product", source.id()),
                                Locale.ENGLISH,
                                List.of(approved("heading", source.title(), "Wrong tenant")))));
        var result = service.client(adapter(List.of(source)))
                .fetch(source.id())
                .contextWrite(view -> view.put(LocalizationContext.class, english))
                .block();
        assertThat(result).isEqualTo(source);
    }

    @Test
    void providerReadsCannotRecursivelyInheritPresentationContext() {
        when(provider.resolveAll(any(), any()))
                .thenAnswer(invocation -> Flux.deferContextual(context -> {
                    assertThat(context.hasKey(LocalizationContext.class)).isFalse();
                    return Flux.empty();
                }));
        service.client(adapter(List.of(source)))
                .fetch(source.id())
                .contextWrite(view -> view.put(LocalizationContext.class, english))
                .block();
        verify(provider).resolveAll(any(), any());
    }

    @Test
    void sourceReadFailuresPropagateRatherThanBecomingEmptyPresentation() {
        var adapter = adapter(List.of(source));
        var failing = new LocalizableResourceSource<Product>() {
            public LocalizableResourceDescriptor<Product> descriptor() {
                return descriptor;
            }

            public Mono<Product> fetch(String id) {
                return Mono.error(new IllegalStateException("source unavailable"));
            }

            public Mono<ListResult<Product>> list(PageRequest page) {
                return adapter.list(page);
            }
        };
        StepVerifier.create(service.client(failing).fetch("one")).verifyErrorMessage("source unavailable");
    }

    private LocalizableResourceSource<Product> adapter(List<Product> sources) {
        return new LocalizableResourceSource<>() {
            public LocalizableResourceDescriptor<Product> descriptor() {
                return descriptor;
            }

            public Mono<Product> fetch(String id) {
                return Mono.justOrEmpty(
                        sources.stream().filter(value -> value.id().equals(id)).findFirst());
            }

            public Mono<ListResult<Product>> list(PageRequest page) {
                return Mono.just(new ListResult<>(page.getPageNumber(), page.getPageSize(), 999, sources));
            }
        };
    }

    private LocalizationPatch.Field approved(String key, String source, String value) {
        return new LocalizationPatch.Field(key, LocalizationHash.of(source), value, LocalizationPatch.State.APPROVED);
    }

    record Catalog(String id, List<CatalogEntry> entries) {}

    record CatalogEntry(String code, String value, boolean visible) {}

    @LocalizableResource(namespace = "example.shop", type = "display-catalog")
    record DisplayCatalog(
            @LocalizationId String recordId,
            @Localizable(key = "heading") String displayName,

            @Localizable(key = "rows", type = LocalizableSchema.Type.ARRAY, identityField = "row_id")
            List<DisplayEntry> contentRows,

            BigDecimal unitPrice) {}

    record DisplayEntry(
            @Localizable(policy = Localizable.Operation.PRESERVE)
            String rowId,

            @Localizable String titleText,

            @Localizable(type = LocalizableSchema.Type.IMAGE)
            String imageUrl,

            @Localizable(type = LocalizableSchema.Type.LINK) String linkUrl) {}

    record Note(String id, String html, String raw, String rawType, String editor) {}

    static class NoteDescriptor implements LocalizableResourceDescriptor<Note> {
        @Override
        public String namespace() {
            return "example.shop";
        }

        @Override
        public String type() {
            return "note";
        }

        @Override
        public Class<Note> resourceClass() {
            return Note.class;
        }

        @Override
        public ResourceReference reference(Note source) {
            return ResourceReference.external(namespace(), type(), source.id());
        }

        @Override
        public List<LocalizableField> fields() {
            return LocalizableField.list(LocalizableField.html("body", "/html"), LocalizableField.raw("raw", "/raw"));
        }

        @Override
        public List<LocalizableDocument> documents() {
            return List.of(new LocalizableDocument("body", "raw", "html", null));
        }

        @Override
        public Mono<List<LocalizableDocument>> resolveDocuments(Note source) {
            return Mono.just(List.of(new LocalizableDocument("body", "raw", source.rawType(), source.editor())));
        }
    }

    @LocalizableResource(namespace = "example.shop", type = "product", label = "商品")
    record Product(
            @LocalizationId String id,

            @LocalizableSearchTitle @Localizable(key = "heading")
            String title,

            @Localizable(key = "artwork", type = LocalizableSchema.Type.IMAGE)
            String image,

            BigDecimal price) {}
}
