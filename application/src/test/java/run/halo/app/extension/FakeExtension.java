package run.halo.app.extension;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@GVK(group = "fake.halo.run",
    version = "v1alpha1",
    kind = "Fake",
    plural = "fakes",
    singular = "fake")
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class FakeExtension extends AbstractExtension {

    private FakeStatus status = new FakeStatus();

    public static FakeExtension createFake(String name) {
        var metadata = new Metadata();
        metadata.setName(name);
        var fake = new FakeExtension();
        fake.setMetadata(metadata);
        return fake;
    }

    @Data
    public static class FakeStatus {
        private String state;
    }
}
