package run.halo.app.model.entity;

import lombok.EqualsAndHashCode;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Sheet Visit ip address.
 *
 * @author KristenLawrence
 * @date 2020-05-07
 */
@Entity(name = "SheetVisitIp")
@DiscriminatorValue("1")
@EqualsAndHashCode(callSuper = true)
public class SheetVisitIp extends BaseVisitIp {

}
