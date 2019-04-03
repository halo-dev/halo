package cc.ryanc.halo.model.entity;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Page entity.
 *
 * @author johnniang
 * @date 3/22/19
 */
@Entity(name = "Page")
@DiscriminatorValue("1")
public class Page extends BasePost {
}
