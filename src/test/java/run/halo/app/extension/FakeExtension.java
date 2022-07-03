package run.halo.app.extension;

@GVK(group = "fake.halo.run",
    version = "v1alpha1",
    kind = "Fake",
    plural = "fakes",
    singular = "fake")
public class FakeExtension extends AbstractExtension {

    public static FakeExtension createFake(String name) {
        var metadata = new Metadata();
        metadata.setName(name);
        var fake = new FakeExtension();
        fake.setMetadata(metadata);
        return fake;
    }

}
