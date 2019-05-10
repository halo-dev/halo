package run.halo.app.model.dto;

import run.halo.app.model.dto.base.OutputConverter;
import run.halo.app.model.entity.Photo;
import lombok.Data;

import java.util.Date;

/**
 * @author ryanwang
 * @date : 2019/3/21
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
}
