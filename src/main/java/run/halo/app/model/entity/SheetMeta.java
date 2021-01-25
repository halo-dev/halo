package run.halo.app.model.entity;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import lombok.EqualsAndHashCode;

/**
 * SheetMeta entity.
 *
 * @author ryanwang
 * @author ikaisec
 * @date 2019-08-04
 */
@Entity(name = "SheetMeta")
@DiscriminatorValue("1")
@EqualsAndHashCode(callSuper = true)
public class SheetMeta extends BaseMeta {
}
