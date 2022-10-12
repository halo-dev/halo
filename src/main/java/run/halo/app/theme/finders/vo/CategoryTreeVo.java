package run.halo.app.theme.finders.vo;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.util.Assert;
import run.halo.app.core.extension.Category;
import run.halo.app.extension.MetadataOperator;

/**
 * A tree vo for {@link Category}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
@Builder
@ToString
@EqualsAndHashCode
public class CategoryTreeVo {

    private MetadataOperator metadata;

    private Category.CategorySpec spec;

    private Category.CategoryStatus status;

    private List<CategoryTreeVo> children;

    private String parentName;

    private Integer postCount;

    /**
     * Convert {@link CategoryVo} to {@link CategoryTreeVo}.
     *
     * @param category category value object
     * @return category tree value object
     */
    public static CategoryTreeVo from(CategoryVo category) {
        Assert.notNull(category, "The category must not be null");
        return CategoryTreeVo.builder()
            .metadata(category.getMetadata())
            .spec(category.getSpec())
            .status(category.getStatus())
            .children(List.of())
            .postCount(Objects.requireNonNullElse(category.getPostCount(), 0))
            .build();
    }

    public void print(StringBuilder buffer, String prefix, String childrenPrefix) {
        buffer.append(prefix);
        buffer.append(getSpec().getDisplayName())
            .append(" (").append(getPostCount()).append(")");
        buffer.append('\n');
        if (children == null) {
            return;
        }
        for (Iterator<CategoryTreeVo> it = children.iterator(); it.hasNext(); ) {
            CategoryTreeVo next = it.next();
            if (it.hasNext()) {
                next.print(buffer, childrenPrefix + "├── ", childrenPrefix + "│   ");
            } else {
                next.print(buffer, childrenPrefix + "└── ", childrenPrefix + "    ");
            }
        }
    }
}
