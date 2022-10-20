package run.halo.app.infra.utils;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.config.YamlProcessor;
import org.springframework.core.io.Resource;
import run.halo.app.extension.Unstructured;

/**
 * <p>Process the content in yaml that matches the {@link DocumentMatcher} and convert it to an
 * unstructured list.</p>
 * <p>Multiple resources can be processed at one time.</p>
 * <p>The following specified key must be included before the resource can be processed:
 * <pre>
 *     apiVersion
 *     kind
 *     metadata.name
 * </pre>
 * Otherwise, skip it and continue to read the next resource.
 * </p>
 *
 * @author guqing
 * @since 2.0.0
 */
public class YamlUnstructuredLoader extends YamlProcessor {

    private static final DocumentMatcher DEFAULT_UNSTRUCTURED_MATCHER = properties -> {
        if (properties.containsKey("apiVersion")
            && properties.containsKey("kind")
            && (properties.containsKey("metadata.name")
            || properties.containsKey("metadata.generateName"))) {
            return YamlProcessor.MatchStatus.FOUND;
        }
        return MatchStatus.NOT_FOUND;
    };

    public YamlUnstructuredLoader(Resource... resources) {
        setResources(resources);
        setDocumentMatchers(DEFAULT_UNSTRUCTURED_MATCHER);
    }

    public List<Unstructured> load() {
        List<Unstructured> unstructuredList = new ArrayList<>();
        process((properties, map) -> {
            Unstructured unstructured = JsonUtils.mapToObject(map, Unstructured.class);
            unstructuredList.add(unstructured);
        });
        return unstructuredList;
    }
}
