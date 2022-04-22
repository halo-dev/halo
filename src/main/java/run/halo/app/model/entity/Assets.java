
package run.halo.app.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import lombok.Data;

/**
 * The Java class relevant to the Json returned by github api.
 *
 * @author Chen_Kunqiu
 */
@Data
public class Assets {
    private String url;
    private int id;
    @JsonProperty("node_id")
    private String nodeId;
    private String name;
    private String label;
    private Uploader uploader;
    @JsonProperty("content_type")
    private String contentType;
    private String state;
    private long size;
    @JsonProperty("download_count")
    private int downloadCount;
    @JsonProperty("created_at")
    private Date createdAt;
    @JsonProperty("updated_at")
    private Date updatedAt;
    @JsonProperty("browser_download_url")
    private String browserDownloadUrl;
}