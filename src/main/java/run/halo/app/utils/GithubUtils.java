package run.halo.app.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.*;
import run.halo.app.service.ThemeService;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * GithubUtils send request to api.github.com
 *
 * @author bigbang019
 * @date 2020-05-31
 */
@Slf4j
public class GithubUtils {

    /**
     * The prefix need to remove
     */
    static final String PREFIX = "https://github.com/";

    /**
     * Get latest release
     *
     * @param uri repository url must not be null
     * @return the map object containning tagname and zipfile url
     */
    public static Map<String, Object> getLatestRelease(String uri) {
        String repoUrl = StringUtils.removeStartIgnoreCase(uri, PREFIX);

        try {
            GithubLatestRelease githubLatestRelease = new GithubLatestRelease(repoUrl);

            Thread thread = new Thread(githubLatestRelease);

            thread.start();

            thread.join(10 * 1000);

            return githubLatestRelease.result;
        } catch (InterruptedException e) {
            log.warn("Interrupted", e);
        }

        return null;
    }

    /**
     * Get release information
     *
     * @param uri repository url must not be null
     * @return list of tagname of releases
     */
    public static List<String> getReleases(String uri) {
        String repoUrl = StringUtils.removeStartIgnoreCase(uri, PREFIX);

        try {
            GithubReleases githubReleases = new GithubReleases(repoUrl);

            Thread thread = new Thread(githubReleases);

            thread.start();

            thread.join(10 * 1000);

            return githubReleases.result;
        } catch (InterruptedException e) {
            log.warn("Getting releases from github interrupted", e);
        }

        return null;
    }

    /**
     * Get release information
     *
     * @param uri     repository url must not be null
     * @param tagName tag must not be null
     * @return the map object containning tagname and zipfile url
     */
    public static Map<String, Object> getRelease(String uri, String tagName) {
        String repoUrl = StringUtils.removeStartIgnoreCase(uri, PREFIX);

        try {
            GithubRelease githubRelease = new GithubRelease(repoUrl, tagName);

            Thread thread = new Thread(githubRelease);

            thread.start();

            thread.join(10 * 1000);

            return githubRelease.result;
        } catch (InterruptedException e) {
            log.warn("Interrupted", e);
        }
        return null;
    }

    /**
     * Get the content of theme.yaml/theme.yml
     *
     * @param uri    repository url must not be null
     * @param branch branch must not be null
     * @return content of the file
     */
    public static String accessThemeProperty(String uri, String branch) {
        String repoUrl = StringUtils.removeStartIgnoreCase(uri, PREFIX);

        try {
            GithubFile githubFile = new GithubFile(repoUrl, branch);

            Thread thread = new Thread(githubFile);

            thread.start();

            thread.join(10 * 1000);

            return githubFile.result;
        } catch (InterruptedException e) {
            log.warn("Interrupted", e);
        }

        return null;
    }

    private static class GithubRelease implements Runnable {

        /**
         * should be in format of "username/reponame"
         */
        private final String repoUrl;

        /**
         * repository tag name
         */
        private final String tagName;

        /**
         * The return result is zip url and tag name etc.
         */
        private HashMap<String, Object> result;

