package run.halo.app.core.extension.theme;

import static java.nio.file.Files.createTempDirectory;
import static org.springframework.util.FileSystemUtils.copyRecursively;
import static run.halo.app.infra.utils.FileUtils.deleteRecursivelyAndSilently;
import static run.halo.app.infra.utils.FileUtils.unzip;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.BaseStream;
import java.util.stream.Stream;
import java.util.zip.ZipInputStream;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.halo.app.core.extension.Theme;
import run.halo.app.extension.Unstructured;
import run.halo.app.infra.exception.ThemeInstallationException;
import run.halo.app.infra.utils.FileUtils;
import run.halo.app.infra.utils.YamlUnstructuredLoader;

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

    static Mono<Unstructured> unzipThemeTo(InputStream inputStream, Path themeWorkDir) {
        return unzipThemeTo(inputStream, themeWorkDir, false);
    }

    static Mono<Unstructured> unzipThemeTo(InputStream inputStream, Path themeWorkDir,
        boolean override) {
        var tempDir = new AtomicReference<Path>();
        return Mono.just(inputStream)
            .publishOn(Schedulers.boundedElastic())
            .doFirst(() -> {
                try {
                    tempDir.set(createTempDirectory(THEME_TMP_PREFIX));
                } catch (IOException e) {
                    throw Exceptions.propagate(e);
                }
            })
            .doOnNext(is -> {
                try (var zipIs = new ZipInputStream(is)) {
                    // unzip input stream into temporary directory
                    unzip(zipIs, tempDir.get());
                } catch (IOException e) {
                    throw Exceptions.propagate(e);
                }
            })
            .flatMap(is -> ThemeUtils.locateThemeManifest(tempDir.get()))
            .switchIfEmpty(
                Mono.error(() -> new ThemeInstallationException("Missing theme manifest",
                    "problemDetail.theme.install.missingManifest", null)))
            .map(themeManifestPath -> {
                var theme = loadThemeManifest(themeManifestPath);
                var themeName = theme.getMetadata().getName();
                var themeTargetPath = themeWorkDir.resolve(themeName);
                try {
                    if (!override && !FileUtils.isEmpty(themeTargetPath)) {
                        throw new ThemeInstallationException("Theme already exists.",
                            "problemDetail.theme.install.alreadyExists", new Object[] {themeName});
                    }
                    // install theme to theme work dir
                    copyRecursively(themeManifestPath.getParent(), themeTargetPath);
                    return theme;
                } catch (IOException e) {
                    deleteRecursivelyAndSilently(themeTargetPath);
                    throw Exceptions.propagate(e);
                }
            })
            .doFinally(signalType -> deleteRecursivelyAndSilently(tempDir.get()));
    }

    static Unstructured loadThemeManifest(Path themeManifestPath) {
        List<Unstructured> unstructureds =
            new YamlUnstructuredLoader(new FileSystemResource(themeManifestPath))
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

    static Mono<Path> locateThemeManifest(Path dir) {
        return Mono.justOrEmpty(dir)
            .filter(Files::isDirectory)
            .publishOn(Schedulers.boundedElastic())
            .mapNotNull(path -> {
                var queue = new LinkedList<Path>();
                queue.add(dir);
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
                return manifest.orElse(null);
            });
    }

    static boolean isManifest(Path file) {
        if (!Files.isRegularFile(file)) {
            return false;
        }
        return Set.of(THEME_MANIFESTS).contains(file.getFileName().toString());
    }

}
