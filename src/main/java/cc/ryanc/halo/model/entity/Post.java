package cc.ryanc.halo.model.entity;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Post entity.
 *
 * @author johnniang
 */
@Entity(name = "Post")
@SQLDelete(sql = "update posts set deleted = true where id = ?")
@Where(clause = "deleted = false")
@DiscriminatorValue(value = "0")
public class Post extends BasePost {

}
