package run.halo.app.model.support;

import javax.validation.GroupSequence;

/**
 * All check for hibernate validation.
 *
 * @author johnniang
 * @date 19-4-28
 */
@GroupSequence({CreateCheck.class, UpdateCheck.class})
public interface AllCheck {
}
