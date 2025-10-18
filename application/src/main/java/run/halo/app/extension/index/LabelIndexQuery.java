package run.halo.app.extension.index;

import java.util.Set;

public interface LabelIndexQuery {

    Set<String> exists(String labelKey);

    Set<String> equal(String labelKey, String labelValue);

    Set<String> notEqual(String labelKey, String labelValue);

    Set<String> in(String labelKey, Set<String> labelValues);

    Set<String> notIn(String labelKey, Set<String> labelValues);

}
