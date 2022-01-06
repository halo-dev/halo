package run.halo.app.model.entity;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Sheet content entity.
 *
 * @author guqing
 * @date 2022-01-07
 */
@Entity(name = "SheetContent")
@DiscriminatorValue("1")
public class SheetContent extends BaseContent{

}
