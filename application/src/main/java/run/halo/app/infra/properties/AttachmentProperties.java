package run.halo.app.infra.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.LinkedList;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@Data
public class AttachmentProperties {

    private List<ResourceMapping> resourceMappings = new LinkedList<>();

    @Valid
    @NestedConfigurationProperty
    private final ThumbnailProperties thumbnail = new ThumbnailProperties();

    @Data
    public static class ThumbnailProperties {

        /**
         * Whether to disable thumbnail generation.
         */
        private boolean disabled;

        /**
         * The concurrent threads for thumbnail generation.
         */
        @Min(1)
        private Integer concurrentThreads;

        /**
         * The quality of generated thumbnails, value between 0.0 and 1.0.
         */
        @PositiveOrZero
        @Max(1)
        private Double quality;

    }

    @Data
    public static class ResourceMapping {

        /**
         * Like: {@code /upload/**}.
         */
        private String pathPattern;

        /**
         * The location is a relative path to attachments folder in working directory.
         */
        private List<String> locations;

    }
}
