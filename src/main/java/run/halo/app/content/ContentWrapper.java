package run.halo.app.content;

import lombok.Builder;
import lombok.Data;

/**
 * @author guqing
 * @since 2.0.0
 */
@Data
@Builder
public class ContentWrapper {
    private String snapshotName;
    private String raw;
    private String content;
    private String rawType;
}
