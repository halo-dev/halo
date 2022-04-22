package run.halo.app.model.dto;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import run.halo.app.model.entity.Assets;
import run.halo.app.model.entity.GithubApiVersionJson;

/**
 * Version information of a release.
 *
 * <p>This is a simplified representation of
 * {@linkplain run.halo.app.model.entity.GithubApiVersionJson}.
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
     * @param json the json data given by github api
     * @return the simplified VO object
     */
    public static VersionInfoDTO convertFrom(GithubApiVersionJson json) {
        final Assets asset = json.getAssets().get(0);
        return VersionInfoDTO.builder().version(json.getTagName()).desc(json.getBody())
            .githubUrl(json.getHtmlUrl()).jarName(asset.getName()).size(asset.getSize())
            .downloadUrl(asset.getBrowserDownloadUrl()).build();
    }
}
