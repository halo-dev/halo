package run.halo.app.infra.properties;

import java.util.LinkedList;
import java.util.List;
import lombok.Data;

@Data
public class AttachmentProperties {

    private List<ResourceMapping> resourceMappings = new LinkedList<>();

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
