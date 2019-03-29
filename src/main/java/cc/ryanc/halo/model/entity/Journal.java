package cc.ryanc.halo.model.entity;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Journal entity
 *
 * @author johnniang
 * @date 3/22/19
 */
@Entity(name = "Journal")
@Where(clause = "deleted = false")
@SQLDelete(sql = "update posts set deleted = true where id = ?")
@DiscriminatorValue("2")
public class Journal extends BasePost {

}
