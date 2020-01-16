package run.halo.app.model.entity;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Page entity.
 *
 * @author johnniang
 * @date 3/22/19
 */
@Entity(name = "Sheet")
@DiscriminatorValue("1")
public class Sheet extends BasePost {

}
