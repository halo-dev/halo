package run.halo.app.model.dto;

import lombok.Data;

/**
 * Theme controller.
 *
 * @author ryanwang
 * @date 2019/5/4
 */
@Data
public class InternalSheetDTO {

    private Integer id;

    private String title;

    private String url;

    private boolean status;
}
