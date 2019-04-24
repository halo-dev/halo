package run.halo.app.model.params;

import lombok.Data;

/**
 * Gallery query params.
 *
 * @author : RYAN0UP
 * @date : 2019/04/25
 */
@Data
public class GalleryQuery {

    /**
     * Keyword.
     */
    private String keyword;

    private String team;
}
