package run.halo.app.model.entity;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * PostComment entity.
 *
 * @author johnniang
 */
@Entity(name = "PostComment")
@DiscriminatorValue("0")
public class PostComment extends BaseComment {

}
