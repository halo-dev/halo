package run.halo.app.model.entity;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Journal comment.
 *
 * @author johnniang
 * @date 19-4-25
 */
@Entity(name = "JournalComment")
@DiscriminatorValue("2")
public class JournalComment extends BaseComment {

}
