package run.halo.app.extension.index;

import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

@GVK(
    group = "fake.halo.app",
    version = "v1",
    kind = "Fake",
    singular = "fake",
    plural = "fakes"
)
@Data
@EqualsAndHashCode(callSuper = true)
class Fake extends AbstractExtension {

    private Set<String> stringValues = new HashSet<>();

    private String stringValue;

}
