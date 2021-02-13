package run.halo.app.model.enums;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.convert.ConversionService;
import org.springframework.test.context.ActiveProfiles;

/**
 * Attach origin test.
 *
 * @author johnniang
 */
@SpringBootTest
@ActiveProfiles("test")
class AttachmentTypeTest {

    @Autowired
    ConversionService conversionService;

    @Test
    void conversionTest() {
        Assertions.assertEquals(AttachmentType.LOCAL,
            conversionService.convert("LOCAL", AttachmentType.class));
        Assertions.assertEquals(AttachmentType.LOCAL,
            conversionService.convert("local", AttachmentType.class));
        Assertions.assertEquals(AttachmentType.LOCAL,
            conversionService.convert("Local", AttachmentType.class));
        Assertions.assertEquals(AttachmentType.LOCAL,
            conversionService.convert("LoCal", AttachmentType.class));
    }
}
