package run.halo.app.model.params;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import run.halo.app.model.dto.base.InputConverter;
import run.halo.app.model.entity.Sheet;
import run.halo.app.model.entity.SheetMeta;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.utils.SlugUtils;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Sheet param.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-4-24
 */
@Data
public class SheetParam implements InputConverter<Sheet> {

    @NotBlank(message = "页面标题不能为空")
    @Size(max = 100, message = "页面标题的字符长度不能超过 {max}")
    private String title;

    private PostStatus status = PostStatus.DRAFT;

    private String url;

    private String originalContent;

    private String summary;

    @Size(max = 255, message = "页面缩略图链接的字符长度不能超过 {max}")
    private String thumbnail;

    private Boolean disallowComment = false;

    private Date createTime;

    @Size(max = 255, message = "页面密码的字符长度不能超过 {max}")
    private String password;

    @Size(max = 255, message = "Length of template must not be more than {max}")
    private String template;

    @Min(value = 0, message = "Post top priority must not be less than {value}")
    private Integer topPriority = 0;

    private Set<SheetMetaParam> sheetMetas;

    @Override
    public Sheet convertTo() {
        url = StringUtils.isBlank(url) ? SlugUtils.slug(title) : SlugUtils.slug(url);

        if (null == thumbnail) {
            thumbnail = "";
        }

        return InputConverter.super.convertTo();
    }

    @Override
    public void update(Sheet sheet) {
        url = StringUtils.isBlank(url) ? SlugUtils.slug(title) : SlugUtils.slug(url);

        if (null == thumbnail) {
            thumbnail = "";
        }

        InputConverter.super.update(sheet);
    }

    public Set<SheetMeta> getSheetMetas() {
        Set<SheetMeta> sheetMetasSet = new HashSet<>();
        if (CollectionUtils.isEmpty(sheetMetas)) {
            return sheetMetasSet;
        }

        for (SheetMetaParam sheetMetaParam : sheetMetas) {
            SheetMeta sheetMeta = sheetMetaParam.convertTo();
            sheetMetasSet.add(sheetMeta);
        }
        return sheetMetasSet;
    }
}
