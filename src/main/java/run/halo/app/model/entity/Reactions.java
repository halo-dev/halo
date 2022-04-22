package run.halo.app.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * The Java class relevant to the Json returned by github api.  
 *
 * @author Chen_Kunqiu
 */
@Data
public class Reactions {
    private String url;
    @JsonProperty("total_count")
    private int totalCount;
    @JsonProperty("+1")
    private int plusOne;
    @JsonProperty("-1")
    private int minusOne;
    private int laugh;
    private int hooray;
    private int confused;
    private int heart;
    private int rocket;
    private int eyes;
}