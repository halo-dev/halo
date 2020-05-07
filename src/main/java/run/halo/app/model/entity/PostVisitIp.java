package run.halo.app.model.entity;

import lombok.EqualsAndHashCode;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Post Visit ip address.
 *
 * @author KristenLawrence
 * @date 2020-05-07
 */
@Entity(name = "PostVisitIp")
@DiscriminatorValue("0")
@EqualsAndHashCode(callSuper = true)
public class PostVisitIp extends BaseVisitIp {

}
