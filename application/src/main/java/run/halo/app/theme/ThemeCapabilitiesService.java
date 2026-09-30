package run.halo.app.theme;

import static run.halo.app.theme.utils.PatternUtils.normalizePattern;
import static run.halo.app.theme.utils.PatternUtils.normalizePostPattern;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.thymeleaf.autoconfigure.ThymeleafProperties;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.halo.app.core.extension.Theme;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting.ThemeRouteRules;
import run.halo.app.infra.ThemeRootGetter;
import run.halo.app.infra.utils.FileUtils;
import run.halo.app.plugin.UiPluginBundleService;
import run.halo.app.theme.ThemeCapabilities.ThemeTemplateFile;
import run.halo.app.theme.ThemeCapabilities.ThemeTemplateUsage;

@Component
@RequiredArgsConstructor
public class ThemeCapabilitiesService {
    private static final Set<String> SYSTEM_PAGE_TEMPLATES = Set.of(
            "login",
            "signup",
            "logout",
            "setup",
            "complete_profile",
            "login_oauth2_select",
            "password-reset/email/send",
            "password-reset/email/reset",
            "challenges/two-factor/totp");

    private final ThemeRootGetter themeRoot;
    private final ThymeleafProperties thymeleafProperties;
    private final ThemeLayoutCompatibilityChecker layoutChecker;
    private final UiPluginBundleService uiPluginBundleService;

    private final SystemConfigFetcher systemConfigFetcher;

    public Mono<ThemeCapabilities> inspect(Theme theme) {
        return Mono.zip(
                        Mono.fromCallable(() -> inspectTemplates(theme)).subscribeOn(Schedulers.boundedElastic()),
                        uiPluginBundleService.getThemeUiResources(theme),
                        systemConfigFetcher.fetchRouteRules().defaultIfEmpty(ThemeRouteRules.empty()))
                .map(result -> new ThemeCapabilities(
                        result.getT1().templates(), routes(result.getT3()),
                        result.getT1().pageLayout(), result.getT2()));
    }

    private Map<String, String> routes(ThemeRouteRules rules) {
        var suffix = thymeleafProperties.getSuffix();
        var categories = normalizePattern(rules.getCategories());
        var tags = normalizePattern(rules.getTags());
        return Map.of(
                "index" + suffix,
                "/",
                "archives" + suffix,
                normalizePattern(rules.getArchives()),
                "categories" + suffix,
                categories,
                "category" + suffix,
                categories + "/{slug}",
                "tags" + suffix,
                tags,
                "tag" + suffix,
                tags + "/{slug}",
                "post" + suffix,
                normalizePostPattern(rules),
                "page" + suffix,
                "/{slug}",
                "author" + suffix,
                "/authors/{name}");
    }

    TemplateInventory inspectTemplates(Theme theme) throws IOException {
        var root = themeRoot.get().toAbsolutePath().normalize();
        var themePath = root.resolve(theme.getMetadata().getName()).normalize();
        FileUtils.checkDirectoryTraversal(root, themePath);
        // A theme root may itself be a linked local development workspace.
        var realThemePath = themePath.toRealPath();
        var templates = realThemePath.resolve("templates");
        var suffix = thymeleafProperties.getSuffix();
        var usages = new LinkedHashMap<String, List<ThemeTemplateUsage>>();
        for (var template : DefaultTemplateEnum.values()) {
            addUsage(
                    usages,
                    template.getValue() + suffix,
                    new ThemeTemplateUsage("system", template.getValue(), null, null));
        }
        addUsage(usages, "layout.html", new ThemeTemplateUsage("layout", null, null, null));
        var custom = theme.getSpec().getCustomTemplates();
        if (custom != null) {
            addCustomTemplates(usages, custom.getPost(), "post", suffix);
            addCustomTemplates(usages, custom.getCategory(), "category", suffix);
            addCustomTemplates(usages, custom.getPage(), "page", suffix);
        }

        if (Files.exists(templates, LinkOption.NOFOLLOW_LINKS)) {
            var realTemplates = templates.toRealPath();
            if (!Files.isDirectory(realTemplates) || !realTemplates.startsWith(realThemePath)) {
                throw new IOException("The templates directory must be inside the theme directory.");
            }
            Files.walkFileTree(realTemplates, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    return dir.equals(realTemplates.resolve("assets"))
                            ? FileVisitResult.SKIP_SUBTREE
                            : FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    var path = realTemplates.relativize(file).toString().replace('\\', '/');
                    if (path.endsWith(suffix)) {
                        var name = path.substring(0, path.length() - suffix.length());
                        var errorTemplate = name.equals("error") || name.matches("error/(?:[45][0-9]{2}|[45]xx|error)");
                        if (errorTemplate || SYSTEM_PAGE_TEMPLATES.contains(name)) {
                            addUsage(
                                    usages,
                                    path,
                                    new ThemeTemplateUsage("system", errorTemplate ? "error" : null, null, null));
                        } else {
                            usages.computeIfAbsent(
                                    path,
                                    ignored -> new ArrayList<>(
                                            List.of(new ThemeTemplateUsage("other", null, null, null))));
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        var files = usages.entrySet().stream()
                .filter(entry -> isReadableTemplate(templates, realThemePath, entry.getKey()))
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new ThemeTemplateFile(entry.getKey(), entry.getValue()))
                .toList();
        Theme.ThemeStatus.PageLayout layout;
        if (!Files.exists(templates.resolve("layout.html"), LinkOption.NOFOLLOW_LINKS)
                || isReadableTemplate(templates, realThemePath, "layout.html")) {
            layout = layoutChecker.check(realThemePath);
        } else {
            layout = new Theme.ThemeStatus.PageLayout();
            layout.setState(Theme.PageLayoutState.INVALID);
            layout.setTemplate(PageLayoutContract.TEMPLATE_FILE);
            layout.setReason("UnreadableLayoutTemplate");
            layout.setMessage("The layout template is not readable inside the theme templates directory.");
        }
        return new TemplateInventory(files, layout);
    }

    private static void addCustomTemplates(
            Map<String, List<ThemeTemplateUsage>> usages,
            List<Theme.TemplateDescriptor> declarations,
            String type,
            String suffix) {
        if (declarations == null) {
            return;
        }
        for (var declaration : declarations) {
            var file = declaration.getFile();
            try {
                var normalized = Path.of(file).normalize().toString().replace('\\', '/');
                file = normalized.endsWith(suffix) ? normalized : normalized + suffix;
            } catch (InvalidPathException e) {
                // Invalid declarations are excluded by the readability check.
            }
            addUsage(
                    usages,
                    file,
                    new ThemeTemplateUsage("custom", type, declaration.getName(), declaration.getDescription()));
        }
    }

    private static void addUsage(Map<String, List<ThemeTemplateUsage>> usages, String file, ThemeTemplateUsage usage) {
        usages.computeIfAbsent(file, ignored -> new ArrayList<>()).add(usage);
    }

    private static boolean isReadableTemplate(Path templates, Path themePath, String file) {
        try {
            var path = templates.resolve(file).normalize();
            if (!path.startsWith(templates) || Path.of(file).isAbsolute() || !Files.isRegularFile(path)) {
                return false;
            }
            var realTemplates = templates.toRealPath();
            return realTemplates.startsWith(themePath)
                    && path.toRealPath().startsWith(realTemplates)
                    && Files.isReadable(path);
        } catch (InvalidPathException | IOException e) {
            return false;
        }
    }

    record TemplateInventory(List<ThemeTemplateFile> templates, Theme.ThemeStatus.PageLayout pageLayout) {}
}
