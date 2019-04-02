package cc.ryanc.halo.model.vo;

import cc.ryanc.halo.model.dto.post.PostMinimalOutputDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Comparator;
import java.util.List;

/**
 * Archive vo.
 *
 * @author johnniang
 * @date 4/2/19
 */
@Data
@ToString
@EqualsAndHashCode
public class ArchiveYearVO implements Comparator<ArchiveYearVO> {

    private Integer year;

    private List<PostMinimalOutputDTO> posts;

    @Override
    public int compare(ArchiveYearVO current, ArchiveYearVO other) {
        return current.year - other.year;
    }
}
