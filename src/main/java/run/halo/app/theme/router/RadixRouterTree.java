package run.halo.app.theme.router;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.server.PathContainer;
import org.springframework.lang.Nullable;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

/**
 * A router tree implementation based on radix tree.
 *
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
public class RadixRouterTree extends RadixTree<HandlerFunction<ServerResponse>> {

    @Override
    public void insert(String key, HandlerFunction<ServerResponse> value)
        throws IllegalArgumentException {
        super.insert(key, value);
        if (log.isDebugEnabled()) {
            checkIndices();
        }
    }

    @Override
    public boolean delete(String key) {
        boolean result = super.delete(key);
        if (log.isDebugEnabled()) {
            checkIndices();
        }
        return result;
    }

    /**
     * <p>Find the tree node according to the request path.</p>
     * There are the following situations:
     * <ul>
     * <li>If found, return to the {@link HandlerFunction} directly.</li>
     * <li>If the request path is not found in tree, it may be this request path corresponds
     * to a pattern,so all the patterns will be iterated to match the current request path. If
     * they match, the handler will be returned.
     * Otherwise, null will be returned.</li>
     * </ul>
     * for example, there is a tree as follows:
     * <pre>
     * / [indices=tca, priority=4]
     * ├── tags/ [indices=h{, priority=2]
     * │   ├── halo [value=tags-halo, priority=1]*
     * │   └── {slug}/page/{page} [value=post by tags, priority=1]*
     * ├── categories/default [value=categories-default, priority=1]*
     * └── about [value=about, priority=1]*
     * </pre>
     * <p>1. find the request path "/categories/default" in tree, return the handler directly.</p>
     * <p>2. but find the request path "/categories/default/page/1", it will be iterated to
     * match</p>
     * TODO Optimize matching algorithm to improve efficiency and try your best to get results
     *   through one search
     *
     * @param request server request
     * @return a handler function if matched, otherwise null
     */
    public HandlerFunction<ServerResponse> match(ServerRequest request) {
        String path = pathToFind(request);
        HandlerFunction<ServerResponse> result = find(path);
        if (result != null) {
            return result;
        }

        PathContainer pathContainer = PathContainer.parsePath(path);

        List<PathPattern> matches = new ArrayList<>();
        for (String pathPattern : getKeys()) {
            if (!hasPatternSyntax(pathPattern)) {
                continue;
            }
            log.trace("PathPatternParser handle pathPattern [{}]", pathPattern);
            PathPattern parse = PathPatternParser.defaultInstance.parse(pathPattern);
            if (parse.matches(pathContainer)) {
                matches.add(parse);
            }
        }

        if (matches.isEmpty()) {
            return null;
        }
        matches.sort(PathPattern.SPECIFICITY_COMPARATOR);
        PathPattern bestMatch = matches.get(0);
        if (matches.size() > 1) {
            if (log.isTraceEnabled()) {
                log.trace("request [GET {}] matching mappings: [{}]", path, matches);
            }
            PathPattern secondBestMatch = matches.get(1);
            if (PathPattern.SPECIFICITY_COMPARATOR.compare(bestMatch, secondBestMatch) == 0) {
                throw new IllegalStateException(
                    "Ambiguous mapping mapped for '" + path + "': {" + bestMatch + ", "
                        + secondBestMatch + "}");
            }
        }
        PathPattern.PathMatchInfo info =
            bestMatch.matchAndExtract(request.requestPath().pathWithinApplication());
        if (info != null) {
            mergeAttributes(request, info.getUriVariables(), bestMatch);
        }
        return find(bestMatch.getPatternString());
    }

    /**
     * TODO Optimize parameter route matching query.
     * Router 仅匹配请求方法和请求的 URL 路径, 形如 /?p=post-name 是 URL query，而不是 URL 路径的一部分。
     */
    static String pathToFind(ServerRequest request) {
        String requestPath = processRequestPath(request.path());
        MultiValueMap<String, String> queryParams = request.queryParams();
        // 文章的 permalink 规则需要对 p 参数规则特殊处理
        if (requestPath.equals("/") && queryParams.containsKey("p")) {
            // post special route path
            String postSlug = queryParams.getFirst("p");
            requestPath = requestPath + "?p=" + postSlug;
        }
        // /categories/{slug}/page/{page} 和 /tags/{slug}/page/{page} 需要去掉 page 部分
        if (PageUrlUtils.isPageUrl(requestPath)) {
            int i = requestPath.lastIndexOf("/page/");
            if (i != -1) {
                requestPath = requestPath.substring(0, i);
            }
        }
        requestPath = StringUtils.removeEnd(requestPath, "/");
        return StringUtils.prependIfMissing(requestPath, "/");
    }

    private static void mergeAttributes(ServerRequest request, Map<String, String> variables,
        PathPattern pattern) {
        Map<String, String> pathVariables = mergePathVariables(request.pathVariables(), variables);
        request.attributes().put(RouterFunctions.URI_TEMPLATE_VARIABLES_ATTRIBUTE,
            Collections.unmodifiableMap(pathVariables));

        pattern = mergePatterns(
            (PathPattern) request.attributes().get(RouterFunctions.MATCHING_PATTERN_ATTRIBUTE),
            pattern);
        request.attributes().put(RouterFunctions.MATCHING_PATTERN_ATTRIBUTE, pattern);
    }

    private static PathPattern mergePatterns(@Nullable PathPattern oldPattern,
        PathPattern newPattern) {
        if (oldPattern != null) {
            return oldPattern.combine(newPattern);
        } else {
            return newPattern;
        }
    }

    private static Map<String, String> mergePathVariables(Map<String, String> oldVariables,
        Map<String, String> newVariables) {

        if (!newVariables.isEmpty()) {
            Map<String, String> mergedVariables = new LinkedHashMap<>(oldVariables);
            mergedVariables.putAll(newVariables);
            return mergedVariables;
        } else {
            return oldVariables;
        }
    }

    private static String processRequestPath(String requestPath) {
        String path = StringUtils.prependIfMissing(requestPath, "/");
        return UriUtils.decode(path, StandardCharsets.UTF_8);
    }

    public boolean hasPatternSyntax(String pathPattern) {
        return pathPattern.indexOf('{') != -1 || pathPattern.indexOf(':') != -1
            || pathPattern.indexOf('*') != -1;
    }

    /**
     * Get all keys(paths) in trie, call recursion function
     * Time O(n), Space O(n), n is number of nodes in trie.
     */
    public List<String> getKeys() {
        List<String> res = new ArrayList<>();
        keysHelper(root, res, "");
        return res;
    }

    /**
     * Similar to pre-order (DFS, depth first search) of the tree,
     * recursion is used to traverse all nodes in trie. When visiting the node,
     * the method concatenates characters from previously visited nodes with
     * the character of the current node. When the node's isReal is true,
     * the recursion reaches the last character of the <code>path</code>.
     * Add the <code>path</code> to the result list.
     * recursion function, Time O(n), Space O(n), n is number of nodes in trie
     */
    void keysHelper(RadixTreeNode<HandlerFunction<ServerResponse>> node, List<String> res,
        String prefix) {
        if (node == null) {
            //base condition
            return;
        }

        if (node.isReal()) {
            String path = prefix + node.getKey();
            res.add(path);
        }
        for (RadixTreeNode<HandlerFunction<ServerResponse>> child : node.getChildren()) {
            keysHelper(child, res, prefix + node.getKey());
        }
    }

}
