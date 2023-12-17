package run.halo.app.extension.router;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collections;
import java.util.List;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.core.convert.ConversionService;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

public interface IListRequest {

    @Schema(description = "The page number. Zero indicates no page.")
    Integer getPage();

    @Schema(description = "Size of one page. Zero indicates no limit.")
    Integer getSize();

    @Schema(description = "Label selector for filtering.")
    List<String> getLabelSelector();

    @Schema(description = "Field selector for filtering.")
    List<String> getFieldSelector();

    class QueryListRequest implements IListRequest {

        protected final MultiValueMap<String, String> queryParams;

        private final ConversionService conversionService =
            ApplicationConversionService.getSharedInstance();

        public QueryListRequest(MultiValueMap<String, String> queryParams) {
            this.queryParams = queryParams;
        }

        @Override
        public Integer getPage() {
            var page = queryParams.getFirst("page");
            if (StringUtils.hasText(page)) {
                return conversionService.convert(page, Integer.class);
            }
            return 0;
        }

        @Override
        public Integer getSize() {
            var size = queryParams.getFirst("size");
            if (StringUtils.hasText(size)) {
                return conversionService.convert(size, Integer.class);
            }
            return 0;
        }

        @Override
        public List<String> getLabelSelector() {
            return queryParams.getOrDefault("labelSelector", Collections.emptyList());
        }

        @Override
        public List<String> getFieldSelector() {
            return queryParams.getOrDefault("fieldSelector", Collections.emptyList());
        }
    }
}
