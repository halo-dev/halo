package run.halo.app.model.entity;

import lombok.EqualsAndHashCode;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * PostMeta entity.
 *
 * @author ryanwang
 * @author ikaisec
 * @date 2019-08-04
 */
@Entity(name = "PostMeta")
@DiscriminatorValue("0")
@EqualsAndHashCode(callSuper = true)
public class PostMeta extends BaseMeta {
}
