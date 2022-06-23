package run.halo.app.plugin.resources;

import org.springframework.stereotype.Component;
import run.halo.app.core.extension.ReverseProxy;

/**
 * TODO Optimize code to support user customize js bundle rules.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class JsBundleRuleProvider {

    /**
     * Gets plugin js bundle rule.
     *
     * @param pluginName plugin name
     * @return a js bundle rule
     */
    public ReverseProxy.ReverseProxyRule jsRule(String pluginName) {
        ReverseProxy.FileReverseProxyProvider
            file = new ReverseProxy.FileReverseProxyProvider("admin", "main.js");
        return new ReverseProxy.ReverseProxyRule("/admin/main.js", file);
    }

    /**
     * Gets plugin stylesheet rule.
     *
     * @param pluginName plugin name
     * @return a stylesheet bundle rule
     */
    public ReverseProxy.ReverseProxyRule cssRule(String pluginName) {
        ReverseProxy.FileReverseProxyProvider
            file = new ReverseProxy.FileReverseProxyProvider("admin", "style.css");
        return new ReverseProxy.ReverseProxyRule("/admin/style.css", file);
    }
}
