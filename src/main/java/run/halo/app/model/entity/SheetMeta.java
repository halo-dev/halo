package run.halo.app.model.entity;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * SheetMeta entity.
 *
 * @author ryanwang
 * @author ikaisec
 * @date 2019-08-04
 */
@Entity(name = "SheetMeta")
@DiscriminatorValue("1")
public class SheetMeta extends BaseMeta {
}
