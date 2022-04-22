package run.halo.app.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.annotation.Async;
import run.halo.app.model.dto.VersionInfoDTO;


/**
 * The service to control halo version.
 *
 * @author Chen_Kunqiu
 */
public interface HaloVersionCtrlService {


    /**
     * check whether the halo jar of the specified version exists in local repository.
     *
     * @param tagName the specified version
     * @return exist in local or not
     */
    boolean isInLocal(String tagName);

    /**
     * Get all the release info of halo through github api.
     *
     * @return List of release info organized in json.
     */
    List<VersionInfoDTO> getAllReleasesInfo();

    /**
     * Get specified release info by tagName of the release.
     *
     * @param tagName the tag name of a github release
     * @return single release info
     */
    VersionInfoDTO getReleaseInfoByTag(String tagName);

    /**
     * Get the release info of the latest release.
     *
     * @return the release info
     */
    VersionInfoDTO getLatestReleaseInfo();

    /**
     * Download the specified jar by tagName into local repository.
     *
     * @param tagName the specified tag name of the release
     */
    @Async
    void downloadSpecifiedJarToRepo(String tagName);


    /**
     * Download the latest halo jar into local repository.
     *
     * @return the version of downloaded jar.
     */
    @Async
    CompletableFuture<String> downloadLatestJar();

    /**
     * Switch the version of the running halo app into the specified version.
     *
     * <p>The general mechanism is shown as follows.
     * <ol>
     *      <li>Get the specified halo jar</li>
     *      <ul>
     *          <li>If found in local repository, copy it into work directory.</li>
     *          <li>If not found, download it from github into work directory.</li>
     *      </ul>
     *      <li>Backup the current halo jar into halo-ori-bak.jar.</li>
     *      <li>Construct the same command which launched the current halo jar,
     *          and modify the target halo jar in the command into the new version halo jar.</li>
     *      <li>Register a JVM exit hook which use the constructed command to
     *          launch new version halo jar in subprocess.</li>
     *      <li>Terminate current JVM and trigger relevant hook.</li>
     *      <li>After original halo app exit, delete the original halo jar in few seconds.</li>
     * </ol>
     *
     * @param tagName the version to switch
     */
    @Async
    void switchVersion(String tagName);


    /**
     * Switch version of the running halo app into latest.
     *
     * <p>If specified version not exist in local repository,
     * directly download it into user dir.
     */
    @Async
    void switchLatest();


    /**
     * Similar to {@linkplain #switchVersion switchVersion}, but the halo jar would be downloaded
     * into local repository.
     *
     * @param tagName the version to download and switch
     */
    @Async
    void downloadAndSwitch(String tagName);

    /**
     * Similar to {@linkplain #switchLatest switchLatest}, but the halo jar would be downloaded
     * into local repository.
     */
    @Async
    void downloadAndSwitchLatest();

    /**
     * Get the version of current running halo app.
     *
     * @return the current halo version
     */
    String getCurVersion();


}
