package run.halo.app.model.entity;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Journal entity
 *
 * @author johnniang
 * @date 3/22/19
 */
@Entity(name = "Journal")
@DiscriminatorValue("2")
public class Journal extends BasePost {

}
