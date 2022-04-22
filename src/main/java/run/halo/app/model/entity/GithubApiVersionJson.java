package run.halo.app.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.List;
import lombok.Data;
import lombok.ToString;

/**
 * The Java class relevant to the Json returned by github api.
 *
 * <p>The json structure refers to the response of
 * <a href="https://api.github.com/repos/halo-dev/halo/releases/latest">
 *     https://api.github.com/repos/halo-dev/halo/releases/latest
 * </a>.
 *
 * @author Chen_Kunqiu
 */
@Data
@ToString
public class GithubApiVersionJson {
    private String url;
    @JsonProperty("assets_url")
    private String assetsUrl;
    @JsonProperty("upload_url")
    private String uploadUrl;
    @JsonProperty("html_url")
    private String htmlUrl;
    private int id;
    private Author author;
    @JsonProperty("node_id")
    private String nodeId;
    @JsonProperty("tag_name")
    private String tagName;
    @JsonProperty("target_commitish")
    private String targetCommitish;
    private String name;
    private boolean draft;
    private boolean prerelease;
    @JsonProperty("created_at")
    private Date createdAt;
    @JsonProperty("published_at")
    private Date publishedAt;
    private List<Assets> assets;
    @JsonProperty("tarball_url")
    private String tarballUrl;
    @JsonProperty("zipball_url")
    private String zipballUrl;
    private String body;
    private Reactions reactions;
    @JsonProperty("mentions_count")
    private int mentionsCount;
}