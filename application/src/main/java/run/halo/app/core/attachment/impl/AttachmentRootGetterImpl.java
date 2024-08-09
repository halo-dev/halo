package run.halo.app.core.attachment.impl;

import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import run.halo.app.core.attachment.AttachmentRootGetter;
import run.halo.app.infra.properties.HaloProperties;

@Component
@RequiredArgsConstructor
public class AttachmentRootGetterImpl implements AttachmentRootGetter {
    private final HaloProperties haloProp;

    @Override
    public Path get() {
        return haloProp.getWorkDir().resolve("attachments");
    }
}
