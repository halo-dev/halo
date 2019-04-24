package run.halo.app.model.vo;

import run.halo.app.model.dto.post.PostMinimalDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Comparator;
import java.util.List;

/**
 * Year archive vo.
 *
 * @author johnniang
 * @date 4/2/19
 */
@Data
@ToString
@EqualsAndHashCode
public class ArchiveYearVO {

    private Integer year;

    private List<PostMinimalDTO> posts;

    public static class ArchiveComparator implements Comparator<ArchiveYearVO> {

        @Override
        public int compare(ArchiveYearVO left, ArchiveYearVO right) {
            return right.getYear() - left.getYear();
        }
    }
}
