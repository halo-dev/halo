package run.halo.app.model.entity;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Post content entity.
 *
 * @author guqing
 * @date 2022-01-07
 */
@Entity(name = "PostContent")
@DiscriminatorValue("0")
public class PostContent extends BaseContent {

}
