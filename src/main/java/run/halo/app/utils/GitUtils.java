package run.halo.app.utils;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.storage.file.WindowCacheConfig;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import run.halo.app.service.ThemeService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Git utilities.
 *
 * @author johnniang
 * @date 19-6-12
 */
@Slf4j
public class GitUtils {

    static final String prefix="https://github.com/";

    static final String contentApiPattern="https://api.github.com/repos/%s/contents/%s?ref=%s";

    static final String releaseApiPattern="https://api.github.com/repos/%s/releases/latest";

    static final String zipFile="tarball_url";

    private GitUtils() {
        // Config packed git MMAP
        WindowCacheConfig config = new WindowCacheConfig();
        config.setPackedGitMMAP(false);
        config.install();
    }

    public static void cloneFromGit(@NonNull String repoUrl, @NonNull Path targetPath) throws GitAPIException {
        Assert.hasText(repoUrl, "Repository remote url must not be blank");
        Assert.notNull(targetPath, "Target path must not be null");

        log.debug("Trying to clone git repo [{}] to [{}]", repoUrl, targetPath);

        // Use try-with-resource-statement
        Git git = null;
        try {
            git = Git.cloneRepository()
                .setURI(repoUrl)
                .setDirectory(targetPath.toFile())
                .call();
            log.debug("Cloned git repo [{}] successfully", repoUrl);
        } finally {
            closeQuietly(git);
        }
    }

    public static Git openOrInit(Path repoPath) throws IOException, GitAPIException {
        Git git;

        try {
            git = Git.open(repoPath.toFile());
        } catch (RepositoryNotFoundException e) {
            log.warn("Git repository may not exist, we will try to initialize an empty repository", e);
            git = Git.init().setDirectory(repoPath.toFile()).call();
        }

        return git;
    }

    public static void cloneFromGit(@NonNull String repoUrl, @NonNull Path targetPath, @NonNull String branchName) throws GitAPIException {
        Assert.hasText(repoUrl, "Repository remote url must not be blank");
        Assert.notNull(targetPath, "Target path must not be null");

        Git git = null;
        try{
            git = Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(targetPath.toFile())
                    .setBranchesToClone(Arrays.asList("refs/heads/"+branchName))
                    .setBranch("refs/heads/"+branchName)
                    .call();
        }finally {
            closeQuietly(git);
        }
    }

    public static String getLastestRelease(@NonNull String repoUrl) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        RestTemplate restTemplate=new RestTemplate(new HttpComponentsClientHttpRequestFactory(HttpClientUtils.createHttpsClient(5000)));
        String apiUrl=String.format(releaseApiPattern, StringUtils.removeStartIgnoreCase(repoUrl, prefix));
        ResponseEntity<Map> responseEntity = restTemplate.getForEntity(apiUrl, Map.class);
        Map<String, Object> map=(Map<String, Object>)responseEntity.getBody();
        return (String) map.get(zipFile);
    }

    public static List<String> getAllBranches(@NonNull String repoUrl) {
        List<String> branches=new ArrayList<String>();
        try{
            Collection<Ref> refs = Git.lsRemoteRepository()
                    .setHeads(true)
                    .setRemote(repoUrl)
                    .call();
            for (Ref ref : refs){
                branches.add(ref.getName().substring(ref.getName().lastIndexOf("/")+1, ref.getName().length()));
            }
        } catch (InvalidRemoteException e) {
            log.warn("Git url is not valid");
        } catch (TransportException e) {
            log.warn("Transport exception");
        } catch (GitAPIException e) {
            log.warn("Git api exception");
        }
        return branches;
    }

    public static String accessThemeProperty(@NonNull String repoUrl) throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        return accessThemeProperty(repoUrl, "master");
    }

    public static String accessThemeProperty(@NonNull String repoUrl, @NonNull String branchName) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, IOException {
        RestTemplate restTemplate=new RestTemplate(new HttpComponentsClientHttpRequestFactory(HttpClientUtils.createHttpsClient(5000)));
        for(String propertyPathName: ThemeService.THEME_PROPERTY_FILE_NAMES){
            String apiUrl=String.format(contentApiPattern,StringUtils.removeStartIgnoreCase(repoUrl,prefix),propertyPathName, branchName);
            ResponseEntity<Map> responseEntity = restTemplate.getForEntity(apiUrl, Map.class);
            if (responseEntity.getStatusCode()== HttpStatus.OK){
                Map<String,Object> map=(Map<String, Object>)responseEntity.getBody();
                String encodedContent=(String) map.get("content");
                Base64.Decoder decoder=Base64.getDecoder();
                return new String(decoder.decode(encodedContent.replaceAll("\r|\n","")),StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    public static void closeQuietly(Git git) {
        if (git != null) {
            git.getRepository().close();
            git.close();
        }
    }

}
