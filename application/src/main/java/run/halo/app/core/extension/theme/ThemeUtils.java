package run.halo.app.core.extension.theme;

import static org.springframework.util.FileSystemUtils.copyRecursively;
import static run.halo.app.infra.utils.FileUtils.createTempDir;
import static run.halo.app.infra.utils.FileUtils.deleteRecursivelyAndSilently;
import static run.halo.app.infra.utils.FileUtils.unzip;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.BaseStream;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import run.halo.app.core.extension.Theme;
import run.halo.app.extension.Unstructured;
import run.halo.app.infra.exception.ThemeAlreadyExistsException;
import run.halo.app.infra.exception.ThemeInstallationException;
import run.halo.app.infra.utils.FileUtils;
import run.halo.app.infra.utils.YamlUnstructuredLoader;

@Slf4j
class ThemeUtils {
    private static final String THEME_TMP_PREFIX = "halo-theme-";
    private static final String[] THEME_MANIFESTS = {"theme.yaml", "theme.yml"};

    static Flux<Theme> listAllThemesFromThemeDir(Path themesDir) {
        return walkThemesFromPath(themesDir)
            .filter(Files::isDirectory)
            .map(ThemeUtils::findThemeManifest)
            .flatMap(Flux::fromIterable)
            .filter(unstructured -> unstructured.getKind().equals(Theme.KIND))
            .map(unstructured -> Unstructured.OBJECT_MAPPER.convertValue(unstructured,
                Theme.class))
            .sort(Comparator.comparing(theme -> theme.getMetadata().getName()));
    }

    private static Flux<Path> walkThemesFromPath(Path path) {
        return Flux.using(() -> Files.walk(path, 2),
                Flux::fromStream,
                BaseStream::close
            )
            .subscribeOn(Schedulers.boundedElastic());
    }

    private static List<Unstructured> findThemeManifest(Path themePath) {
        List<Resource> resources = new ArrayList<>(4);
        for (String themeResource : THEME_MANIFESTS) {
            Path resourcePath = themePath.resolve(themeResource);
            if (Files.exists(resourcePath)) {
                resources.add(new FileSystemResource(resourcePath));
            }
        }
        if (CollectionUtils.isEmpty(resources)) {
            return List.of();
        }
        return new YamlUnstructuredLoader(resources.toArray(new Resource[0]))
            .load();
    }

    static List<Unstructured> loadThemeResources(Path themePath) {
        try (Stream<Path> paths = Files.list(themePath)) {
            List<FileSystemResource> resources = paths
                .filter(path -> {
                    String pathString = path.toString();
                    return pathString.endsWith(".yaml") || pathString.endsWith(".yml");
                })
                .filter(path -> {
                    String pathString = path.toString();
                    for (String themeManifest : THEME_MANIFESTS) {
                        if (pathString.endsWith(themeManifest)) {
                            return false;
                        }
                    }
                    return true;
                })
                .map(FileSystemResource::new)
                .toList();
            return new YamlUnstructuredLoader(resources.toArray(new Resource[0]))
                .load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static Mono<Unstructured> unzipThemeTo(Publisher<DataBuffer> content, Path themeWorkDir,
        Scheduler scheduler) {
        return unzipThemeTo(content, themeWorkDir, false, scheduler)
            .onErrorMap(e -> !(e instanceof ResponseStatusException), e -> {
                log.error("Failed to unzip theme", e);
                throw new ServerWebInputException("Failed to unzip theme");
            });
    }

    static Mono<Unstructured> unzipThemeTo(Publisher<DataBuffer> content, Path themeWorkDir,
        boolean override, Scheduler scheduler) {
        return Mono.usingWhen(
            createTempDir(THEME_TMP_PREFIX, scheduler),
            tempDir -> {
                var locateThemeManifest = Mono.fromCallable(() -> locateThemeManifest(tempDir)
                    .orElseThrow(() -> new ThemeInstallationException("Missing theme manifest",
                        "problemDetail.theme.install.missingManifest", null)));
                return unzip(content, tempDir, scheduler)
                    .then(locateThemeManifest)
                    .<Unstructured>handle((themeManifestPath, sink) -> {
                        var theme = loadThemeManifest(themeManifestPath);
                        var themeName = theme.getMetadata().getName();
                        var themeTargetPath = themeWorkDir.resolve(themeName);
                        try {
                            if (!override && !FileUtils.isEmpty(themeTargetPath)) {
                                sink.error(new ThemeAlreadyExistsException(themeName));
                                return;
                            }
                            // install theme to theme work dir
                            copyRecursively(themeManifestPath.getParent(), themeTargetPath);
                            sink.next(theme);
                        } catch (IOException e) {
                            deleteRecursivelyAndSilently(themeTargetPath);
                            sink.error(e);
                        }
                    })
                    .subscribeOn(scheduler);
            },
            tempDir -> FileUtils.deleteRecursivelyAndSilently(tempDir, scheduler)
        );
    }

    static Unstructured loadThemeManifest(Path themeManifestPath) {
        var unstructureds = new YamlUnstructuredLoader(new FileSystemResource(themeManifestPath))
            .load();
        if (CollectionUtils.isEmpty(unstructureds)) {
            throw new ThemeInstallationException("Missing theme manifest",
                "problemDetail.theme.install.missingManifest", null);
        }
        return unstructureds.get(0);
    }

    @Nullable
    static Path resolveThemeManifest(Path tempDirectory) {
        for (String themeManifest : THEME_MANIFESTS) {
            Path path = tempDirectory.resolve(themeManifest);
            if (Files.exists(path)) {
                return path;
            }
        }
        return null;
    }

    static Optional<Path> locateThemeManifest(Path path) {
        if (!Files.isDirectory(path)) {
            return Optional.empty();
        }
        var queue = new LinkedList<Path>();
        queue.add(path);
        var manifest = Optional.<Path>empty();
        while (!queue.isEmpty()) {
            var current = queue.pop();
            try (Stream<Path> subPaths = Files.list(current)) {
                manifest = subPaths.filter(Files::isReadable)
                    .filter(subPath -> {
                        if (Files.isDirectory(subPath)) {
                            queue.add(subPath);
                            return false;
                        }
                        return true;
                    })
                    .filter(Files::isRegularFile)
                    .filter(ThemeUtils::isManifest)
                    .findFirst();
            } catch (IOException e) {
                throw Exceptions.propagate(e);
            }
            if (manifest.isPresent()) {
                break;
            }
        }
        return manifest;
    }

    static boolean isManifest(Path file) {
        if (!Files.isRegularFile(file)) {
            return false;
        }
        return Set.of(THEME_MANIFESTS).contains(file.getFileName().toString());
    }

}
