package run.halo.app.theme;

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
import run.halo.app.infra.ThemeRootGetter;
import run.halo.app.infra.utils.FileUtils;
import run.halo.app.plugin.UiPluginBundleService;
import run.halo.app.theme.ThemeCapabilities.ThemeTemplateFile;
import run.halo.app.theme.ThemeCapabilities.ThemeTemplateUsage;

@Component
@RequiredArgsConstructor
public class ThemeCapabilitiesService {
    // ponytail: bound directory inspection; use paginated traversal if larger themes need full inventories.
    static final int MAX_ENTRIES = 2000;

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

    public Mono<ThemeCapabilities> inspect(Theme theme) {
        return Mono.fromCallable(() -> inspectTemplates(theme))
                .subscribeOn(Schedulers.boundedElastic())
                .zipWith(uiPluginBundleService.getThemeUiResources(theme))
                .map(result -> new ThemeCapabilities(
                        result.getT1().templates(), result.getT1().complete(),
                        result.getT1().pageLayout(), result.getT2()));
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

        var complete = new boolean[] {true};
        if (Files.exists(templates, LinkOption.NOFOLLOW_LINKS)) {
            if (!Files.isDirectory(templates) || !templates.toRealPath().startsWith(realThemePath)) {
                complete[0] = false;
            } else {
                try {
                    var realTemplates = templates.toRealPath();
                    Files.walkFileTree(realTemplates, new SimpleFileVisitor<>() {
                        private int visited;

                        private FileVisitResult visit() {
                            if (++visited > MAX_ENTRIES) {
                                complete[0] = false;
                                return FileVisitResult.TERMINATE;
                            }
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                            return visit();
                        }

                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            if (visit() == FileVisitResult.TERMINATE) {
                                return FileVisitResult.TERMINATE;
                            }
                            if (attrs.isSymbolicLink() && Files.isDirectory(file)) {
                                complete[0] = false;
                            }
                            var path = realTemplates.relativize(file).toString().replace('\\', '/');
                            if (path.endsWith(suffix)) {
                                var name = path.substring(0, path.length() - suffix.length());
                                var errorTemplate =
                                        name.equals("error") || name.matches("error/(?:[45][0-9]{2}|[45]xx|error)");
                                if (errorTemplate || SYSTEM_PAGE_TEMPLATES.contains(name)) {
                                    addUsage(
                                            usages,
                                            path,
                                            new ThemeTemplateUsage(
                                                    "system", errorTemplate ? "error" : null, null, null));
                                } else {
                                    usages.computeIfAbsent(
                                            path,
                                            ignored -> new ArrayList<>(
                                                    List.of(new ThemeTemplateUsage("other", null, null, null))));
                                }
                            }
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFileFailed(Path file, IOException exc) {
                            complete[0] = false;
                            return visit();
                        }
                    });
                } catch (IOException e) {
                    complete[0] = false;
                }
            }
        }
        var files = usages.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new ThemeTemplateFile(
                        entry.getKey(), fileState(templates, realThemePath, entry.getKey()), entry.getValue()))
                .toList();
        var layoutState = fileState(templates, realThemePath, "layout.html");
        Theme.ThemeStatus.PageLayout layout;
        if (layoutState.equals("available") || layoutState.equals("missing")) {
            layout = layoutChecker.check(realThemePath);
        } else {
            layout = new Theme.ThemeStatus.PageLayout();
            layout.setState(Theme.PageLayoutState.INVALID);
            layout.setTemplate(PageLayoutContract.TEMPLATE_FILE);
            layout.setReason("UnreadableLayoutTemplate");
            layout.setMessage("The layout template is not readable inside the theme templates directory.");
        }
        return new TemplateInventory(files, complete[0], layout);
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
                // Preserve invalid declarations so administrators can see the problem.
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

    private static String fileState(Path templates, Path themePath, String file) {
        try {
            var path = templates.resolve(file).normalize();
            if (!path.startsWith(templates) || Path.of(file).isAbsolute()) {
                return "invalid";
            }
            if (!Files.exists(templates, LinkOption.NOFOLLOW_LINKS)) {
                return "missing";
            }
            var realTemplates = templates.toRealPath();
            if (!realTemplates.startsWith(themePath)) {
                return "invalid";
            }
            if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
                return "missing";
            }
            if (!path.toRealPath().startsWith(realTemplates)) {
                return "invalid";
            }
            return Files.isRegularFile(path) && Files.isReadable(path) ? "available" : "unreadable";
        } catch (InvalidPathException e) {
            return "invalid";
        } catch (IOException e) {
            return "unreadable";
        }
    }

    record TemplateInventory(
            List<ThemeTemplateFile> templates, boolean complete, Theme.ThemeStatus.PageLayout pageLayout) {}
}
