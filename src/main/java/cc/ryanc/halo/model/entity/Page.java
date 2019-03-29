package cc.ryanc.halo.model.entity;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Page entity.
 *
 * @author johnniang
 * @date 3/22/19
 */
@Entity(name = "Page")
@Where(clause = "deleted = false")
@SQLDelete(sql = "update posts set deleted = true where id = ?")
@DiscriminatorValue("1")
public class Page extends BasePost {
}
