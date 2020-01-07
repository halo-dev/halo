package run.halo.app.model.dto;

import lombok.Data;
import run.halo.app.model.enums.Mode;

/**
 * Theme controller.
 *
 * @author ryanwang
 * @date 2019/5/4
 */
@Data
public class EnvironmentDTO {

    private String database;

    private Long startTime;

    private String version;

    private Mode mode;
}
