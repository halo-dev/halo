package run.halo.app.content;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import run.halo.app.extension.GroupVersionKind;

public class PermalinkIndexer {
    // 根据 gvk 获取 permalink
    // 根据 permalink 获取 gvk and name
    private MultiValueMap<GroupVersionKind, String> gvkPermalinkMap = new LinkedMultiValueMap<>();
    private Map<String, ExtensionLocator> permalinkMapper = new HashMap<>();

    public ExtensionLocator lookup(String permalink) {
        return permalinkMapper.get(permalink);
    }

    public List<String> getPermalinks(GroupVersionKind gvk) {
        return gvkPermalinkMap.get(gvk);
    }

    public record ExtensionLocator(GroupVersionKind gvk, String name) {

    }
}
