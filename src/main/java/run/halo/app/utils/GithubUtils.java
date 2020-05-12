package run.halo.app.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.*;
import run.halo.app.service.ThemeService;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class GithubUtils {

    static final String PREFIX = "https://github.com/";

    public static Map<String, Object> getLatestRelease(String uri) {
        String repoUrl = StringUtils.removeStartIgnoreCase(uri, PREFIX);

        try {
            GithubRelease githubRelease = new GithubRelease(repoUrl);

            Thread thread = new Thread(githubRelease);

            thread.start();

            thread.join(10 * 1000);

            return githubRelease.result;
        } catch (InterruptedException e) {
        }

        return null;
    }

    public static String accessThemeProperty(String uri, String branch) {
        String repoUrl = StringUtils.removeStartIgnoreCase(uri, PREFIX);

        try {
            GithubFile githubFile = new GithubFile(repoUrl, branch);

            Thread thread = new Thread(githubFile);
            
            thread.start();

            thread.join(10 * 1000);

            return githubFile.result;
        } catch (InterruptedException e) {
        }

        return null;
    }

    private static class GithubRelease implements Runnable {

        /**
         * The return result is zip url and tag name etc.
         */
        private HashMap<String, Object> result;

        /**
         * should be in format of "username/reponame"
         */
        private String repoUrl;

        public GithubRelease(String repoUrl) {
            this.repoUrl = repoUrl;
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
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    private static class GithubFile implements Runnable {

        /**
         * result is file content
         */
        private String result;

        /**
         * should be in format of "username/reponame"
         */
        private String repoUrl;

        /**
         * the branch name
         */
        private String branch;

        public GithubFile(String repoUrl, String branch) {
            this.repoUrl = repoUrl;
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
