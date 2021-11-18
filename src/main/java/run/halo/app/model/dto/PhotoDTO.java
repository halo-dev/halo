package run.halo.app.model.dto;

import java.util.Date;
import lombok.Data;
import run.halo.app.model.dto.base.OutputConverter;
import run.halo.app.model.entity.Photo;

/**
 * Photo dto.
 *
 * @author ryanwang
 * @author guqing
 * @date 2019-03-21
 */
@Data
public class PhotoDTO implements OutputConverter<PhotoDTO, Photo> {

    private Integer id;

    private String name;

    private String thumbnail;

    private Date takeTime;

    private String url;

    private String team;

    private String location;

    private String description;

    private Long likes;
}
