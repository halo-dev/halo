package run.halo.app.infra.utils;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.springframework.util.FileSystemUtils.deleteRecursively;
import static run.halo.app.infra.utils.DataBufferUtils.toInputStream;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.lang.NonNull;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import run.halo.app.infra.exception.AccessDeniedException;

/**
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
public abstract class FileUtils {

    private FileUtils() {
    }

    public static Mono<Void> unzip(Publisher<DataBuffer> content, @NonNull Path targetPath) {
        return unzip(content, targetPath, Schedulers.boundedElastic());
    }

    public static Mono<Void> unzip(Publisher<DataBuffer> content, @NonNull Path targetPath,
        Scheduler scheduler) {
        return Mono.usingWhen(
            toInputStream(content, scheduler),
            is -> {
                try (var zis = new ZipInputStream(is)) {
                    unzip(zis, targetPath);
                    return Mono.empty();
                } catch (IOException e) {
                    return Mono.error(e);
                }
            },
            is -> Mono.fromRunnable(() -> closeQuietly(is))
        );
    }

    public static void unzip(@NonNull ZipInputStream zis, @NonNull Path targetPath)
        throws IOException {
        // 1. unzip file to folder
        // 2. return the folder path
        Assert.notNull(zis, "Zip input stream must not be null");
        Assert.notNull(targetPath, "Target path must not be null");

        // Create path if absent
        createIfAbsent(targetPath);

        // Folder must be empty
        ensureEmpty(targetPath);

        ZipEntry zipEntry = zis.getNextEntry();

        while (zipEntry != null) {
            // Resolve the entry path
            Path entryPath = targetPath.resolve(zipEntry.getName());

            checkDirectoryTraversal(targetPath, entryPath);

            if (Files.notExists(entryPath.getParent())) {
                Files.createDirectories(entryPath.getParent());
            }

            if (zipEntry.isDirectory()) {
                // Create directory
                Files.createDirectory(entryPath);
            } else {
                // Copy file
                Files.copy(zis, entryPath);
            }

            zipEntry = zis.getNextEntry();
        }
    }

    public static void zip(Path sourcePath, Path targetPath) throws IOException {
        try (var zos = new ZipOutputStream(Files.newOutputStream(targetPath))) {
            Files.walkFileTree(sourcePath, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                    checkDirectoryTraversal(sourcePath, file);
                    var relativePath = sourcePath.relativize(file);
                    var entry = new ZipEntry(relativePath.toString());
                    zos.putNextEntry(entry);
                    Files.copy(file, zos);
                    zos.closeEntry();
                    return super.visitFile(file, attrs);
                }
            });
        }
    }

    public static void jar(Path sourcePath, Path targetPath) throws IOException {
        try (var jos = new JarOutputStream(Files.newOutputStream(targetPath))) {
            Files.walkFileTree(sourcePath, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                    checkDirectoryTraversal(sourcePath, file);
                    var relativePath = sourcePath.relativize(file);
                    var entry = new JarEntry(relativePath.toString());
                    jos.putNextEntry(entry);
                    Files.copy(file, jos);
                    jos.closeEntry();
                    return super.visitFile(file, attrs);
                }
            });
        }
    }

    /**
     * Creates directories if absent.
     *
     * @param path path must not be null
     * @throws IOException io exception
     */
    public static void createIfAbsent(@NonNull Path path) throws IOException {
        Assert.notNull(path, "Path must not be null");

        if (Files.notExists(path)) {
            // Create directories
            Files.createDirectories(path);

            log.debug("Created directory: [{}]", path);
        }
    }

    /**
     * The given path must be empty.
     *
     * @param path path must not be null
     * @throws IOException io exception
     */
    public static void ensureEmpty(@NonNull Path path) throws IOException {
        if (!isEmpty(path)) {
            throw new DirectoryNotEmptyException("Target directory: " + path + " was not empty");
        }
    }

    /**
     * Checks if the given path is empty.
     *
     * @param path path must not be null
     * @return true if the given path is empty; false otherwise
     * @throws IOException io exception
     */
    public static boolean isEmpty(@NonNull Path path) throws IOException {
        Assert.notNull(path, "Path must not be null");

        if (!Files.isDirectory(path) || Files.notExists(path)) {
            return true;
        }

        try (Stream<Path> pathStream = Files.list(path)) {
            return pathStream.findAny().isEmpty();
        }
    }

    public static void closeQuietly(final Closeable closeable) {
        closeQuietly(closeable, null);
    }

    /**
     * Closes the given {@link Closeable} as a null-safe operation while consuming IOException by
     * the given {@code consumer}.
     *
     * @param closeable The resource to close, may be null.
     * @param consumer  Consumes the IOException thrown by {@link Closeable#close()}.
     */
    public static void closeQuietly(final Closeable closeable,
        final Consumer<IOException> consumer) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                if (consumer != null) {
                    consumer.accept(e);
                }
            }
        }
    }

    /**
     * Checks directory traversal vulnerability.
     *
     * @param parentPath  parent path must not be null.
     * @param pathToCheck path to check must not be null
     */
    public static void checkDirectoryTraversal(@NonNull Path parentPath,
        @NonNull Path pathToCheck) {
        Assert.notNull(parentPath, "Parent path must not be null");
        Assert.notNull(pathToCheck, "Path to check must not be null");

        if (pathToCheck.normalize().startsWith(parentPath)) {
            return;
        }

        throw new AccessDeniedException("Directory traversal detected: " + pathToCheck,
            "problemDetail.directoryTraversal", new Object[] {parentPath, pathToCheck});
    }

    /**
     * Checks directory traversal vulnerability.
     *
     * @param parentPath  parent path must not be null.
     * @param pathToCheck path to check must not be null
     */
    public static void checkDirectoryTraversal(@NonNull String parentPath,
        @NonNull String pathToCheck) {
        checkDirectoryTraversal(Paths.get(parentPath), Paths.get(pathToCheck));
    }

    /**
     * Checks directory traversal vulnerability.
     *
     * @param parentPath  parent path must not be null.
     * @param pathToCheck path to check must not be null
     */
    public static void checkDirectoryTraversal(@NonNull Path parentPath,
        @NonNull String pathToCheck) {
        checkDirectoryTraversal(parentPath, Paths.get(pathToCheck));
    }

    /**
     * Delete folder recursively without exception throwing.
     *
     * @param root the root File to delete
     */
    public static void deleteRecursivelyAndSilently(Path root) {
        try {
            var deleted = deleteRecursively(root);
            if (log.isDebugEnabled()) {
                log.debug("Delete {} result: {}", root, deleted);
            }
        } catch (IOException ignored) {
            // Ignore this error
        }
    }

    public static Mono<Boolean> deleteRecursivelyAndSilently(Path root, Scheduler scheduler) {
        return Mono.fromSupplier(() -> {
            try {
                return deleteRecursively(root);
            } catch (IOException ignored) {
                return false;
            }
        }).subscribeOn(scheduler);
    }


    public static Mono<Boolean> deleteFileSilently(Path file) {
        return deleteFileSilently(file, Schedulers.boundedElastic());
    }

    public static Mono<Boolean> deleteFileSilently(Path file, Scheduler scheduler) {
        return Mono.fromSupplier(
                () -> {
                    if (file == null || !Files.isRegularFile(file)) {
                        return false;
                    }
                    try {
                        return Files.deleteIfExists(file);
                    } catch (IOException ignored) {
                        return false;
                    }
                })
            .subscribeOn(scheduler);
    }

    public static void copy(Path source, Path dest, CopyOption... options) {
        try {
            Files.copy(source, dest, options);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void copyRecursively(Path src, Path target, Set<String> excludes)
        throws IOException {
        var pathMatcher = new AntPathMatcher();
        Predicate<Path> shouldExclude = path -> excludes.stream()
            .anyMatch(pattern -> pathMatcher.match(pattern, path.toString()));
        Files.walkFileTree(src, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                throws IOException {
                if (!shouldExclude.test(src.relativize(file))) {
                    Files.copy(file, target.resolve(src.relativize(file)), REPLACE_EXISTING);
                }
                return super.visitFile(file, attrs);
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException {
                if (shouldExclude.test(src.relativize(dir))) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                Files.createDirectories(target.resolve(src.relativize(dir)));
                return super.preVisitDirectory(dir, attrs);
            }
        });
    }

    public static Mono<Path> createTempDir(String prefix, Scheduler scheduler) {
        return Mono.fromCallable(() -> Files.createTempDirectory(prefix)).subscribeOn(scheduler);
    }
}
