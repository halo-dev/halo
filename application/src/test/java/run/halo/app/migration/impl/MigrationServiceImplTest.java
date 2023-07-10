package run.halo.app.migration.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.server.PathContainer;
import org.springframework.util.AntPathMatcher;

class MigrationServiceImplTest {

    @Mock
    MigrationServiceImpl migrationService;

    @Test
    void pathPredicateTest() {
        var root = Path.of("workspaces/halo-dev/halo");
        var git = root.resolve("sub/.git");
        var relative = root.relativize(git);
        System.out.println(relative);
        var pattern = ".git/";
        pattern = pattern.replace('/', File.separatorChar);
        var pathMatcher = new AntPathMatcher(File.separator);
        var match = pathMatcher.match(pattern, relative.toString());
        System.out.println(match);
    }

    @Test
    void matchAbsolutePath() {
        assertFalse(pathMatch(".git", Path.of(".git").toAbsolutePath()));
        assertTrue(pathMatch(".git", Path.of(".git")));
        assertFalse(pathMatch(".git/", Path.of(".git")));
        assertFalse(pathMatch("**/backups/**", Path.of("backup")));
        assertTrue(pathMatch("**/backups/**", Path.of("backups")));
        assertTrue(pathMatch("**/backups/**", Path.of("backups/halo")));
        assertTrue(pathMatch("**/backups/**", Path.of("halo/backups")));
        assertTrue(pathMatch("**/backups/**", Path.of("halo/backups/halo")));
    }

    boolean pathMatch(String pattern, Path relativePath) {
        if (relativePath.isAbsolute()) {
            return false;
        }
        pattern = pattern.replace('/', File.separatorChar);
        var pathMatcher = new AntPathMatcher(File.separator);
        PathContainer.parsePath("", PathContainer.Options.create(File.separatorChar, false));
        return pathMatcher.match(pattern, relativePath.toString());
    }
}