package run.halo.app.theme.router;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.server.PathContainer;
import org.springframework.web.reactive.function.server.HandlerFunction;
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
        // uri decode key to insert
        String decodedKey = UriUtils.decode(key, StandardCharsets.UTF_8);
        super.insert(decodedKey, value);
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
     * @param requestPath request path
     * @return a handler function if matched, otherwise null
     */
    public HandlerFunction<ServerResponse> match(String requestPath) {
        String path = processRequestPath(requestPath);
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

        return find(bestMatch.getPatternString());
    }

    private String processRequestPath(String requestPath) {
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
