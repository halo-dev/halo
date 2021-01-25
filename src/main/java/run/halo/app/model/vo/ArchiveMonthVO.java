package run.halo.app.model.vo;

import java.util.Comparator;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Month archive vo.
 *
 * @author johnniang
 * @date 4/2/19
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ArchiveMonthVO extends ArchiveYearVO {

    private Integer month;

    public static class ArchiveComparator implements Comparator<ArchiveMonthVO> {

        @Override
        public int compare(ArchiveMonthVO left, ArchiveMonthVO right) {
            int compare = right.getYear() - left.getYear();

            if (compare != 0) {
                return compare;
            }

            return right.getMonth() - left.getMonth();
        }
    }
}
