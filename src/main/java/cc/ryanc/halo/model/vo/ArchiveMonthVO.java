package cc.ryanc.halo.model.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Archive vo.
 *
 * @author johnniang
 * @date 4/2/19
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ArchiveMonthVO extends ArchiveYearVO {

    private Integer month;

    @Override
    public int compare(ArchiveYearVO current, ArchiveYearVO other) {
        int compare = super.compare(current, other);

        if (compare != 0) {
            return compare;
        }

        return ((ArchiveMonthVO) current).month - ((ArchiveMonthVO) other).month;
    }
}
