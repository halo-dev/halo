package run.halo.app.model.dto;

import java.io.IOException;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.kohsuke.github.GHAsset;
import org.kohsuke.github.GHRelease;
import run.halo.app.exception.ServiceException;

/**
 * Version information of a release.
 *
 * <p>This is a simplified representation of
 * {@linkplain org.kohsuke.github.GHRelease}.
 *
 * @author Chen_Kunqiu
 */
@Data
@ToString
@Builder
public class VersionInfoDTO {
    private String version;
    private String jarName;
    private String desc;
    private String githubUrl;
    private String downloadUrl;
    private Boolean inLocal;
    private Long size;

    /**
     * Initially convert the JSON given by Github into VO.
     *
     * @param release the json data given by github api
     * @return the simplified VO object
     */
    public static VersionInfoDTO convertFrom(GHRelease release) {
        final VersionInfoDTO versionInfoDTO =
            VersionInfoDTO.builder().version(release.getTagName()).desc(release.getBody())
                .githubUrl(release.getHtmlUrl().toString())
                .jarName("halo.jar").build();
        try {
            final GHAsset asset = release.listAssets().iterator().next();
            versionInfoDTO.setJarName(asset.getName());
            versionInfoDTO.setSize(asset.getSize());
            versionInfoDTO.setDownloadUrl(asset.getBrowserDownloadUrl());
        } catch (IOException e) {
            throw new ServiceException("This release has no assert.");
        }
        return versionInfoDTO;
    }
}