        public GithubRelease(String repoUrl, String tagName) {
            this.repoUrl = StringUtils.removeEndIgnoreCase(repoUrl, ".git");
            this.tagName = tagName;
            result = null;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    GitHub gitHub = GitHub.connectAnonymously();
                    GHRepository ghRepository = gitHub.getRepository(repoUrl);
                    List<GHRelease> ghReleaseList = ghRepository.getReleases();

                    if (ghReleaseList.size() == 0) {
                        break;
                    }

                    Optional<GHRelease> res = ghReleaseList.stream()
                            .filter(release -> StringUtils.equalsIgnoreCase(release.getTagName(), tagName))
                            .findFirst();

                    if (res.isPresent()) {
                        GHRelease ghRelease = res.get();

                        result = new HashMap<String, Object>() {
                            {
                                put(ThemeService.ZIP_FILE_KEY, ghRelease.getZipballUrl());
                                put(ThemeService.TAG_KEY, ghRelease.getTagName());
                            }
                        };
                    }

                    break;

                } catch (Exception e) {
                    if (e instanceof HttpException) {
                        int code = ((HttpException) e).getResponseCode();
                        if (code != -1) {
                            break;
                        }
                    } else {
                        break;
                    }
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    private static class GithubReleases implements Runnable {

        private final String repoUrl;
        private List<String> result;

        public GithubReleases(String repoUrl) {
            this.repoUrl = StringUtils.removeEndIgnoreCase(repoUrl, ".git");
        }

        @Override
        public void run() {
            while (true) {
                try {
                    GitHub gitHub = GitHub.connectAnonymously();
                    GHRepository ghRepository = gitHub.getRepository(repoUrl);
                    List<GHRelease> ghReleaseList = ghRepository.getReleases();

                    result = new ArrayList<>();

                    for (GHRelease ghRelease : ghReleaseList) {
                        result.add(ghRelease.getTagName());
                    }

                    break;

                } catch (Exception e) {
                    if (log.isErrorEnabled()) {
                        log.error("Failed to react with github.", e);
                    }
                    if (e instanceof HttpException) {
                        int code = ((HttpException) e).getResponseCode();
                        if (code != -1) {
                            break;
                        }
                    } else {
                        break;
                    }
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    private static class GithubLatestRelease implements Runnable {

        /**
         * should be in format of "username/reponame"
         */
        private final String repoUrl;
        /**
         * The return result is zip url and tag name etc.
         */
        private HashMap<String, Object> result;

        public GithubLatestRelease(String repoUrl) {
            this.repoUrl = StringUtils.removeEndIgnoreCase(repoUrl, ".git");
            result = null;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    GitHub gitHub = GitHub.connectAnonymously();
                    GHRepository ghRepository = gitHub.getRepository(repoUrl);
                    List<GHRelease> ghReleaseList = ghRepository.getReleases();

                    if (ghReleaseList.size() == 0) {
                        break;
                    }
                    GHRelease ghRelease = ghReleaseList.get(0);

                    result = new HashMap<String, Object>() {
                        {
                            put(ThemeService.ZIP_FILE_KEY, ghRelease.getZipballUrl());
                            put(ThemeService.TAG_KEY, ghRelease.getTagName());
                        }
                    };

                    break;

                } catch (Exception e) {
                    if (e instanceof HttpException) {
                        int code = ((HttpException) e).getResponseCode();
                        if (code != -1) {
                            break;
                        }
                    } else {
                        break;
                    }
                }

                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    private static class GithubFile implements Runnable {

        /**
         * should be in format of "username/reponame"
         */
        private final String repoUrl;
        /**
         * the branch name
         */
        private final String branch;
        /**
         * result is file content
         */
        private String result;

        public GithubFile(String repoUrl, String branch) {
            this.repoUrl = StringUtils.removeEndIgnoreCase(repoUrl, ".git");
            this.branch = branch;
            result = null;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    GitHub gitHub = GitHub.connectAnonymously();

                    GHRepository ghRepository = gitHub.getRepository(repoUrl);

                    GHContent ghContent = null;

                    for (String themePropertyFile : ThemeService.THEME_PROPERTY_FILE_NAMES) {

                        try {
                            ghContent = ghRepository.getFileContent(themePropertyFile, branch);
                        } catch (FileNotFoundException e) {
                        }
                    }

                    if (ghContent == null) {
                        break;
                    }

                    result = ghContent.getContent();

                    break;
                } catch (Exception e) {
                    if (e instanceof HttpException) {
                        int code = ((HttpException) e).getResponseCode();
                        if (code != -1) {
                            break;
                        }

                    } else {
                        break;
                    }
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                }
            }
        }
    }
}
