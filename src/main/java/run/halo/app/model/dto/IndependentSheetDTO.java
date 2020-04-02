package run.halo.app.model.dto;

import lombok.Data;

/**
 * Theme controller.
 *
 * @author ryanwang
 * @date 2019/5/4
 */
@Data
public class IndependentSheetDTO {

    private Integer id;

    private String title;

    private String fullPath;

    private String routeName;

    private Boolean available;
}
